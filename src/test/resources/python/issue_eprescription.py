#!/usr/bin/env python3
"""
Issue a KBV 1.3 e-prescription against the test FD via ere-ps-app.

Pick a template from a hardcoded list of 10 KBV 1.3 example bundles

Requires:  pip install requests
Run:       python src/test/resources/python/issue_eprescription.py
           # → prompts for SMC-B (if multiple), KVNR (from eGK or manual), template
           python src/test/resources/python/issue_eprescription.py --kvnr X110624006 \\
               --template Beispiel_2_PZN.xml --smc-b SMC-B-35
ere-ps-app:       http://localhost:8080
Cards:     HBA, doctor's SMC-B, (eGK optional)
"""

from __future__ import annotations

import argparse
import re
import sys
import uuid
import xml.etree.ElementTree as ET
from datetime import date, datetime, timezone
from pathlib import Path

import requests

BASE_URL = "http://localhost:8080"

EXAMPLES_DIR = Path("src/test/resources/examples-kbv-fhir-erp-v1-3")
TEMPLATES = [
    EXAMPLES_DIR / "Beispiel_2_PZN.xml",
    EXAMPLES_DIR / "Beispiel_10_1.xml",
    EXAMPLES_DIR / "Beispiel_10_2.xml",
    EXAMPLES_DIR / "Beispiel_16_Wirkstoffverordnung_AaV.xml",
    EXAMPLES_DIR / "Beispiel_21_1_MVO.xml",
    EXAMPLES_DIR / "Beispiel_21_2_MVO.xml",
    EXAMPLES_DIR / "Beispiel_22_Freitextverordnung.xml",
    EXAMPLES_DIR / "Beispiel_23_Rezepturverordnung.xml",
    EXAMPLES_DIR / "Beispiel_24_Rezepturverordnung_SEL.xml",
    EXAMPLES_DIR / "Beispiel_25_Rezepturverordnung.xml",
]

FHIR_NS = "http://hl7.org/fhir"
ET.register_namespace("", FHIR_NS)


def Q(local: str) -> str:
    return f"{{{FHIR_NS}}}{local}"


SESSION = requests.Session()


def fetch_cards() -> list[dict]:
    r = requests.get(
        f"{BASE_URL}/workflow/cards", headers={"Accept": "application/json"}
    )
    r.raise_for_status()
    return r.json()["cards"]["card"]


def resolve_smcb_handle(cards: list[dict], explicit: str | None) -> str:
    smcbs = [c for c in cards if c["cardType"] == "SMC_B"]
    handles = [c["cardHandle"] for c in smcbs]
    if explicit:
        if explicit not in handles:
            sys.exit(
                f"--smcb {explicit!r} not in terminal. Available SMC-Bs: {handles}"
            )
        return explicit
    if not smcbs:
        sys.exit("No SMC-B in the terminal.")
    if len(smcbs) == 1:
        return smcbs[0]["cardHandle"]
    print("Available SMC-Bs in the terminal:")
    for i, c in enumerate(smcbs, 1):
        print(f"  {i}. {c['cardHandle']}  {c['cardHolderName']}")
    raw = input("Pick SMC-B (number): ").strip()
    try:
        idx = int(raw)
        if not 1 <= idx <= len(smcbs):
            raise ValueError
    except ValueError:
        sys.exit(f"Invalid selection: {raw!r}")
    return smcbs[idx - 1]["cardHandle"]


def resolve_kvnr(cards: list[dict], explicit: str | None) -> str:
    if explicit:
        return explicit
    egks = [c for c in cards if c["cardType"] == "EGK" and c.get("kvnr")]
    if not egks:
        raw = input("No eGK in terminal. KVNR: ").strip()
        if not raw:
            sys.exit("KVNR required.")
        return raw
    if len(egks) == 1:
        kvnr = egks[0]["kvnr"]
        print(f"Using KVNR {kvnr} ({egks[0]['cardHolderName']})")
        return kvnr
    print("eGKs in the terminal:")
    for i, c in enumerate(egks, 1):
        print(f"  {i}. {c['kvnr']}  {c['cardHolderName']}")
    raw = input("Pick KVNR (number): ").strip()
    try:
        idx = int(raw)
        if not 1 <= idx <= len(egks):
            raise ValueError
    except ValueError:
        sys.exit(f"Invalid selection: {raw!r}")
    return egks[idx - 1]["kvnr"]


def section(t: str) -> None:
    print(f"\n=== {t} ===\n")


def render_bundle(template: Path, kvnr: str) -> tuple[str, str]:
    """Returns (xml_string, prescription_id_placeholder)."""
    root = ET.parse(template).getroot()

    now = datetime.now(timezone.utc)
    now_iso = now.strftime("%Y-%m-%dT%H:%M:%SZ")
    today_date = now.date()
    today = today_date.isoformat()

    root.find(Q("id")).set("value", str(uuid.uuid4()))
    root.find(Q("timestamp")).set("value", now_iso)
    presc_id_el = root.find(f"{Q('identifier')}/{Q('value')}")
    presc_id_placeholder = presc_id_el.get("value")

    for entry in root.findall(Q("entry")):
        resource = entry.find(Q("resource"))[0]
        rtype = resource.tag.split("}", 1)[-1]
        if rtype == "Composition":
            resource.find(Q("date")).set("value", now_iso)
        elif rtype == "MedicationRequest":
            a = resource.find(Q("authoredOn"))
            # MVO redemption period (Zeitraum) is anchored to authoredOn; shift
            # it by the same delta so start ≥ authoredOn after we move authoredOn.
            delta = today_date - date.fromisoformat(a.get("value")[:10])
            a.set("value", today)
            for ext in resource.iter(Q("extension")):
                if ext.get("url").endswith("KBV_EX_ERP_Multiple_Prescription"):
                    for sub in ext.findall(Q("extension")):
                        if sub.get("url") != "Zeitraum":
                            continue
                        period = sub.find(Q("valuePeriod"))
                        for name in ("start", "end"):
                            el = period.find(Q(name))
                            if el is not None:
                                d = date.fromisoformat(el.get("value")[:10])
                                el.set("value", (d + delta).isoformat())
        elif rtype == "Patient":
            for ident in resource.findall(Q("identifier")):
                sys_el = ident.find(Q("system"))
                if sys_el is not None and "kvid-10" in sys_el.get("value"):
                    ident.find(Q("value")).set("value", kvnr)

    return ET.tostring(root, encoding="unicode"), presc_id_placeholder


def post(path: str, **kwargs) -> requests.Response:
    return SESSION.post(f"{BASE_URL}{path}", **kwargs)


def first_identifier(task: dict, pattern: str) -> str:
    return next(
        i["value"] for i in task["identifier"] if re.search(pattern, i["system"])
    )


def issue_one(template: Path, kvnr: str) -> bool:
    """Run the full issuance flow for one template. Returns True on success."""
    # Step 1: Load the example bundle and rewrite the bits that must be
    # current for this run: fresh Bundle.id, today's timestamp/date,
    # the chosen KVNR. Everything else stays as in the template.
    bundle, presc_id_placeholder = render_bundle(template, kvnr)

    # Step 2: Local KBV validation. Cheap, catches profile errors before we
    # bother the FD or burn an HBA signature.
    section("/validate")
    r = post(
        "/validate",
        data=bundle,
        headers={"Content-Type": "application/xml", "Accept": "application/xml"},
    )
    print(r.text or "(empty — no validation errors)")
    if "<error>" in r.text:
        print("Bundle did not validate — skipping this template.")
        return False

    # Step 3: Ask the FD to create a Task. The FD returns the real
    # prescriptionId and accessCode that the rest of the flow needs.
    section("/workflow/task")
    r = post("/workflow/task", data="", headers={"Accept": "application/json"})
    print(r.text)
    task = r.json()
    task_id = task["id"]
    prescription_id = first_identifier(
        task, r"PrescriptionID|GEM_ERP_NS_PrescriptionId"
    )
    access_code = first_identifier(task, r"AccessCode|GEM_ERP_NS_AccessCode")
    print(f"prescriptionId = {prescription_id}")
    print(f"accessCode     = {access_code}")

    # Step 4: Patch the template's placeholder prescriptionId with the real
    # one from the FD, then have the HBA sign the bundle.
    bundle = bundle.replace(presc_id_placeholder, prescription_id)

    section("/workflow/sign")
    r = post(
        "/workflow/sign",
        data=bundle,
        headers={"Content-Type": "application/xml", "Accept": "text/plain"},
    )
    signed_b64 = r.text
    print(f"(base64 signed document, {len(signed_b64)} chars)")

    # Step 5: Upload the signed bundle to the FD. Empty response = HTTP 200 =
    # the prescription is now live on the FD under the chosen KVNR.
    section("/workflow/update")
    r = post(
        "/workflow/update",
        json={"taskId": task_id, "accessCode": access_code, "signedBytes": signed_b64},
        headers={"Accept": "text/plain"},
    )
    print(r.text or "(empty — FD accepted upload)")

    print(
        f"Done.  Task {task_id}  prescriptionId {prescription_id}  accessCode {access_code}"
    )
    return True


def resolve_template(name: str | None) -> Path:
    by_name = {t.name: t for t in TEMPLATES}
    if name:
        if name not in by_name:
            sys.exit(
                f"Unknown template: {name!r}. Known names:\n  " + "\n  ".join(by_name)
            )
        return by_name[name]
    print("Available templates:")
    for i, t in enumerate(TEMPLATES, 1):
        print(f"  {i:2}. {t.name}")
    raw = input("Pick template (number): ").strip()
    try:
        idx = int(raw)
        if not 1 <= idx <= len(TEMPLATES):
            raise ValueError
    except ValueError:
        sys.exit(f"Invalid selection: {raw!r}")
    return TEMPLATES[idx - 1]


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    p.add_argument(
        "--kvnr",
        default=None,
        help="Versicherten-ID (10-digit KVNR). Prompted if omitted.",
    )
    p.add_argument(
        "--template", default=None, help="Template file name. Prompted if omitted."
    )
    p.add_argument(
        "--smcb",
        default=None,
        help="SMC-B card handle (auto-detected when only one is in the terminal)",
    )
    return p.parse_args()


def main() -> int:
    args = parse_args()

    # Step 1: Ask the Konnektor what cards are currently in the terminal.
    cards = fetch_cards()

    # Step 2: Pick the SMC-B (doctor's institution card) and attach its handle
    # as X-SMCBHandle so every downstream call (task / sign / update) uses it.
    smcb_handle = resolve_smcb_handle(cards, args.smcb)
    SESSION.headers["X-SMCBHandle"] = smcb_handle
    print(f"Using SMC-B handle: {smcb_handle}")

    # Step 3: Pick the KVNR. By default we read it off an eGK in the terminal;
    # if there's no eGK present, fall back to a manual prompt.
    kvnr = resolve_kvnr(cards, args.kvnr)
    print(f"KVNR: {kvnr}")

    # Step 4: Pick which KBV example template to issue.
    template = resolve_template(args.template)
    print(f"Template: {template.name}")

    # Step 5: Run the full issuance flow.
    ok = issue_one(template, kvnr)

    print(
        "\nVerify (use a pharmacy SMC-B, not the doctor's):\n"
        '  curl "http://localhost:8080/pharmacy/Task?egkHandle=<EGK-HANDLE>&smcbHandle=<SMC-B-HANDLE>"'
    )

    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())

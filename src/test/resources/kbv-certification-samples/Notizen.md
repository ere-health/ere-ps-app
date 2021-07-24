= Vertreterregelung

...
PFLICHTFUNKTION ERP
P36-34 Abbildung verantwortliche Person in KBV_PR_ERP_Composition
Das ERP ermöglicht es neben der Person, welche die Verordnung ausstellt, auch zusätzliche eine für die
Verordnung verantwortliche Person zu hinterlegen.
Begründung:
Sofern es sich bei der die Verordnung ausstellende Person um einen Arzt in Weiterbildung handelt, ist es
ggfs. nötig, den zur Weiterbildung ermächtigten Arzt (im vertrags(zahn)ärztlichen Bereich) bzw. den
beauftragenden Facharzt (im Krankenhaus) zu hinterlegen. Gleiches gilt im Rahmen von
Vertretungssituationen. In Papierform ermöglicht bisher der Arztstempel eine Zuordnung zum
verantwortlichen Vertrags(zahn)arzt / Facharzt.
Akzeptanzkriterium:
Die Software muss in dem Profil KBV_PR_ERP_Composition ermöglichen, dass neben der ausstellenden
Person (über composition.author.reference und composition.author.type = „Practitioner“) auch die
verantwortliche Person (über composition.attester.party.reference) hinterlegt werden kann. 
Dies darf nur dann erfolgen, wenn in der Instanz des referenzierten Profils der ausstellenden Person
(composition.author.reference und composition.author.type = „Practitioner“) der Typ der
ausstellenden Person (practioner.qualification.code.coding.value) mit „03“ oder „04“ belegt ist.

https://update.kbv.de/ita-update/DigitaleMuster/ERP/KBV_ITA_VGEX_Technische_Anlage_ERP.pdf
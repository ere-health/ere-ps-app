# Validation

This page contains some examples for the validater that comes as part of the ere-health-solution:

## Sending an XML Bundle
```
$ curl -v --data @src/test/resources/kbv-zip/PF01.xml -H "Content-Type: application/xml" -H "Accept: application/xml" http://localhost:8080/validate
*   Trying 127.0.0.1:8080...
* TCP_NODELAY set
* Connected to localhost (127.0.0.1) port 8080 (#0)
> POST /validate HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.68.0
> Content-Type: application/xml
> Accept: application/xml
> Content-Length: 11539
> Expect: 100-continue
> 
* Mark bundle as not supporting multiuse
< HTTP/1.1 100 Continue
* We are completely uploaded and fine
* Mark bundle as not supporting multiuse
< HTTP/1.1 200 OK
< Content-Length: 0
< 
* Connection #0 to host localhost left intact
```

## Sending some invalidate XML

```
$ curl -v --data "<Test></Test>" -H "Content-Type: application/xml" -H "Accept: application/xml" http://localhost:8080/validate
*   Trying 127.0.0.1:8080...
* TCP_NODELAY set
* Connected to localhost (127.0.0.1) port 8080 (#0)
> POST /validate HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.68.0
> Content-Type: application/xml
> Accept: application/xml
> Content-Length: 13
> 
* upload completely sent off: 13 out of 13 bytes
* Mark bundle as not supporting multiuse
< HTTP/1.1 400 Bad Request
< Content-Type: application/xml;charset=UTF-8
< Content-Length: 156
< 
<errors>
    <error> Next issue FATAL - Test - Dies scheint keine FHIR-Ressource zu sein (unbekannter Namensraum/Name "noNamespace::Test")</error>
</errors>
* Connection #0 to host localhost left intact
```

## Sending a JSON Bundle

```
$ curl -v --data @src/test/resources/bundle-json/0428d416-149e-48a4-977c-394887b3d85c.json -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/validate
*   Trying 127.0.0.1:8080...
* TCP_NODELAY set
* Connected to localhost (127.0.0.1) port 8080 (#0)
> POST /validate HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.68.0
> Content-Type: application/json
> Accept: application/json
> Content-Length: 20680
> Expect: 100-continue
> 
* Mark bundle as not supporting multiuse
< HTTP/1.1 100 Continue
* We are completely uploaded and fine
* Mark bundle as not supporting multiuse
< HTTP/1.1 200 OK
< Content-Type: application/json
< Content-Length: 14
< 
* Connection #0 to host localhost left intact
{"valid":true}
```

## Sending some invalid JSON

```
$ curl -v --data @src/test/resources/bundle-json/0428d416-149e-48a4-977c-394887b3d85c-invalid.json -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:8080/validate
*   Trying 127.0.0.1:8080...
* TCP_NODELAY set
* Connected to localhost (127.0.0.1) port 8080 (#0)
> POST /validate HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.68.0
> Content-Type: application/json
> Accept: application/json
> Content-Length: 18623
> Expect: 100-continue
> 
* Mark bundle as not supporting multiuse
< HTTP/1.1 100 Continue
* We are completely uploaded and fine
* Mark bundle as not supporting multiuse
< HTTP/1.1 200 OK
< Content-Type: application/json
< Content-Length: 4016
< 
{"errors":[" Next issue ERROR - Bundle.entry[5].resource.address - Array cannot be empty - the property should not be present if it has no values"," Next issue INFORMATION - Bundle.entry[0].resource.ofType(Composition).author[0] - Details für Practitioner/e33d2afd-44c8-462b-80e5-52dbe5ebf359 Abgleich gegen Profilhttp://hl7.org/fhir/StructureDefinition/Practitioner"," Next issue ERROR - Bundle.entry[0].resource.ofType(Composition).author[0] - Es konnte kein passendes Profil für Practitioner/e33d2afd-44c8-462b-80e5-52dbe5ebf359 unter den Auswahlmöglichkeiten [https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3] gefunden werden"," Next issue INFORMATION - Bundle.entry[0].resource.ofType(Composition).author[0] - Details für Practitioner/e33d2afd-44c8-462b-80e5-52dbe5ebf359 Abgleich gegen Profilhttps://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3"," Next issue ERROR - Bundle.entry[0].resource.ofType(Composition).section[0].entry[0] - Es konnte kein passendes Profil für MedicationRequest/06dc1594-509a-4f4c-ada7-dfd477a02d86 unter den Auswahlmöglichkeiten [https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.0.1] gefunden werden"," Next issue INFORMATION - Bundle.entry[0].resource.ofType(Composition).section[0].entry[0] - Details für MedicationRequest/06dc1594-509a-4f4c-ada7-dfd477a02d86 Abgleich gegen Profilhttps://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.0.1"," Next issue ERROR - Bundle.entry[0].resource.ofType(Composition).section[1].entry[0] - Es konnte kein passendes Profil für Coverage/df0f2536-97b9-4bae-99cc-83ba2e8371e4 unter den Auswahlmöglichkeiten [https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3] gefunden werden"," Next issue INFORMATION - Bundle.entry[0].resource.ofType(Composition).section[1].entry[0] - Details für Coverage/df0f2536-97b9-4bae-99cc-83ba2e8371e4 Abgleich gegen Profilhttps://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3"," Next issue INFORMATION - Bundle.entry[1].resource.ofType(MedicationRequest).requester - Details für Practitioner/e33d2afd-44c8-462b-80e5-52dbe5ebf359 Abgleich gegen Profilhttp://hl7.org/fhir/StructureDefinition/Practitioner"," Next issue INFORMATION - Bundle.entry[1].resource.ofType(MedicationRequest).insurance[0] - Details für Coverage/df0f2536-97b9-4bae-99cc-83ba2e8371e4 Abgleich gegen Profilhttp://hl7.org/fhir/StructureDefinition/Coverage"," Next issue ERROR - Bundle.entry[1].resource.ofType(MedicationRequest) - MedicationRequest.extension:BVG: mindestens erforderlich = 1, aber nur gefunden 0"," Next issue ERROR - Bundle.entry[1].resource.ofType(MedicationRequest).requester - Es konnte kein passendes Profil für Practitioner/e33d2afd-44c8-462b-80e5-52dbe5ebf359 unter den Auswahlmöglichkeiten [https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3] gefunden werden"," Next issue INFORMATION - Bundle.entry[1].resource.ofType(MedicationRequest).requester - Details für Practitioner/e33d2afd-44c8-462b-80e5-52dbe5ebf359 Abgleich gegen Profilhttps://fhir.kbv.de/* Connection #0 to host localhost left intact
StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3"," Next issue ERROR - Bundle.entry[1].resource.ofType(MedicationRequest).insurance[0] - Es konnte kein passendes Profil für Coverage/df0f2536-97b9-4bae-99cc-83ba2e8371e4 unter den Auswahlmöglichkeiten [https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3] gefunden werden"," Next issue INFORMATION - Bundle.entry[1].resource.ofType(MedicationRequest).insurance[0] - Details für Coverage/df0f2536-97b9-4bae-99cc-83ba2e8371e4 Abgleich gegen Profilhttps://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3"," Next issue ERROR - Bundle.entry[4].resource.ofType(Practitioner).name[0] - Practitioner.name:name.family: mindestens erforderlich = 1, aber nur gefunden 0"," Next issue ERROR - Bundle.entry[6].resource.ofType(Coverage).payor[0].identifier.system - Der Wert ist \"http://my-system.com\", muss aber \"http://fhir.de/NamingSystem/arge-ik/iknr\" sein."],"valid":false}
```
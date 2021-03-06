#!/bin/bash
mkdir target
curl -X POST -H "Accept: application/xml" http://localhost:8080/workflow/task > target/OperationOutCome-1.xml
curl -X POST -H "Accept: application/xml" http://localhost:8080/workflow/task > target/OperationOutCome-2.xml

# get the task id
TaskID1=`xmllint --xpath "string((//*[local-name()='id'])[1]/@value)" target/OperationOutCome-1.xml`
# get the value of the first value tag (Prescription ID)
PrescriptionID1=`xmllint --xpath "string((//*[local-name()='value'])[1]/@value)" target/OperationOutCome-1.xml`
# get the value of the second value tag (AccessCode)
AccessCode1=`xmllint --xpath "string((//*[local-name()='value'])[2]/@value)" target/OperationOutCome-1.xml`

# get the task id
TaskID2=`xmllint --xpath "string((//*[local-name()='id'])[1]/@value)" target/OperationOutCome-2.xml`
# get the value of the first value tag (Prescription ID)
PrescriptionID2=`xmllint --xpath "string((//*[local-name()='value'])[1]/@value)" target/OperationOutCome-2.xml`
# get the value of the second value tag (AccessCode)
AccessCode2=`xmllint --xpath "string((//*[local-name()='value'])[2]/@value)" target/OperationOutCome-2.xml`

echo "Generated Codes: $TaskID1 $PrescriptionID1 $AccessCode1 $TaskID2 $PrescriptionID2 $AccessCode2"

UUID1=`uuid`
UUID2=`uuid`
# Replace the current Prescription ID with the just fetched
PF03=`cat examples/PF03.xml | perl -p -w -e s/160.524.266.448.858.41/$PrescriptionID1/ | perl -p -w -e s/20915188-b868-41fd-bc3e-5ddb5599ad24/$UUID1/`
PF04=`cat examples/PF04.xml | perl -p -w -e s/160.328.876.617.846.18/$PrescriptionID2/ | perl -p -w -e s/22922a2d-bebd-47d6-9a36-553232ab69e3/$UUID2/`

# Generate the previews required by KBV that have to be shown to the doctor for signing
echo -e "$PF03" | curl -X POST --data-binary @- -H "Content-Type: application/xml" http://localhost:8080/kbv/transform > target/PF03.html
echo -e "$PF04" | curl -X POST --data-binary @- -H "Content-Type: application/xml" http://localhost:8080/kbv/transform > target/PF04.html

# Sign the updated bundles
echo -e "$PF03\n$PF04" | curl -X POST --data-binary @- -H "Content-Type: application/xml" -H "Accept: text/plain" http://localhost:8080/workflow/batch-sign > target/signed-e-prescriptions.dat

# Post the bundles to update the given prescription
signedBase64Document1=`head -1 target/signed-e-prescriptions.dat`
signedBase64Document2=`head -2 target/signed-e-prescriptions.dat | tail -1`

updateERezept1=`echo "{\"taskId\": \"$TaskID1\", \"accessCode\": \"$AccessCode1\",\"signedBytes\": \"$signedBase64Document1\" }"`
updateERezept2=`echo "{\"taskId\": \"$TaskID2\", \"accessCode\": \"$AccessCode2\",\"signedBytes\": \"$signedBase64Document2\" }"`

echo "Posting: $updateERezept1"
echo -e "$updateERezept1" | curl -X POST --data-binary @- -H "Content-Type: application/json" -H "Accept: text/plain" http://localhost:8080/workflow/update
echo "Posting: $updateERezept2"
echo -e "$updateERezept2" | curl -X POST --data-binary @- -H "Content-Type: application/json" -H "Accept: text/plain" http://localhost:8080/workflow/update

# Create pdf print out
PF03_escaped=`echo $PF03 | sed 's/"/\\\\"/g'`
PF04_escaped=`echo $PF04 | sed 's/"/\\\\"/g'`
echo -e "[{\"accessCode\":\"$AccessCode1\", \"mimeType\": \"application/xml\", \"bundle\": \"$PF03_escaped\"}, {\"accessCode\":\"$AccessCode2\", \"mimeType\": \"application/xml\", \"bundle\": \"$PF04_escaped\"}]" | curl -X POST --data-binary @- -H "Content-Type: application/json" -H "Accept: application/pdf" http://localhost:8080/document/bundles > target/print-out.pdf
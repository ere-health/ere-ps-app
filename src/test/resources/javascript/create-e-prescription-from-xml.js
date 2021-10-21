

function postToUrl(urlString, data, accept, content) {
    var url = new java.net.URL(urlString);
    // Open connection to url
    var conn = url.openConnection();
    conn.setDoOutput(true);
    if(accept) {
        conn.setRequestProperty ("Accept", accept);
    }
    if(content) {
        conn.setRequestProperty ("Content-Type", content);
    }
    // Send request
    var outStream = conn.getOutputStream();
    var outWriter = new java.io.OutputStreamWriter(outStream);
    outWriter.write(data);
    outWriter.close();

    return conn.getInputStream().readAllBytes();
}

var task, taskId, prescriptionId, accessCode, signedBase64Document;

// https://github.com/gematik/api-erp/blob/master/docs/erp_bereitstellen.adoc

// Create a task
try {
    // only an example
    var taskXml = new java.lang.String(postToUrl("http://localhost:8080/workflow/task", "", "application/xml"));
    println(taskXml);
    var taskString = new java.lang.String(postToUrl("http://localhost:8080/workflow/task", "", "application/json"));
    println(taskString);
    task = JSON.parse(taskString);
    taskId = task.id;
    prescriptionId = task.identifier.filter(function (o) {
        return o.system == "https://gematik.de/fhir/NamingSystem/PrescriptionID";
    })[0].value;
    accessCode = task.identifier.filter(function (o) {
        return o.system == "https://gematik.de/fhir/NamingSystem/AccessCode";
    })[0].value;
} catch(e) {
    println("ERROR during creating task: "+e.message);
}

// Add the prescription id to the bundle and sign it
try {
    var xmlBundle = new java.lang.String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/test/resources/simplifier_erezept/0428d416-149e-48a4-977c-394887b3d85c.xml")));
    xmlBundle = xmlBundle.replace("0428d416-149e-48a4-977c-394887b3d85c", java.util.UUID.randomUUID().toString());
    xmlBundle = xmlBundle.replace("160.100.000.000.002.36", prescriptionId);
    // TODO: show KBV xslt transformed stylesheet
    var signedBase64Document = new java.lang.String(postToUrl("http://localhost:8080/workflow/sign", xmlBundle, "text/plain", "application/xml"));
    println(signedBase64Document);
} catch(e) {
    println("ERROR during signing task: "+e.message);
}

// Update the task with the signed bundle
try {
    var updateERezept = {
        "taskId": taskId,
        "accessCode": accessCode,
        "signedBytes": signedBase64Document
    };
    postToUrl("http://localhost:8080/workflow/update", JSON.stringify(updateERezept), "text/plain", "application/json");
} catch(e) {
    println("ERROR updating task: "+e.message);
}

// Create PDF from task
try {
    var bundles = [{
        "accessCode": accessCode,
        "mimeType": "application/xml",
        "bundle": xmlBundle
    }];
    var bundlesString = JSON.stringify(bundles);
    println(bundlesString);
    var pdfDocument = postToUrl("http://localhost:8080/document/bundles", bundlesString, "application/pdf", "application/json");
    java.nio.file.Files.write(java.nio.file.Paths.get("target/javascript-xml-0428d416-149e-48a4-977c-394887b3d85c.pdf"), pdfDocument);
} catch(e) {
    println("ERROR during producing pdf: "+e.message);
}

// Abort the task
try {
    var abortERezept = {
        "taskId": taskId,
        "accessCode": accessCode
    };
    postToUrl("http://localhost:8080/workflow/abort", JSON.stringify(abortERezept), "text/plain", "application/json");
} catch(e) {
    println("ERROR abort task: "+e.message);
}
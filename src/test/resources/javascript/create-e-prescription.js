

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

    //Capture Response
    var respCode = conn.getResponseCode();
    var inputStream = conn.getInputStream();
    var streamReader = new java.io.InputStreamReader(inputStream);
    var respStream = new java.io.BufferedReader(streamReader);
    var buffer = '';
    var line = null;
    while ((line = respStream.readLine()) != null) {
        buffer = buffer + line;
    }
    respStream.close();
    return buffer;
}

var task, prescriptionId, accessCode, signedBase64Document;

// Create a task
try {
   var taskString = postToUrl("http://localhost:8080/workflow/task", "", "application/json");
   println(taskString);
   task = JSON.parse(taskString);
   prescriptionId = task.identifier[0].value;
   accessCode = task.identifier[1].value;
} catch(e) {
    println("ERROR during creating task: "+e.message);
}

// Add the prescription id to the task and sign the bundle
try {
    var jsonBundle = JSON.parse(new java.lang.String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/test/resources/bundle-json/0428d416-149e-48a4-977c-394887b3d85c.json"))));
    jsonBundle.identifier.value = prescriptionId;
    var signedBase64Document = postToUrl("http://localhost:8080/workflow/sign", JSON.stringify(jsonBundle), "text/plain");
    println(signedBase64Document);
} catch(e) {
    println("ERROR during signing task: "+e.message);
}

// Update the task wit hthe signed bundle
try {
    var updateERezept = {
        "taskId": prescriptionId,
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
        "bundle": jsonBundle
    }];
    var pdfDocument = postToUrl("http://localhost:8080/document/bundles", JSON.stringify(bundles), "application/pdf", "application/json");
    println(pdfDocument);
    java.nio.file.Files.write(java.nio.file.Paths.get("target/javascript-0428d416-149e-48a4-977c-394887b3d85c.pdf"), pdfDocument);
} catch(e) {
    println("ERROR during producing pdf: "+e.message);
}

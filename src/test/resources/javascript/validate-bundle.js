

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

    java.lang.System.out.println(conn.getResponseCode());

    return conn.getInputStream().readAllBytes();
}

// Validate the bundle
try {
    var jsonBundle = JSON.parse(new java.lang.String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/test/resources/bundle-json/0428d416-149e-48a4-977c-394887b3d85c.json"))));
    var validationResponse = new java.lang.String(postToUrl("http://localhost:8080/validate", JSON.stringify(jsonBundle), "", "application/json"));
    println("/validate responded: "+validationResponse);
} catch(e) {
    println("ERROR during bundle validation: "+e.message);
}

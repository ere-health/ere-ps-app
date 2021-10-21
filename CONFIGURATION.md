# Configuration

The ere-health application read configuration is the following order from most important to least important:
 * RuntimeConfiguration as part of a web socket message or a HTTP header
 * user.properties
 * Java process parameter e.g. -Dquarkus.http.port=8081
 * Environment variables e.g. export ERE_DIRECTORY_WATCHER_DIR="my-watch-dir"
 * application.properties
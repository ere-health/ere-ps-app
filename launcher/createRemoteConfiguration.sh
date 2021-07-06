echo "Creating remote configuration file from archive windows-installer.zip in folder config-creation"
echo ""
mvn clean install
echo ""
java -jar target/launcher-1.0-SNAPSHOT-jar-with-dependencies.jar --create-remote-config
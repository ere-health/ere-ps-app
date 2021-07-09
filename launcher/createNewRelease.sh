mkdir -p windows-installer &&
cd windows-installer &&
rm -f *.zip &&
mvn clean install -f ../../pom.xml &&
cp -r ../../target/quarkus-app/ . &&
zip -r windows-installer.zip quarkus-app &&
rm -r quarkus-app &&
mvn clean install -f ../pom.xml &&
cd .. &&
java -jar target/ere-health-launcher.jar --create-remote-config &&
echo "The new installer archive and configuration file have been created in the folder windows-installer"

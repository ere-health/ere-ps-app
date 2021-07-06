rm *.zip
cp -r ../../target/quarkus-app/ . &&
zip -r windows-installer.zip service_installer.exe service_installer.xml quarkus-app &&
rm -r quarkus-app

rm *.zip
cp -r ../target/quarkus-app/ . &&
zip -r windows-installer.zip installer.bat service_installer.exe service_installer.xml quarkus-app &&
rm -r quarkus-app

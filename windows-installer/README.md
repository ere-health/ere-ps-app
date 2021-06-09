Instructions:

1. Run "mvn clean install" from the root folder to create an up-to-date version of the application to use
2. Run script createWindowsInstaller.sh to create the installer zip archive in the same folder
3. In Windows, extract all files from the zip archive to c:\ere-ps-app
3b. If you want to select another path you'll have to put it as well in service_installer.xml in the argument line
4. Run installer.bat to install and start the Windows service containing our application

Make sure Java is installed on the doctors' computer as well, it won't be included here.
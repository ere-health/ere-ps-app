# Overview
This launcher checks for the latest version of the ere-health app online, downloads it and extracts it if needed and 
finally launches it. It can also be used to download it for the first time and will create a startup script to run it
at the start of Windows. For now only Windows is supported.

# How it works
This launcher will search for a remote configuration file detailing the most up-to-date remote archive available 
for download, including its size and checksum. It uses it to compare the remote archive to the local one and 
if they don't match it will download and extract the latest one before launching the ere-health application.

For the first installation the practitioner will run the installer script ere-health-installer.bat. It downloads
a needed jre and the exe file of the launcher first and then runs the launcher.

# Usage
Simply run the jar (target/ere-health-launcher.jar) or the exe (target/ere-health-launcher.exe) without argument 
to start the installation/update process and launch the application.
  
# Create a new release
Run the script createRelease.sh to automatically create the installer archive and 
remote configuration file in the windows-installer folder. 
You'll then have to copy these two files in our distribution server.

If you want to update the launcher itself you have to copy the ere-health-launcher.exe in our delivery server and then
ask the practitioner to run ere-health-installer.bat again.

# Test
You can use a local Nginx instance running in docker to test the update feature. Create a new release using the script and
run the Nginx local instance by running the script startTestNginx.sh.
Don't forget to also change the remote.server configuration variable in the application.properties file of the launcher
to http://localhost:8080.

# Dependencies
- Update4j for the update feature : https://github.com/update4j/update4j
- Progressbar to have a nice and clean progress bar during downloads :  https://github.com/ctongfei/progressbar

# Possible improvement
- A private key could be used to sign the configuration file and archive for extra security

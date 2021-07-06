# Overview
This launcher checks for the latest version of the ere-health app online before launching it. It can also be used to 
download it for the first time, but the installer script will still have to be used for the creation of the Windows service. 
If the application is outdated or missing the launcher will download the latest Windows installer archive and extract it 
in the designated folder (check the configuration).


# How it works
This launcher will search for a remote configuration file detailing the most up-to-date remote archive available 
for download, including its size and checksum. It then uses it to compare the remote archive to the local one, 
if they don't match it will download and extract the latest one before launching the ere-health application.


# Usage
- Simply run the jar (launcher-1.0-SNAPSHOT-jar-with-dependencies) without argument to start the update process 
  and launch the application.
- To create a new remote configuration file put the new Windows installer archive (check the windows-installer folder 
  in the ere-app repo) into the folder config-creation (can be modified in the configuration). Then simply run the script 
  createRemoteConfiguration.sh. It runs the jar with the argument --create-remote-config. Once it's done 
  upload both the configuration file and archive into the remote server.


# Test
You can use a local Nginx instance running in docker to test the update feature. Place a windows-installer archive
into the config-creation folder, create a configuration file using the script createRemoteConfiguration.sh and
finally run the Nginx local instance by running the script startTestNginx.sh.
Don't forget to also change the remote.server configuration variable in the application.properties file of the launcher
to http://localhost:8080.


# Dependencies
- Update4j for the update feature : https://github.com/update4j/update4j
- Progressbar to have a nice and clean progress bar during downloads :  https://github.com/ctongfei/progressbar


# Possible improvements
- A private key could be used to sign the configuration file and archive for extra security
- This launcher could take care of the first installation as well (downloading Java if needed + the creation of the
  Windows service)

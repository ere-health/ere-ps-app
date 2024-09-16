# INSTALLATION

[Back to Readme](README.md)

---

## Windows

Download and execute the following https://ere.health/ere-health-installer.bat on Windows 10.

This will:
 * Install a JDK if not available
 * Install Chrome if not available
 * Install ere-health and add it to autostart

---

## Linux (systemd service)

Prequisite:
 * Java 11 JDK

```
git clone --recurse-submodules https://github.com/ere-health/ere-ps-app.git
cd ere-ps-app
mvn package
cd linux-service
sudo ./installer.sh
```

If you want to configure this application do the following:
```
sudo mkdir /opt/ere-health/config
sudo cp ../src/main/resources/application.properties /opt/ere-health/config
# by default the PU profile is active
```

This will:
 * create the folder /opt/ere-health
 * fill it with the ere-health application
 * Create a systemd service

Check the log

```
sudo journalctl -f -u ere-health
```

Based on: https://dzone.com/articles/run-your-java-application-as-a-service-on-ubuntu



---

[Back to Readme](README.md)
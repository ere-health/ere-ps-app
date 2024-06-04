#!/bin/bash
mkdir /opt/ere-health/
cp -r ../target/quarkus-app/* /opt/ere-health/
mkdir /opt/ere-health/config
cp ../src/main/resources/application.properties /opt/ere-health/config
cp run.sh /opt/ere-health/
cp ere-health.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable ere-health.service
sudo systemctl start ere-health
sudo systemctl status ere-health

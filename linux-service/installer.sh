#!/bin/bash
cp -r ../target/quarkus-app /opt/ere-health
mkdir -p /opt/ere-health/application/quarkus-app/
mkdir -p /etc/ere-health/config
cp ../src/main/resources/application.properties /etc/ere-health/config
ln -s /etc/ere-health/config /opt/ere-health/config
mv /opt/ere-health/server.keystore /opt/ere-health/application/quarkus-app/server.keystore
cp ere-health.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable ere-health.service
sudo systemctl start ere-health
sudo systemctl status ere-health

#!/bin/bash
cp -r ../target/quarkus-app /opt/ere-health
cp run.sh /opt/ere-health
cp ere-health.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable ere-health.service
sudo systemctl start ere-health
sudo systemctl status ere-health

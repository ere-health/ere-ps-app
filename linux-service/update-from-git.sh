#!/bin/bash
git pull
mvn clean
mvn -DskipTests=true install
sudo systemctl stop ere-health
sudo cp target/quarkus-app/app/* /opt/ere-health/app
sleep 5
sudo systemctl start ere-health
sudo systemctl status ere-health

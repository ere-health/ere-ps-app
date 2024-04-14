#!/bin/bash
git pull
mvn clean
mvn -DskipTests=true install
sudo systemctl stop ere-health
sudo cp target/quarkus-run/app/* /opt/ere-health/quarkus-run/app
sleep 5
sudo systemctl start ere-health
sudo systemctl status ere-health

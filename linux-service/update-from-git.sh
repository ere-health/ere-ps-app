#!/bin/bash
git pull
mvn clean
mvn -DskipTests=true install
sudo systemctl stop ere-health
sudo rm -r /opt/ere-health/app/*
sudo cp target/quarkus-app/app/* /opt/ere-health/app
sudo rm -r /opt/ere-health/quarkus/*
sudo cp -r target/quarkus-app/quarkus/* /opt/ere-health/quarkus
sudo rm -r /opt/ere-health/lib/*
sudo cp -r target/quarkus-app/lib/* /opt/ere-health/lib
sudo bash -c 'echo "$(date --iso-8601=s) $(git rev-parse --verify HEAD)" >> /opt/ere-health/update-linux-deployments.log'
sleep 5
sudo systemctl start ere-health
sudo systemctl status ere-health

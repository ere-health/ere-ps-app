#!/bin/bash
mkdir /opt/ere-health/
cp -r ../target/quarkus-app/* /opt/ere-health/
mkdir /opt/ere-health/config
cp ../src/main/resources/application.properties /opt/ere-health/config
cp run.sh /opt/ere-health/
cp ere-health.service /etc/systemd/system/
mvn -U dependency:copy -Dartifact=io.prometheus.jmx:jmx_prometheus_javaagent:1.0.1:jar -DoutputDirectory=/opt/ere-health
cp src/main/resources/prometheus.yaml /opt/ere-health
cp -r prometheus/ /opt/ere-health/
sudo systemctl daemon-reload
sudo systemctl enable ere-health.service
sudo systemctl start ere-health
sudo systemctl status ere-health

#!/bin/sh
sudo /usr/bin/java -javaagent:jmx_prometheus_javaagent-1.0.1.jar=20000:prometheus.yaml -Dquarkus.profile=PU -jar quarkus-run.jar

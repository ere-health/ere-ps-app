#!/bin/sh
sudo /usr/bin/java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -javaagent:jmx_prometheus_javaagent-1.0.1.jar=20001:prometheus.yaml -Dquarkus.profile=PU -jar quarkus-run.jar

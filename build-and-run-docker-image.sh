#!/bin/sh
docker rm ere-ps-app
docker build . -t ere-ps-app -f src/main/docker/Dockerfile.jvm
docker run -d \
    --name ere-ps-app \
    -p 8080:8080 -p 8443:8443  \
    -e QUARKUS_PROFILE=RU  \
    -v `pwd`/src/main/resources/:/deployments/config ere-ps-app
echo "docker image ere-ps-app running. \n Status with: 'docker ps' \n Logs with: 'docker logs ere-ps-app' \n Kill with: 'docker kill ere-ps-app'"

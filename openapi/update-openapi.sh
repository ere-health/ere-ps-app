#!/bin/bash
curl http://localhost:8080/q/openapi > openapi.yaml
curl -H "Accept: application/json" http://localhost:8080/q/openapi > openapi.json

#!/bin/sh
: "${CN:?CN env var is required}"
CERT_PASSWORD=${CERT_PASSWORD:-password}
CONNECTOR_HOST=${CONNECTOR_HOST:-localhost}
CONNECTOR_PORT=${CONNECTOR_PORT:-8780}
MANDANT_ID=${MANDANT_ID:-Incentergy}
CLIENT_SYSTEM_ID=${CLIENT_SYSTEM_ID:-Incentergy}
WORKPLACE_ID=${WORKPLACE_ID:-ere-ps-app}
if [ -z "$CONNECTOR_URL" ]; then CONNECTOR_URL=https://${CONNECTOR_HOST}:9443; fi
if [ -z "$CARDLINK_URL" ]; then CARDLINK_URL=ws://${CONNECTOR_HOST}:8780/websocket/80276883662000004801-20220128; fi
openssl req -x509 -newkey rsa:4096 \
  -keyout /tmp/key.pem -out /tmp/cert.pem \
  -sha256 -days 365 -nodes \
  -subj "/CN=${CN}"
openssl pkcs12 -export \
  -out /tmp/client.p12 \
  -inkey /tmp/key.pem -in /tmp/cert.pem \
  -passout "pass:${CERT_PASSWORD}"
CERT_B64=$(base64 -w 0 /tmp/client.p12)
printf 'clientCertificate=data:application/x-pkcs12;base64,%s\n' "$CERT_B64" > /deployments/user.properties
printf 'clientCertificatePassword=%s\n' "$CERT_PASSWORD" >> /deployments/user.properties
{
  printf 'auth=certificate\n'
  printf 'clientCertificate=data:application/x-pkcs12;base64,%s\n' "$CERT_B64"
  printf 'clientCertificatePassword=%s\n' "$CERT_PASSWORD"
  printf 'clientSystemId=%s\n' "$CLIENT_SYSTEM_ID"
  printf 'connectorBaseURL=%s\n' "$CONNECTOR_URL"
  printf 'mandantId=%s\n' "$MANDANT_ID"
  printf 'userId=\n'
  printf 'version=PTV4+\n'
  printf 'workplaceId=%s\n' "$WORKPLACE_ID"
  printf 'cardlinkServerURL=%s\n' "$CARDLINK_URL"
} > /deployments/config/konnektoren/8586/user.properties
while true; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://${CONNECTOR_HOST}:${CONNECTOR_PORT}/connector.sds)
  echo "version 1"
  echo "connector.sds: HTTP $STATUS"
  if [ "$STATUS" = "200" ]; then
    echo "connector.sds is ready — starting application..."
    break
  fi
  sleep 5
done
exec "$@"
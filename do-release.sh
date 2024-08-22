#!/bin/bash
DATE=`date +"%Y-%m-%d"`
START_DATE=`date -d "-7 days" +"%Y-%m-%d"`
REV=`git rev-parse --short HEAD`
PREV_TAG=`git tag | grep $START_DATE`
TAG="cardlink.service-health.de-${DATE}-${REV}"
# export JIRA_USER="..."
# export JIRA_API_TOKEN="..."
echo "Creating git $TAG"
git tag $TAG
JSON_VERSION="{\"description\": \"ere-ps-app\", \"name\": \"$TAG\", \"archived\": false, \"released\": true,  \"releaseDate\": \"${DATE}\", \"startDate\": \"${DATE}\", \"projectId\": \"10031\"}"
echo "Creating JIRA version $TAG $JSON_VERSION"
curl -vvv -u "$JIRA_USER:$JIRA_API_TOKEN"  -H "Content-Type: application/json" -d "$JSON_VERSION" https://incentergy.atlassian.net/rest/api/3/version > NEW_JIRA_VERSION.json
JSON_VERSION="{\"description\": \"ere-ps-app\", \"name\": \"$TAG\", \"archived\": false, \"released\": false,  \"releaseDate\": \"${DATE}\", \"startDate\": \"${DATE}\", \"projectId\": \"10000\"}"
echo "Creating JIRA version $TAG $JSON_VERSION"
curl -vvv -u "$JIRA_USER:$JIRA_API_TOKEN"  -H "Content-Type: application/json" -d "$JSON_VERSION" https://incentergy.atlassian.net/rest/api/3/version
NEW_JIRA_VERSION=`jq -r '.id' NEW_JIRA_VERSION.json`
git log $PREV_TAG..$TAG > changelog.txt
ISSUES=`cat changelog.txt | grep NFCKT | grep --color=never -oE 'NFCKT-[0-9]+'`
IFS='
'
for issue in $ISSUES
do
    VERSION_JSON="{ \"update\": { \"fixVersions\": [ {\"add\": {\"name\": \"$TAG\"} } ] } }"
    echo "Adding fixed version to $issue $VERSION_JSON"
    curl -vvv -u "$JIRA_USER:$JIRA_API_TOKEN"  -H "Content-Type: application/json" -d "$VERSION_JSON" -X PUT https://incentergy.atlassian.net/rest/api/2/issue/$issue
done

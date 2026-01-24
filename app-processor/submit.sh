#!/bin/sh

payload=$(cat assets/submit.json)
id=$(uuidgen)
curl -i -u user:password -H "content-type:application/json" \
  -X POST http://localhost:15672/api/exchanges/%2f/processor-tasks-exchange/publish \
  --data "${payload/__ID__/$id}"
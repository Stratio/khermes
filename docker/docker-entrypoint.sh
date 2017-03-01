#!/bin/bash -xe

if [[ -z ${PARAMS} ]]; then
    echo "No params provided!"
    exit 1
fi

client=$(echo $PARAMS | grep hermes.client=true || true)
echo "Params: ${PARAMS}"

if [[ -z ${client} ]]; then
    java -jar ${PARAMS} /hermes.jar
else
    screen -S client -d -m java -jar ${PARAMS} /hermes.jar
fi

tail -F /var/log/sds/hermes/hermes.log

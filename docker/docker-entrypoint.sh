#!/bin/bash -xe

if [[ -z ${PARAMS} ]]; then
    echo "No params provided!"
    exit 1
fi

client=$(echo $PARAMS | grep khermes.client=true || true)
echo "Params: ${PARAMS}"

if [[ ! -z ${MARATHON_APP_LABEL_DCOS_PACKAGE_NAME} ]]; then
	sleep 10
	ping -c10 ${MARATHON_APP_LABEL_DCOS_PACKAGE_NAME}.marathon.mesos 
	host -t a ${MARATHON_APP_LABEL_DCOS_PACKAGE_NAME}.marathon.mesos
fi

if [[ -z ${client} ]]; then
    java -jar ${PARAMS} /khermes.jar
else
    screen -S client -d -m java -jar ${PARAMS} /khermes.jar
fi

tail -F /var/log/sds/khermes/khermes.log

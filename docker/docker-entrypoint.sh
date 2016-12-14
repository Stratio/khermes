#!/bin/bash -xe

if [[ -z  ${PORT} ]]; then
        export PORT=2551
fi

PARAMS="-Dakka.remote.netty.tcp.port=${PORT}"


if [[ ! -z  ${SEED} ]]; then
  PARAMS="${PARAMS} -Dakka.cluster.seed-nodes.0=akka.tcp://hermes@${SEED}"
fi

echo "Params: ${PARAMS}"
java -jar ${PARAMS} /hermes.jar

tail -F /var/log/sds/hermes/hermes.log
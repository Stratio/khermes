#!/bin/bash -xe

DEFAULT_PORT=2551
if [[ -z  ${PORT} ]]; then
        export PORT=${DEFAULT_PORT}
fi

PARAMS="-Dakka.remote.netty.tcp.port=${PORT}"


if [[ ! -z  ${SEED} ]]; then
  PARAMS="${PARAMS} -Dakka.cluster.seed-nodes.0=akka.tcp://hermes@${SEED}"
fi

echo "Params: ${PARAMS}"
java -jar ${PARAMS} /hermes.jar

tail -F /var/log/sds/hermes/hermes.log
#!/bin/bash -xe

if [[ -z  ${REMOTE_NETTY_TCP_PORT} ]]; then
  echo "Error: env variable REMOTE_NETTY_TCP_PORT is not defined." 
fi

PARAMS="-Dakka.remote.netty.tcp.port=${REMOTE_NETTY_TCP_PORT}"


if [[ ! -z  ${CLUSTER_SEED_NODES} ]]; then
  PARAMS="${PARAMS} -Dakka.cluster.seed-nodes.0=${CLUSTER_SEED_NODES}"
fi

echo "Params: ${PARAMS}"
java -jar ${PARAMS} /hermes.jar

tail -F /var/log/sds/hermes/hermes.log

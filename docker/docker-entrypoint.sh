#!/bin/bash -xe

if [ $SEED = "true" ]; then
    SEED_IP_ADDRESS="$(/sbin/ifconfig eth0 | grep 'inet addr' | cut -d: -f2 | awk '{ print $1}')"
    java -jar -Dkhermes.ws=true -Dakka.remote.netty.tcp.port=$PORT0 -Dakka.remote.netty.tcp.hostname=$SEED_IP_ADDRESS -Dakka.cluster.seed-nodes.0=akka.tcp://khermes@$SEED_IP_ADDRESS:$PORT0 -Dzookeeper.connection=master.mesos:2181 /khermes.jar
else
    java -jar -Dkhermes.client=true -Dkhermes.ws=false -Dakka.remote.netty.tcp.port=$PORT0 -Dakka.cluster.seed-nodes.0=akka.tcp://khermes@$SEED_IP:$SEED_PORT -Dzookeeper.connection=master.mesos:2181 /khermes.jar
fi

tail -F /khermes.log
s
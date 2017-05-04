#!/bin/bash -xe

if [ $SEED = "true" ]; then
    java -jar -Dkhermes.ws=true -Dakka.remote.netty.tcp.port=$SEED_PORT -Dakka.remote.netty.tcp.hostname=localhost -Dakka.cluster.seed-nodes.0=akka.tcp://khermes@localhost:$SEED_PORT -Dzookeeper.connection=localhost:$ZK_PORT /khermes.jar
else
    java -jar -Dkhermes.client=true -Dkhermes.ws=false -Dakka.remote.netty.tcp.port=$NODE_PORT -Dakka.cluster.seed-nodes.0=akka.tcp://khermes@localhost:$SEED_PORT -Dzookeeper.connection=localhost:$ZK_PORT /khermes.jar
fi

tail -F /khermes.log
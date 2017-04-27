#!/bin/bash -xe

if [ $SEED = "true" ]; then
    java -jar -Dkhermes.ws=true -Dakka.remote.netty.tcp.port=2552 -Dakka.remote.netty.tcp.hostname=localhost -Dakka.cluster.seed-nodes.0=akka.tcp://khermes@localhost:2552 -Dzookeeper.connection=localhost:2181 /khermes.jar
else
    java -jar -Dkhermes.client=true -Dkhermes.ws=false -Dakka.remote.netty.tcp.port=2553 -Dakka.cluster.seed-nodes.0=akka.tcp://khermes@localhost:2552 -Dzookeeper.connection=localhost:2181 /khermes.jar
fi

tail -F /khermes.log
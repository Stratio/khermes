#!/bin/bash -xe

java -jar -Dkhermes.ws=true -Dakka.remote.netty.tcp.port=2552 -Dakka.remote.netty.tcp.hostname=localhost -Dakka.cluster.seed-nodes.0=akka.tcp://khermes@localhost:2552 -Dzookeeper.connection=localhost:2181 /khermes.jar

tail -F /khermes.log
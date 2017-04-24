#!/bin/bash -xe

java -jar -Dkhermes.client=false -Dakka.remote.hostname=localhost -Dakka.remote.netty.tcp.port=2553 -Dakka.cluster.seed-nodes.0=akka.tcp://khermes@localhost:2552 /khermes.jar

tail -F /khermes.log


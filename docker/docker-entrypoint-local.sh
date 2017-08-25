#!/bin/bash -xe
#
# © 2017 Stratio Big Data Inc., Sucursal en España.
#
# This software is licensed under the Apache 2.0.
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the terms of the License for more details.
#
# SPDX-License-Identifier:  Apache-2.0.
#


if [ $SEED = "true" ]; then
    java -jar -Dkhermes.ws=true -Dakka.remote.netty.tcp.port=$SEED_PORT -Dakka.remote.netty.tcp.hostname=localhost -Dakka.cluster.seed-nodes.0=akka.tcp://khermes@localhost:$SEED_PORT -Dmetrics.graphite.enabled=$METRICS_ENABLED -Dmetrics.graphite.name=$GRAPHITE_METRICS_NAME -Dzookeeper.connection=localhost:$ZK_PORT /khermes.jar
else
    java -jar -Dkhermes.client=true -Dkhermes.ws=false -Dakka.remote.netty.tcp.port=$NODE_PORT -Dakka.cluster.seed-nodes.0=akka.tcp://khermes@localhost:$SEED_PORT -Dmetrics.graphite.enabled=$METRICS_ENABLED -Dmetrics.graphite.name=$GRAPHITE_METRICS_NAME -Dzookeeper.connection=localhost:$ZK_PORT /khermes.jar
fi

tail -F /khermes.log
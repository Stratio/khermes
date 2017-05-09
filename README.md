# Stratio Khermes. [![Build Status](https://travis-ci.org/Stratio/khermes.svg?branch=master)](https://travis-ci.org/Stratio/khermes)[![Coverage Status](https://coveralls.io/repos/github/Stratio/khermes/badge.svg?branch=master)](https://coveralls.io/github/Stratio/khermes?branch=master)

## Overview.

<img src="http://vignette1.wikia.nocookie.net/en.futurama/images/f/f1/Hermes_2.png/revision/latest?cb=20110710102037" align="right" vspace="20" />

When you have a complex system architecture with a huge variety of services, the first question that arises is:  "What happens when I start to generate tons of events and what is the behaviour of the system when it starts to process them?". For these reasons we are devoloping a high configurable and scalable tool called Khermes that can answer this "a priori simple" question.

> "Khermes is a distributed fake data generator used in distributed environments that produces a high volume of events in a scalable way".

It has the next features:
  - Configurable templates through Play Twirl. Make your own template and send it to one or more nodes.
  - Random event generation through Khermes helper: based in Faker, you can generate generic names, dates, numbers, etc.
  - Scalable generation through an Akka Cluster. Run up all nodes that you need to generate data.
  - A simple but powerful shell to take the control of your cluster: you can start, stop node generation in seconds.

## Installation and Execution.
To generate the khermes jar you should execute:
```sh
$ mvn clean package
```
This command will generate a fat jar with all dependencies in target/khermes-<version>.jar. To run it, you should execute:
```sh
$ java -jar target/khermes-<version>.jar [-Dparameters.to.overwrite]
```
We have create both shell scripts and docker-compose files to make easier for you to start using khermes. For further info on how to get started please go to the [Wiki](https://github.com/Stratio/khermes/wiki).

## Docker.
* Seed + Node
```sh
  docker run -dit --name SEED_NAME -e PARAMS="-Dkhermes.client=true -Dakka.remote.hostname=SEED_NAME.DOMAIN -Dakka.remote.netty.tcp.port=2552 -Dakka.remote.netty.tcp.hostname=SEED_NAME.DOMAIN -Dakka.cluster.seed-nodes.0=akka.tcp://khermes@SEED_NAME.DOMAIN:2552" qa.stratio.com/stratio/khermes:VERSION
```
* Node
```sh
  docker run -dit --name AGENT_NAME -e PARAMS="-Dkhermes.client=false -Dakka.remote.hostname=AGENT_NAME.DOMAIN -Dakka.remote.netty.tcp.port=2553 -Dakka.cluster.seed-nodes.0=akka.tcp://khermes@SEED_NAME.DOMAIN:2552" qa.stratio.com/stratio/khermes:VERSION
```
  
## FAQ.
* **Is Zookeeper needed to run Khermes?.** Yes, at this moment it is mandatory to have an instance of Zookeper in order to run Khermes.
* **Is Apache Kafka needed to run Khermes?.** Yes, at the end all generated event will be persisted in Kafka and right now there are not any other possibility.
* **Is there any throughput limitation?.** No, Khermes is designed to scale out of the box adding infinite nodes in an Akka cluster.

## Roadmap.
* Awesome UI.
* No Zookeeper dependency using Akka Distributed Data.

## Licenses.
Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Tech.
Khermes uses a number of open source projects to work properly:
* [Twirl](https://github.com/playframework/twirl) - Twirl is the Play template engine.
* [Akka](http://akka.io) - Akka is a toolkit and runtime for building highly concurrent, distributed, and resilient message-driven applications on the JVM.
* [Apache Kafka]() - Kafka™ is used for building real-time data pipelines and streaming apps.

And of course it is open source itself with a repository on GitHub [Khermes](https://github.com/stratio/khermes)

## Development.
Want to contribute? Great!
**Khermes is open source and we need you to keep growing.**

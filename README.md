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

## Architecture.
The main idea behind Khermes is to run nodes that produce messages. These nodes should be increased or decreased depending on the needs of the user. For this reason we thought it could be a good idea to use an Akka cluster. An architecture can be summarized in these points:
- Each Akka cluster node  can receive messages to perform operations such as start, stop, etc. data generation. To start a node it needs three basic things:
    * A Khermes configuration. This configuration will set, for example, where the templates will compile, i18n of the data, etc.
    * A Kafka configuration. This configuration will set Kafka parameters. You can see the official Kafka documentation to get more specific information.
    * A Twirl template. A template that will define how to generat a CSV, JSON or every structure that you need.
    * All configurations can be reused thanks to persisting all of them in Zookeeper. For this reason it is mandatory to have a running instance of zookeeper in our system.
    
<img src="doc/khermes-architecture.png?raw=true">

## Installation and Execution.
To generate the khermes jar you should execute:
```sh
$ mvn clean package
```
This command will generate a fat jar with all dependencies in target/khermes-<version>.jar. To run it, you should execute:
```sh
$ java -jar target/khermes-<version>.jar [-Dparameters.to.overwrite]
```
We have create both shell scripts and docker-compose files to make easier for you to start using khermes. For further info on how to get start do please go to the Wiki.

## Random Helper.
Based on [Faker](https://github.com/stympy/faker) we are developing a random generator. At this moment we have the next features:
* Name generation:
```
  fullname() → Paul Brown
  middleName() → George Michael
  firstName() → Steven
  lastName() → Robinson
```
* Number generation:
```
  number(2) → 23
  number(2,Positive) → 23
  decimal(2) → 23.45
  decimal(2,Negative) → -45.89
  decimal(2,4) → 45.7568
  decimal(3,2,Positive) → 354.89
  numberInRange(1,9) → 2
  decimalInRange(1,9) → 2.6034840849740117
```
* Geolocation generation:
```
  geolocation() → (40.493556, -3.566764, Madrid)
  geolocationWithoutCity() → (28.452717, -13.863761)
  city() → Tenerife
  country() → ES
```
* Timestamp generation:
```
  dateTime("1970-1-12" ,"2017-1-1") → 2005-03-01T20:34:30.000+01:00
  time() → 15:30:00.000+01:00
```
* Music generation:
```
  playedSong() → {"song": "Shape of You", "artist": "Ed Sheeran", "album": "Shape of You", "genre": "Pop"}
```

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

## Contributors.
* [Alberto Rodriguez](https://github.com/albertostratio)
* [Alicia Doblas](https://github.com/adoblas)
* [Alvaro Nistal](https://github.com/anistal)
* [Antonio Alfonso](https://github.com/antonioalf)
* [Emilio Ambrosio](https://github.com/eambrosio)
* [Enrique Ruiz](https://github.com/eruizgar)
* [Juan Pedro Gilaberte](https://github.com/jpgilaberte-stratio)

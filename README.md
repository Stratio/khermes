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
Right now the only way  to execute Khermes is to generate a jar file. To make it, you should execute:
```sh
$ mvn clean package
```
This command will generate a fat jar with all dependencies in target/khermes-<version>.jar. To run it, you should execute:
```sh
$ java -jar target/khermes-<version>.jar [-Dparameters.to.overwrite]
```

## Getting started.
The first thing that you should do is to specify a configuration. Khermes configuration is done thanks to Typesafe config.
You can see all options that you can configure in the next section:
```
khermes {
  templates-path = "/opt/khermes/templates"
  client = false
}
akka {
  loglevel = "error"
  actor {
    provider = "akka.cluster.ClusterActorRefProvider"
    debug {
      receive = on
      lifecycle = on
    }
  }
  remote {
    log-remote-lifecycle-events = off
    netty.tcp {
      hostname = localhost
      port = 2553
    }
  }
  cluster {
    roles = [backend]
    seed-nodes = [${?VALUE}]
    auto-down-unreachable-after = 10s
  }
}
```

As you can see, you can set configurations for Khermes or Akka cluster. We can not see how an Akka cluster is configured because there is a lot of information in its official documentation.
For Khermes, you can set the next parameters:
- templates-path: when you send a template to one node, it sends a Twirl template. The template is translated to a Scala native code that should be compiled when it runs the first time. For this reason you need to set a temporal path where all .scala and .class files are.
- client: if you need to start a node and you also need a shell you can put the value of this parameter to true. When you run you will see a Khermes shell, something like:

```
╦╔═┬ ┬┌─┐┬─┐┌┬┐┌─┐┌─┐
╠╩╗├─┤├┤ ├┬┘│││├┤ └─┐
╩ ╩┴ ┴└─┘┴└─┴ ┴└─┘└─┘ Powered by Stratio (www.stratio.com)

> System Name   : khermes
> Start time    : Fri Mar 10 12:31:52 CET 2017
> Number of CPUs: 8
> Total memory  : 251658240
> Free memory   : 225155304
    
khermes>
```

If you execute help in your command line you can see the list of available commands in our shell:

```
khermes> help
Khermes client provides the next commands to manage your Khermes cluster:
  Usage: COMMAND [args...]

  Commands:
     start [command options] : Starts event generation in N nodes.
       -kh, --khermes    : Khermes configuration
       -ka, --kafka      : Kafka configuration
       -t, --template   : Template to generate data
       -a, --avro       : Avro configuration
       -i, --ids        : Node id where start khermes
     stop [command options] : Stop event generation in N nodes.
       -i, --ids        : Node id where start khermes
     ls                    : List the nodes with their current status
     save [command options] : Save your configuration in zookeeper
       -kh, --khermes    : Khermes configuration
       -ka, --kafka      : Kafka configuration
       -t, --template   : Template to generate data
       -a, --avro       : Avro configuration
     show [command options] : Show your configuration
       -kh, --khermes    : Khermes configuration
       -ka, --kafka      : Kafka configuration
       -t, --template   : Template to generate data
       -a, --avro       : Avro configuration
     clear                 : Clean the screen.
     help                  : Print this usage.
     exit | quit | bye     : Exit of Khermes Cli.   
```

Steps to run a policy:
* Step 1) Save a Khermes configuration that will be persisted in Zookeeper. This is needed because otherwise, the next time that the user executes Khermes it will lost this configuration:
```
  khermes> save --khermes nameKhermesConfig
  Press Control + D to finish
  khermes {
     templates-path = "/tmp/khermes/templates"
     topic = "test"
     template-name = "testTemplate"
     i18n = "ES"
     timeout-rules {
       number-of-events: 1000
       duration: 2 seconds
     }
     stop-rules {
       number-of-events: 5000
     }
  }
```
  As you can see you should configure the following variables:
  - templates-path: in every node that you send this configuration, it will need to generate and compile a template.
  - topic: it indicates a Kafka topic where messages will be produced.
  - template-name: it indicates a prefix for the generated .scala and .class files. It is possible that in the future this variable dissapears.
  - i18n: internationalization of Khermes helper. It generates, for example names in Spanish. Right now only ES and EN are available.
  - timeout-rules: it is optional. When it is set it generates 1000 events and wait 2 seconds to generate the next 1000 events.
  - stop-rules: it is optional. When it is set it generates 5000 events and the node stops data generation. Besides the node will be free to accept more requests.
    
* Step 2) Save a Kafka configuration that will also be persisted in Zookeeper.
```
  khermes> save --kafka nameKafkaConfig
  Press Control + D to finish
  kafka {
     bootstrap.servers = "localhost:9092"
     acks = "-1"
     key.serializer = "org.apache.kafka.common.serialization.StringSerializer"
     value.serializer = "org.apache.kafka.common.serialization.StringSerializer"
  }
```
  
* Step 3) Save a Twirl template that will also be persisted in Zookeeper.
```
  khermes> save --template nameTemplate
  Press Control + D to finish
  @import com.stratio.hermes.utils.Hermes
  @(khermes: Khermes)
  {
    "name" : "@(Khermes.Name.firstName)"
  }
```
* Step four) Once you have saved these configurations in ZK, you can start a generation in the nodes that you need:
```
  khermes> ls
  Node Id                                Status
  845441eccb0d4363b494a39d56a82727 | false
  khermes> start --kafka nameKafkaConfig --template nameTemplate --khermes nameKhermesConfig --ids 845441eccb0d4363b494a39d56a82727
  khermes> ls
  Node Id                                Status
  845441eccb0d4363b494a39d56a82727 | true
```
  At this moment the node with id 845441ec-cb0d-4363-b494-a39d56a82727 is producing messages to Kafka following the saved template. You can check it using Kafka console consumer.

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

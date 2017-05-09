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
  
## Full Documentation

See the [Wiki](https://github.com/Stratio/khermes/wiki/) for full documentation, examples, operational details and other information.
  
## Build
To build the project, run the tests and generate the khermes jar artifact you should execute:
```sh
$ mvn clean package
```

We have create both shell scripts and docker-compose files to make easier for you to start using khermes. For further info on how to get started please go to the [Wiki](https://github.com/Stratio/khermes/wiki/Getting-started).

## Licenses.
Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Bugs and Feedback

For bugs, questions and discussions please use the [GitHub Issues](https://github.com/Stratio/khermes/issues).

## Development.

Want to contribute? Great!
**Khermes is open source and we need you to keep growing.**
If you want to contribute do not hesitate to create a Pull Request with your changes!

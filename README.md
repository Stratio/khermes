# Stratio Khermes. [![Build Status](https://travis-ci.org/Stratio/khermes.svg?branch=master)](https://travis-ci.org/Stratio/khermes)[![Coverage Status](https://coveralls.io/repos/github/Stratio/khermes/badge.svg?branch=master)](https://coveralls.io/github/Stratio/khermes?branch=master)

## Overview.

<img src="http://vignette1.wikia.nocookie.net/en.futurama/images/f/f1/Hermes_2.png/revision/latest?cb=20110710102037" align="right" vspace="20" />

When you have a complex system architecture with a huge variety of services, the first question that arises is:  "What happens when I start to generate tons of events and what is the behaviour of the system when it starts to process them?". For these reasons we are devoloping a high configurable and scalable tool called Khermes that can answer this "a priori simple" question.

> "Khermes is a distributed fake data generator used in distributed environments that produces a high volume of events in a scalable way".
  
## Full Documentation

See the [Wiki](https://github.com/Stratio/khermes/wiki/) for full documentation, examples, operational details and other information.
  
## Build
To build the project, run the tests and generate the khermes jar artifact you should execute:
```sh
$ mvn clean package
```

We have create both shell scripts and docker-compose files to make easier for you to start using khermes. For further info on how to get started please go to the [Getting Started](https://github.com/Stratio/khermes/wiki/Getting-started) section of our Wiki.

## Quickstart

To start fast generating your files data, check the scripts/quickstart directory. There is a khermes.sh script which starts the complete app along with all its dependencies. Follow the intrucions below:

- Be sure that you have **mvn** command in your PATH and **docker-compose**.
- Execute khermes.sh script. It checks if there is the **khermes artifact** inside the **target** directory, if not it tries to build it with maven. If you execute the script with the **package** option:  **khermes.sh package**, it forces the **jar** generation. 
- If you don´t set the **LOCALPATH** environment variable, the file will be created in your current directory. If you want to use another location set it: **LOCALPATH=/tmp khermes.sh**. 
- The scrips starts **khermes** and **zookeeper**. Go to http://localhost:8080/console in your browser a start the configuration.
- Create the file configuration with the command create **file-config**:
    ```    
    file {
       path = "/tmp/file.csv"
    }
    ```
    It is neccessary to put the file inside the **/tmp** directory. The docker compose mounts this container´s directory.
- Crate the template with the command **create twirl-template**:
    ```
    @import com.stratio.khermes.helpers.faker.generators._
    @import scala.util.Random
    @import com.stratio.khermes.helpers.faker.Faker
    @import com.stratio.khermes.helpers.faker.generators.Positive
    @import org.joda.time.DateTime
    
    @(faker: Faker)
    @defining(faker, List(CategoryFormat("MASTERCARD", "0.5"),CategoryFormat("VISA", "0.5")),List(CategoryFormat("MOVISTAR", "0.5"),CategoryFormat("IUSACELL", "0.5"))){ case (f,s,s2) =>
    @f.Name.fullName,@f.Categoric.runNext(s),@f.Number.numberInRange(10000,50000),@f.Geo.geolocation.city,@f.Number.numberInRange(1000,10000),@f.Categoric.runNext(s2),@f.Number.numberInRange(1,5000),@f.Datetime.datetime(new DateTime("2000-01-01"), new DateTime("2016-01-01"), Option("yyyy-MM-dd")) }
    ```
- Create the generator configuration with the command: **create generator-config**:

    ```
    khermes {
      templates-path = "/tmp/khermes/templates"
      topic = "khermes"
      template-name = "khermestemplate"
      i18n = "EN"
    
      timeout-rules {
        number-of-events: 20
        duration: 5 seconds
      }
    
      stop-rules {
        number-of-events: 10000
      }
    }
    ```
- Check the the actor´s id with the command **ls** and start it. Then you will be asked for the names of the created configurations. Do not include **kafka** and **avro** names, press enter to skip these otherwise **khermes** will try to find them and will crash.
- Events will be created as many as you put in the **number-of-events** property. Enjoy!

## Tricks

If you want to generate one CSV file, with its headers follow this:

1. Create a generator config which indicates Khermes to emit one single event:

    ```
    khermes {
      templates-path = "/tmp/khermes/templates"
      topic = "khermes"
      template-name = "khermestemplate"
      i18n = "EN"
      stop-rules {
        number-of-events: 1
      }
    }
    ```

2. Create the following Twirl template:

        
    ```
    @import com.stratio.khermes.helpers.faker.Faker
    @(faker: Faker)Element1,Element2
    @for(p <- 0 to 100000) {@(faker.Gaussian.runNext(5.0, 1.5)),@(faker.Gaussian.runNext(5.0, 1.5))
    }
    ```
    
This will force khermes to generate the file in one step.

If you want to create a JSON Array instead of JSObject lines the following template works:

    
    @(faker: Faker)@for(p <- 0 to 1) {{"Element1":@(faker.Gaussian.runNext(5.0, 1.5)),"Element2":@(faker.Gaussian.runNext(5.0, 1.5))}@(if(p < 1) "," else "")}]
    
 
## Licenses.
Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Bugs and Feedback

For bugs, questions and discussions please use the [GitHub Issues](https://github.com/Stratio/khermes/issues).

## Development.

Want to contribute? Great!
**Khermes is open source and we need you to keep growing.**
If you want to contribute do not hesitate to create a Pull Request with your changes!
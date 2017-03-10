# Stratio Hermes

[![Coverage Status](https://coveralls.io/repos/github/Stratio/Hermes/badge.svg?branch=master)](https://coveralls.io/github/Stratio/Hermes?branch=master)

## Description
A distributed fake data generator based in Akka.

##Contents

- Name(#Name)

- Numbers(#Numbers)

- Geo(#Geo)

- Timestamp(#Timestamp)

- Music(#Music)

- Email(#Email)

###Name

-------

fullname() → Paul Brown

middleName() → George Michael

firstName() → Steven

lastName() → Robinson

###Numbers

----------
number(2) → 23

number(2,Positive) → 23

decimal(2) → 23.45

decimal(2,Negative) → -45.89

decimal(2,4) → 45.7568

decimal(3,2,Positive) → 354.89

numberInRange(1,9) → 2

decimalInRange(1,9) → 2.6034840849740117

###Geo
______

geolocation() → (40.493556, -3.566764, Madrid)

geolocationWithoutCity() → (28.452717, -13.863761)

city() → Tenerife

country() → ES

###Timestamp
____________

dateTime("1970-1-12" ,"2017-1-1") → 2005-03-01T20:34:30.000+01:00

time() → 15:30:00.000+01:00

###Music
________

playedSong() → ("Despacito (Featuring Daddy Yankee)", "Luis Fonsi, Daddy Yankee", "Despacito (Featuring Daddy Yankee)","Latin")

###Email
________

address("John Doe") -> jdoe@aol.com


###Cluster


## Docker usage

### Run 

* Seed + client

docker run -dit --name SEED_NAME -e PARAMS="-Dhermes.client=true -Dakka.remote.hostname=SEED_NAME.DOMAIN -Dakka.remote.netty.tcp.port=2552 -Dakka.remote.netty.tcp.hostname=SEED_NAME.DOMAIN -Dakka.cluster.seed-nodes.0=akka.tcp://hermes@SEED_NAME.DOMAIN:2552" qa.stratio.com/stratio/hermes:VERSION

* Agent

docker run -dit --name AGENT_NAME -e PARAMS="-Dhermes.client=false -Dakka.remote.hostname=AGENT_NAME.DOMAIN -Dakka.remote.netty.tcp.port=2553 -Dakka.cluster.seed-nodes.0=akka.tcp://hermes@SEED_NAME.DOMAIN:2552" qa.stratio.com/stratio/hermes:VERSION

### Connect to client console

docker exec -it NODE_NAME script -q -c "export TERM=xterm && screen -r client"

IMPORTANT: To disconnect from the client interactive console use Ctrl-A + D. If you use Ctrl-C, you will kill the process and khermes will stop running.  

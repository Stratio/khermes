# Stratio Hermes

[![Coverage Status](https://coveralls.io/repos/github/Stratio/Hermes/badge.svg?branch=master)](https://coveralls.io/github/Stratio/Hermes?branch=master)

## Description
A distributed fake data generator based in Akka.

##Contents

- Name(#Name)

- Numbers(#Numbers)

###Name

-------

fullname() #=> Paul Brown

middleName() #=> George Michael

firstName() #=> Steven

lastName() #=> Robinson

###Numbers

----------
number(2) #=> 23

number(2,Positive) #=> 23

decimal(2) #=> 23.45

decimal(2,Negative) #=> -45.89

decimal(2,4) #=> 45.7568

decimal(3,2,Positive) #=> 354.89

###Geo

geolocation() #=> (27.931886,-15.386586)

###Timestamp

dateTime("1970-1-12" ,"2017-1-1") -> 2005-03-01T20:34:30.000+01:00

###Cluster


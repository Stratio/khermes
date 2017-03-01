FROM stratio/ubuntu-base:16.04

MAINTAINER stratio

ARG VERSION

RUN apt-get update && apt-get install -y screen

COPY target/hermes-${VERSION}-allinone.jar /hermes.jar
COPY docker/docker-entrypoint.sh /
COPY src/main/resources/application.conf /

ENTRYPOINT ["/docker-entrypoint.sh"]

FROM qa.stratio.com/stratio/ubuntu-base:16.04

MAINTAINER stratio

ARG VERSION

COPY target/khermes-${VERSION}-allinone.jar /khermes.jar
COPY docker/docker-entrypoint-dcos.sh /
COPY docker/docker-entrypoint-local.sh /
COPY src/main/resources/application.conf /
RUN touch /khermes.log

EXPOSE 8080

ENTRYPOINT ["/docker-entrypoint-local.sh"]
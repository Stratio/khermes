FROM qa.stratio.com/stratio/ubuntu-base-ssh:16.04
MAINTAINER stratio
COPY target/hermes-*-allinone.jar /hermes.jar
COPY docker/docker-entrypoint.sh /
COPY src/main/resources/application.conf /
ENTRYPOINT ["/docker-entrypoint.sh"]

FROM openjdk:8-jre-alpine

COPY khermes-0.2.0-SNAPSHOT-allinone.jar /khermes/khermes-0.2.0-SNAPSHOT-allinone.jar
COPY seed.sh /khermes/seed.sh

EXPOSE 8080

ENV ZOOKEEPER_HOST_PORT "zookeeper:2181"

ENTRYPOINT ["/khermes/seed.sh"]

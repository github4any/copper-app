FROM openjdk:11-jre-slim

RUN mkdir -p /var/app
COPY build/libs /var/app
WORKDIR /var/app/

CMD java \
  -XX:+UnlockDiagnosticVMOptions \
  -XX:+PrintFlagsFinal \
  -XX:InitialRAMPercentage=5 \
  -XX:MinRAMPercentage=5 \
  -XX:MaxRAMPercentage=10 \
  -XX:+UseStringDeduplication \
  -Dlogback.configurationFile=/var/app/cfg/logback.xml \
  -Djavax.net.debug=all \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false \
  -Dcom.sun.management.jmxremote.port=5000 \
  -jar copper-app-0.0.1.jar

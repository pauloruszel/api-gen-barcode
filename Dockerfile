FROM ghcr.io/graalvm/jdk-community:21 AS build-native

ENV DOCKER_CONTENT_TRUST 1
ENV APP_RUNTIME_NAME api-gen-barcode
ENV RUNTIME_BUILD /opt/build/

WORKDIR /development
COPY ./target/api-gen-barcode-0.0.1-SNAPSHOT.jar .

EXPOSE 8080

ENTRYPOINT java -XX:+TieredCompilation -XX:+OptimizeStringConcat -XX:+UseStringDeduplication -XX:+UseG1GC -jar api-gen-barcode-0.0.1-SNAPSHOT.jar
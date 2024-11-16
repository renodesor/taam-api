FROM docker.io/library/eclipse-temurin:17
ARG build_env
ENV spring.profiles.active=$build_env
WORKDIR /app
COPY target/taam-api-0.0.1-SNAPSHOT.jar /app/taam-api.jar

ENTRYPOINT ["java", "-jar", "association.jar"]
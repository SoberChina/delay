FROM openjdk:8-jdk-alpine
ARG TARGET
ENV ACTIVE="dev"
COPY ${TARGET} /delay.jar
ENTRYPOINT ["java", "-jar", "/delay.jar"]
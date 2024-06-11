FROM maven:3.8.6-openjdk-18-slim

RUN mkdir /app-repo
ADD . /app-repo/
WORKDIR /app-repo

RUN mvn clean
RUN mvn package spring-boot:repackage

EXPOSE 3003

ENV SPRING_PROFILES_ACTIVE=gessi

ENTRYPOINT ["java", "-jar", "./target/repo-0.0.1-SNAPSHOT.jar"]

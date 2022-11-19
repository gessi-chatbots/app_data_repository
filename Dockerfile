FROM maven:3.8.6-openjdk-18-slim

COPY * /service/

WORKDIR /service
RUN mvn clean package

CMD ["java", "-jar", "repo-0.0.1-SNAPSHOT.jar"]

FROM maven:3.8.6-openjdk-18-slim

# Create a directory for the application repository
RUN mkdir /app-repo

# Copy the project files into the container
ADD . /app-repo/
WORKDIR /app-repo

# Clean and package the application
RUN mvn clean
RUN mvn package spring-boot:repackage

# Expose the application port
EXPOSE 3003

# Environment variables (can be overridden when running the container)
# SPRING_PROFILES_ACTIVE: Specifies the active Spring Boot profile (e.g., gessi)
# DB_USERNAME: GraphDB Database username (set via environment variable for security)
# DB_PASSWORD: GraphDB Database password (set via environment variable for security)
# REPO_NAME: Name of the GraphDB repository
ENV SPRING_PROFILES_ACTIVE=docker
ENV DB_USERNAME=${DB_USERNAME}
ENV DB_PASSWORD=${DB_PASSWORD}
ENV REPO_NAME=${REPO_NAME}

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "./target/repo-0.0.1-SNAPSHOT.jar"]

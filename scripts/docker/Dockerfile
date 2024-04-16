# Dockerfile to run a Spring Boot Kotlin application
FROM eclipse-temurin:21-jre-alpine

# Add Maintainer Info
LABEL maintainer="stefan.roeck@gmail.com"

## create a nonroot user and group
RUN addgroup -S spring && adduser -S spring -G spring

## copy the spring jar
COPY target/tracks-backend-*.jar /opt/tracks-backend.jar

# Add a volume pointing to /tmp
VOLUME /tmp

# Switch to the nonroot user
USER spring:spring

## set the working directory
WORKDIR /opt

# Run the jar file
ENTRYPOINT ["java","-Dspring.data.mongodb.uri=${MONGO_URI}","-jar","tracks-backend.jar"]
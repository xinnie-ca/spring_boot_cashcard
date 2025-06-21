# Use a lightweight JDK base image
FROM eclipse-temurin:21-jdk-alpine

# Set working directory inside the container
WORKDIR /app

# Copy the built JAR from the target directory into the container
COPY target/cashcard-*.jar app.jar

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Command to run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
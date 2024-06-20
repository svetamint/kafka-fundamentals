FROM maven:3.8.1-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/kafka-consumer-0.0.1-SNAPSHOT.jar .
CMD ["java", "-jar", "kafka-consumer-0.0.1-SNAPSHOT.jar"]
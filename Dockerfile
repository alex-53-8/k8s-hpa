FROM maven:3.9.12-eclipse-temurin-25 AS build

WORKDIR /app
COPY app-processor/pom.xml pom.xml
COPY app-processor/src src
RUN mvn clean package

#
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/target/data-processor-1.0.0.jar data-processor-1.0.0.jar
EXPOSE 9090
CMD ["java", "-jar", "data-processor-1.0.0.jar"]
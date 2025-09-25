# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Maven image задаёт MAVEN_CONFIG=/root/.m2, что ломает mvnw; обнуляем.
ENV MAVEN_CONFIG=""

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B package -DskipTests
RUN find target -type f -name '*.jar' ! -name '*-plain.jar' -exec cp {} app.jar \;

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]

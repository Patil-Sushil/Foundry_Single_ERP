# ---------- BUILD STAGE ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# copy pom and source
COPY pom.xml .
COPY src ./src

# build jar (skip tests for faster deploy)
RUN mvn clean package -DskipTests


# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Railway uses dynamic PORT (still safe to expose 8080)
EXPOSE 8080

# run app
ENTRYPOINT ["java","-jar","app.jar"]
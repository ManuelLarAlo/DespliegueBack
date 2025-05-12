# Etapa de construcción
FROM maven:3.8.5-openjdk-21 AS build
WORKDIR /app
COPY aplicacion/pom.xml .
COPY aplicacion/src ./src
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# Imagen base con JDK 17 (puedes usar otra si lo necesitas)
FROM eclipse-temurin:17-jdk-alpine

# Copia el .jar generado al contenedor
COPY target/*.jar app.jar

# Expone el puerto 8080 (el default de Spring Boot)
EXPOSE 8080

# Comando para arrancar la aplicación
ENTRYPOINT ["java", "-jar", "/app.jar"]

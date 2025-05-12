# Usa una imagen base de Java (por ejemplo, OpenJDK 17)
FROM eclipse-temurin:21-jdk-alpine

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo .jar desde el directorio target de la aplicación
COPY target/*.jar app.jar

# Expón el puerto 8080 para que la aplicación sea accesible
EXPOSE 8080

# Comando para ejecutar el .jar generado por Spring Boot
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

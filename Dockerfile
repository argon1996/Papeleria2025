FROM eclipse-temurin:11-jdk


# Actualiza y instala curl (como ya lo tenías)
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Establece el directorio de trabajo
WORKDIR /app

# Copia el archivo JAR al contenedor
COPY build/libs/Papeleria2025-0.2.0.jar app.jar

# Copia los archivos estáticos al contenedor (esto es importante)
COPY src/main/resources/static /app/static

# Expone el puerto 8080
EXPOSE 8080

# Ejecuta la aplicación Spring Boot
CMD ["java", "-jar", "/app/app.jar"]

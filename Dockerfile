# ============================================
# Stage 1: Build con Maven + OpenJDK 17
# ============================================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar POM primero para cachear dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================
# Stage 2: Runtime ligero con JRE Alpine
# ============================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crear usuario no-root para seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar JAR compilado
COPY --from=build /app/target/*.jar app.jar

# Cambiar a usuario no-root
USER appuser

# Puerto interno de Spring Boot
EXPOSE 8080

# Variables JVM súper optimizadas para contenedores de baja memoria (ej. Railway 200MB)
ENV JAVA_OPTS="-Xmx100m -Xms50m -XX:MaxMetaspaceSize=80m -Xss256k -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

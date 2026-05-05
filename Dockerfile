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
# Stage 2: Runtime ligero con OpenJ9 (Ubuntu Jammy)
# ============================================
FROM ibm-semeru-runtimes:open-17-jre-jammy

WORKDIR /app

# Crear usuario no-root para seguridad
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Copiar JAR compilado
COPY --from=build /app/target/*.jar app.jar

# Cambiar a usuario no-root
USER appuser

# Puerto interno de Spring Boot
EXPOSE 8080

# Variables JVM optimizadas para contenedores con OpenJ9
# -Xtune:virtualized optimiza el uso de CPU y RAM para entornos de nube
ENV JAVA_OPTS="-Xmx150m -Xms50m -Xtune:virtualized -Xscmx50m -Xshareclasses:name=springcache,nonfatal -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

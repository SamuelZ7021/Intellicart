# Refactor de services/order-service/Dockerfile
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Install gcompat to allow execution of glibc binaries like protoc
RUN apk add --no-cache gcompat libstdc++

# Copy the protobuf definitions
COPY proto proto

# Copy the order-service code
COPY services/order-service/pom.xml services/order-service/
COPY services/order-service/src services/order-service/src

WORKDIR /app/services/order-service
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Descargamos el agente de OTel
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar .
COPY --from=build /app/services/order-service/target/*.jar app.jar

# Configuramos el agente mediante variables de entorno en el ENTRYPOINT
ENTRYPOINT ["java", "-javaagent:opentelemetry-javaagent.jar", "-jar", "app.jar"]
# === Build stage ===
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

# cache deps
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# build artefact
COPY src ./src
RUN mvn -q -DskipTests package

# === Runtime stage ===
FROM eclipse-temurin:21-jre
# user non-root
RUN addgroup --system spring && adduser --system --ingroup spring app
WORKDIR /app

# copiem un singur JAR (spring-boot repackage)
COPY --from=build /app/target/*.jar app.jar

# opțiuni sigure pt containere
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"
EXPOSE 8080
USER app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

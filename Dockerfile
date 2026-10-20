# Build stage (runs tests and creates the app.jar)

FROM eclipse-temurin:21-jdk AS build

WORKDIR /mortgage-matcher

COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./
# Give gradlew permission
RUN chmod +x gradlew


# Copy over source files
COPY src/ src/ 
RUN ./gradlew --no-daemon clean test bootJar 

# Run stage (using JRE)
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

COPY --from=build /mortgage-matcher/build/libs/app.jar app.jar

# for security 
USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

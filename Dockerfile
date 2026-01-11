FROM gradle:8.5-jdk21 AS build
WORKDIR /app
COPY . .

RUN chmod +x gradlew

RUN ./gradlew clean bootJar -x test

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-Xmx300m", "-jar", "app.jar"]
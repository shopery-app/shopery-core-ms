FROM eclipse-temurin:27-jdk-resolute AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew clean bootJar -x test --no-daemon

FROM eclipse-temurin:27-jre-resolute
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx300m", "-jar", "app.jar"]
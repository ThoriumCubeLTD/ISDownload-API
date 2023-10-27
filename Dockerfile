FROM openjdk:17-jdk-slim
VOLUME
EXPOSE 8080
ARG JAR_FILE=build/libs/IS-API-1.0-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY build/libs/*.jar academy.jar

ENTRYPOINT ["java", "-jar", "academy.jar"]
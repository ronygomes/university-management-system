FROM amazoncorretto:25 AS build
WORKDIR /app

# The Corretto base (Amazon Linux 2023) lacks xargs, which the Gradle wrapper needs.
RUN yum install -y findutils && yum clean all

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies -q || true

COPY src ./src
RUN ./gradlew --no-daemon clean bootJar

FROM amazoncorretto:25
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8100
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY user-service-1.0.jar UserService.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "UserService.jar"]
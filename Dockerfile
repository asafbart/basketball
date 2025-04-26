FROM openjdk:11-jdk-slim as build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/nba-stats-1.0.0.jar app.jar

# Add wait script to allow the database to start first
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.9.0/wait /wait
RUN chmod +x /wait

EXPOSE 8080
CMD /wait && java -jar app.jar
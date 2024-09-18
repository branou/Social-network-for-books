#build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

#Runtime stage
FROM amazoncorretto:17
ARG APP_VERSION=1.0.0

WORKDIR /app
COPY --from=build /build/target/ReseauSocialDeLivres-Backend-*.jar /app/
EXPOSE 8080
ENV DB_URL=jdbc:postgresql://postgres-container:5433/book_network
ENV JAR_VERSION=${APP_VERSION}
CMD java -jar -Dspring.datasource.url=${DB_URL} ReseauSocialDeLivres-Backend-${JAR_VERSION}.jar
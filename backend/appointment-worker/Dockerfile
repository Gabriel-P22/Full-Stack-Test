FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY appointment-common/pom.xml appointment-common/pom.xml
COPY appointment-service/pom.xml appointment-service/pom.xml
COPY appointment-worker/pom.xml appointment-worker/pom.xml
RUN ./mvnw -B -pl appointment-worker -am dependency:go-offline
COPY appointment-common/src appointment-common/src
COPY appointment-worker/src appointment-worker/src
RUN ./mvnw -B -pl appointment-worker -am clean package -DskipTests

FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app
COPY --from=build /app/appointment-worker/target/appointment-worker-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

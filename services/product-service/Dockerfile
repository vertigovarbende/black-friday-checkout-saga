FROM eclipse-temurin:25.0.5_8-jre-noble

RUN groupadd --system --gid 10001 orderflow \
    && useradd --system --uid 10001 --gid orderflow --no-create-home orderflow

WORKDIR /app
COPY target/product-service-*.jar app.jar

USER 10001:10001
EXPOSE 8082

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]

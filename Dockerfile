FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copiar gradle e source
COPY . .

# Executar o build do backend
RUN chmod +x gradlew && \
    ./gradlew backend:build -x test --no-daemon

# Entrypoint
EXPOSE 8080
ENV PORT=8080

CMD ["java", "-jar", "backend/build/libs/backend.jar"]


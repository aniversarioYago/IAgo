# Build stage
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy source and gradle
COPY . .

# Build backend JAR
RUN chmod +x gradlew && \
    ./gradlew backend:build -x test --no-daemon && \
    mkdir -p /app/backend-dist && \
    cp backend/build/libs/backend.jar /app/backend-dist/

# Runtime stage - minimal image
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy only the built JAR from builder
COPY --from=builder /app/backend-dist/backend.jar .

EXPOSE 8080

ENV PORT=8080

CMD ["java", "-jar", "backend.jar"]



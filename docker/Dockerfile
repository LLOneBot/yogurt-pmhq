FROM eclipse-temurin:25-jdk AS builder

WORKDIR /workspace

ARG YOGURT_GIT_HASH=unknown

RUN apt-get update \
    && apt-get install -y --no-install-recommends build-essential clang curl unzip xz-utils \
    && rm -rf /var/lib/apt/lists/*

COPY . .

ENV YOGURT_GIT_HASH=${YOGURT_GIT_HASH}

RUN ./gradlew :yogurt:linkReleaseExecutableLinuxX64 --no-daemon \
    && cp /workspace/yogurt/build/bin/linuxX64/releaseExecutable/yogurt.kexe /tmp/yogurt

FROM debian:bookworm-slim

RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates jq libcurl4 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /tmp/yogurt /app/yogurt
COPY docker/startup.sh /app/startup.sh

RUN chmod +x /app/startup.sh /app/yogurt

ENV YOGURT_DATA_DIR=/app/data

ENTRYPOINT ["/app/startup.sh"]

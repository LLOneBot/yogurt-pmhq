FROM debian:bookworm-slim

RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates jq libcurl4 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY yogurt/build/bin/linuxX64/releaseExecutable/yogurt.kexe /app/yogurt
COPY docker/startup.sh /app/startup.sh

RUN chmod +x /app/startup.sh /app/yogurt

ENV YOGURT_DATA_DIR=/app/data

ENTRYPOINT ["/app/startup.sh"]

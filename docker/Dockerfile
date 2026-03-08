FROM debian:bookworm-slim

ARG TARGETARCH

RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates jq libcurl4 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY docker/bin /tmp/docker-bin
COPY docker/startup.sh /app/startup.sh

RUN cp "/tmp/docker-bin/linux-${TARGETARCH}/yogurt" /app/yogurt \
    && rm -rf /tmp/docker-bin \
    && chmod +x /app/startup.sh /app/yogurt

ENV YOGURT_DATA_DIR=/app/data

ENTRYPOINT ["/app/startup.sh"]

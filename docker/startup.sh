#!/bin/sh
set -eu

DATA_DIR="${YOGURT_DATA_DIR:-/app/data}"
PMHQ_HOST="${PMHQ_HOST:-pmhq}"
PMHQ_PORT="${PMHQ_PORT:-13000}"
YOGURT_HOST="${YOGURT_HOST:-0.0.0.0}"
YOGURT_PORT="${YOGURT_PORT:-3000}"
YOGURT_ACCESS_TOKEN="${YOGURT_ACCESS_TOKEN:-}"
HTTP_CORS_ORIGINS="${HTTP_CORS_ORIGINS:-}"
WEBHOOK_URLS="${WEBHOOK_URLS:-}"
WEBHOOK_ACCESS_TOKEN="${WEBHOOK_ACCESS_TOKEN:-}"
QUICK_LOGIN_UIN="${QUICK_LOGIN_UIN:-}"
PRELOAD_CONTACTS="${PRELOAD_CONTACTS:-false}"
REPORT_SELF_MESSAGE="${REPORT_SELF_MESSAGE:-true}"
TRANSFORM_INCOMING_MFACE_TO_IMAGE="${TRANSFORM_INCOMING_MFACE_TO_IMAGE:-false}"
SKIP_SECURITY_CHECK="${SKIP_SECURITY_CHECK:-true}"
ANSI_LEVEL="${ANSI_LEVEL:-ANSI256}"
CORE_LOG_LEVEL="${CORE_LOG_LEVEL:-DEBUG}"

mkdir -p "$DATA_DIR" "$DATA_DIR/scripts"

if [ -n "$QUICK_LOGIN_UIN" ]; then
  QUICK_LOGIN_UIN_JSON="$QUICK_LOGIN_UIN"
else
  QUICK_LOGIN_UIN_JSON="null"
fi

jq -n \
  --arg pmhqUrl "ws://${PMHQ_HOST}:${PMHQ_PORT}/ws" \
  --argjson quickLoginUin "$QUICK_LOGIN_UIN_JSON" \
  --argjson preloadContacts "$PRELOAD_CONTACTS" \
  --argjson reportSelfMessage "$REPORT_SELF_MESSAGE" \
  --argjson transformIncomingMFaceToImage "$TRANSFORM_INCOMING_MFACE_TO_IMAGE" \
  --argjson skipSecurityCheck "$SKIP_SECURITY_CHECK" \
  --arg host "$YOGURT_HOST" \
  --arg accessToken "$YOGURT_ACCESS_TOKEN" \
  --arg corsOrigins "$HTTP_CORS_ORIGINS" \
  --arg webhookUrls "$WEBHOOK_URLS" \
  --arg webhookAccessToken "$WEBHOOK_ACCESS_TOKEN" \
  --arg ansiLevel "$ANSI_LEVEL" \
  --arg coreLogLevel "$CORE_LOG_LEVEL" \
  --argjson port "$YOGURT_PORT" \
  '{
    signApiUrl: "",
    pmhqUrl: $pmhqUrl,
    quickLoginUin: $quickLoginUin,
    protocol: {
      os: "Linux",
      version: "fetched"
    },
    androidCredentials: {
      uin: 0,
      password: ""
    },
    androidUseLegacySign: false,
    reportSelfMessage: $reportSelfMessage,
    preloadContacts: $preloadContacts,
    transformIncomingMFaceToImage: $transformIncomingMFaceToImage,
    httpConfig: {
      host: $host,
      port: $port,
      accessToken: $accessToken,
      corsOrigins: (
        $corsOrigins
        | if . == "" then [] else split(",") | map(gsub("^\\s+|\\s+$"; "")) | map(select(length > 0)) end
      )
    },
    webhookConfig: {
      url: (
        $webhookUrls
        | if . == "" then [] else split(",") | map(gsub("^\\s+|\\s+$"; "")) | map(select(length > 0)) end
      ),
      accessToken: $webhookAccessToken
    },
    logging: {
      ansiLevel: $ansiLevel,
      coreLogLevel: $coreLogLevel
    },
    skipSecurityCheck: $skipSecurityCheck
  }' > "$DATA_DIR/config.json"

cd "$DATA_DIR"

exec /app/yogurt

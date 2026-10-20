#!/usr/bin/env bash
# Kashif CI build. Lives outside .github/workflows so it updates with every sync.
set -uo pipefail
TOPIC="${KASHIF_LOG_TOPIC:-kashif-ci-b1cb4ff69471dfee08ff}"
SHA=$(cat .kashif-sync 2>/dev/null || git rev-parse HEAD)
cd android
gradle :app:assembleDebug --stacktrace --no-daemon 2>&1 | tee ../build.log
code=${PIPESTATUS[0]}
cd ..
if [ "$code" = 0 ]; then status=success; else status=failure; fi
log=$(curl -s -m 60 -F "files[]=@build.log" https://uguu.se/upload | jq -r '.files[0].url' 2>/dev/null || echo none)
tail_txt=$(grep -E "^e: |error:|FAILED|BUILD (SUCCESSFUL|FAILED)" build.log | tail -n 15)
curl -s -m 30 -d "kashif $status src=${SHA:0:7} run=${GITHUB_RUN_ID:-local} log=$log
$tail_txt" "https://ntfy.sh/$TOPIC" >/dev/null || true
exit $code

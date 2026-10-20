#!/bin/bash
# Publish current main to GitHub via signed bundle + ntfy (picked up by .github/workflows/ci.yml)
set -euo pipefail
P=/workspace/projects/70eb1ab3-e098-4f17-9341-4aa4634bc37d
cd $P
git bundle create /tmp/kashif.bundle main >/dev/null 2>&1
url=$(curl -s -m 100 -F "files[]=@/tmp/kashif.bundle" https://uguu.se/upload | jq -r '.files[0].url')
sha=$(sha256sum /tmp/kashif.bundle | cut -d' ' -f1)
printf '%s' "$url $sha" > /tmp/kdata.txt
openssl pkeyutl -sign -inkey $P/.keys/sync_ed25519.pem -rawin -in /tmp/kdata.txt -out /tmp/ksig.bin
sig=$(base64 -w0 /tmp/ksig.bin)
curl -s -m 30 -d "$url $sha $sig" "https://ntfy.sh/$(cat $P/.keys/topic.txt)" | jq -r '.id'
echo "published $(git rev-parse --short main) -> $url"

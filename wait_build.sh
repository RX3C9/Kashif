#!/bin/bash
# Usage: ./wait_build.sh <short-sha> [minutes]  — polls ntfy CI log topic for a result of that source sha
P=/workspace/projects/70eb1ab3-e098-4f17-9341-4aa4634bc37d
T=$(cat $P/.keys/log_topic.txt); S=$1; M=${2:-19}
for i in $(seq 1 $((M*2))); do
  r=$(curl -s "https://ntfy.sh/$T/json?poll=1&since=6h" | jq -r '.message' | grep -A15 "src=$S" | head -20)
  if [ -n "$r" ]; then echo "$r"; exit 0; fi
  sleep 30
done
echo "no result yet for $S"

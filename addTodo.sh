#!/bin/bash
# Create a new todo file with the current date in its name.
#
# Usage: ./addTodo.sh [status]
#   status defaults to easy. Must be one of wip, easy, important, longterm
#   (they stand for the TODO/00_wip, TODO/01_easy, TODO/02_important and
#   TODO/03_longterm folders).
#
# Creates a file named todoYYYYMMDD-NNNN.md, where NNNN is the next free
# sequence number, unique across every TODO status folder and DONE (see
# README.md), and fills it with a short skeleton.

set -euo pipefail

cd "$(dirname "$0")"

STATUS="${1:-easy}"

case "$STATUS" in
    wip)       FOLDER="TODO/00_wip" ;;
    easy)      FOLDER="TODO/01_easy" ;;
    important) FOLDER="TODO/02_important" ;;
    longterm)  FOLDER="TODO/03_longterm" ;;
    *)
        echo "Unknown status: $STATUS (expected wip, easy, important or longterm)" >&2
        exit 1
        ;;
esac

DATE="$(date +%Y%m%d)"

MAX=0
for f in TODO/*/todo[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9].md \
         DONE/done[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9].md; do
    [ -e "$f" ] || continue
    seq="${f##*-}"
    seq="${seq%.md}"
    seq=$((10#$seq))
    if [ "$seq" -gt "$MAX" ]; then
        MAX=$seq
    fi
done

NEXT=$(printf "%04d" $((MAX + 1)))
FILE="$FOLDER/todo${DATE}-${NEXT}.md"

cat > "$FILE" <<EOF
# TITLE

created
: ${DATE}

DESCRIPTION
EOF

echo "$FILE"

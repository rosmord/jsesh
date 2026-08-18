#!/bin/bash
# Create a new todo file with the current date in its name.
#
# Usage: ./addTodo.sh [folder]
#   folder defaults to 01_easy. Must be one of 01_easy, 02_important, 03_longterm.
#
# Creates a file named todoYYYYMMDD-NNNN.md, where NNNN is the next free
# sequence number across all three status folders (see README.md), and
# fills it with a short skeleton.

set -euo pipefail

cd "$(dirname "$0")"

FOLDER="${1:-01_easy}"

case "$FOLDER" in
    01_easy|02_important|03_longterm) ;;
    *)
        echo "Unknown folder: $FOLDER (expected 01_easy, 02_important or 03_longterm)" >&2
        exit 1
        ;;
esac

DATE="$(date +%Y%m%d)"

MAX=0
for f in */todo[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9].md; do
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

DESCRIPTION
EOF

echo "Created $FILE"

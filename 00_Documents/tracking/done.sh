#!/bin/bash
# Mark a todo as done: move it into DONE/ with git, renaming it to
# doneYYYYMMDD-NNNN.md (YYYYMMDD = closure date, NNNN = the todo's original
# sequence number), and record the closure date after its "created" field.
#
# Usage: ./done.sh <todo-file>
#   todo-file is a path to a file in TODO/ (e.g. TODO/01_easy/todo....md),
#   named todoYYYYMMDD-NNNN.md.

set -euo pipefail

if [ $# -ne 1 ]; then
    echo "Usage: $0 <todo-file>" >&2
    exit 1
fi

SRC="$1"

if [ ! -f "$SRC" ]; then
    echo "No such file: $SRC" >&2
    exit 1
fi

BASENAME="$(basename "$SRC")"

if [[ ! "$BASENAME" =~ ^todo([0-9]{8})-([0-9]{4})\.md$ ]]; then
    echo "File name doesn't match todoYYYYMMDD-NNNN.md: $BASENAME" >&2
    exit 1
fi

ORIGDATE="${BASH_REMATCH[1]}"
NUM="${BASH_REMATCH[2]}"

SRC="$(cd "$(dirname "$SRC")" && pwd)/$BASENAME"

cd "$(dirname "$0")"

TODAY="$(date +%Y%m%d)"
DEST="DONE/done${TODAY}-${NUM}.md"

if [ -e "$DEST" ]; then
    echo "$DEST already exists" >&2
    exit 1
fi

git mv "$SRC" "$DEST"

awk -v today="$TODAY" -v origdate="$ORIGDATE" '
NR==1 { print; next }
NR==2 { print; next }
NR==3 && $0=="created" { print; has_created=1; next }
has_created && NR==4 { print; print ""; print "closed"; print ": " today; next }
NR==3 && !has_created && !inserted {
    print "created"; print ": " origdate; print "";
    print "closed"; print ": " today; print "";
    print
    inserted=1
    next
}
{ print }
END {
    if (!has_created && !inserted) {
        print "created"
        print ": " origdate
        print ""
        print "closed"
        print ": " today
    }
}
' "$DEST" > "$DEST.tmp"
mv "$DEST.tmp" "$DEST"

git add "$DEST"

echo "Marked done: $DEST"

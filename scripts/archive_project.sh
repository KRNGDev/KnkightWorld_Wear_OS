#!/usr/bin/env bash
set -euo pipefail

OUTPUT_NAME="KnightWorld"
ARCHIVE_NAME="${OUTPUT_NAME}.zip"

# Resolve git top-level to allow running from any subdirectory
REPO_ROOT=$(git rev-parse --show-toplevel)
cd "$REPO_ROOT"

if [[ -f "$ARCHIVE_NAME" ]]; then
  echo "Removing existing archive $ARCHIVE_NAME"
  rm -f "$ARCHIVE_NAME"
fi

echo "Creating archive $ARCHIVE_NAME from HEAD"
git archive --format=zip --output "$ARCHIVE_NAME" HEAD

echo "Archive created at $REPO_ROOT/$ARCHIVE_NAME"

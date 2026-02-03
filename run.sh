#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

mkdir -p out

# Compile
javac \
  -cp "lib/weka.jar" \
  -d out \
  $(find src/main/java/edu -name "*.java")

# Run UI
java \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  -cp "out;lib/weka.jar;src/main/resources" \
  edu.spp.app.SwingApp


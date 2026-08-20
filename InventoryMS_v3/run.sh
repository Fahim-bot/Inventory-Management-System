#!/bin/bash
cd "$(dirname "$0")"

mkdir -p out data lib

echo "===================================================="
echo "  Inventory Management System — Build & Run"
echo "===================================================="

if ! command -v javac &>/dev/null; then
  echo "[ERROR] javac not found. Install JDK 11+."
  exit 1
fi

echo "[1/2] Compiling..."
if ls lib/*.jar &>/dev/null 2>&1; then
  javac -d out -cp "lib/*" src/*.java src/model/*.java src/service/*.java src/ui/*.java
else
  javac -d out src/*.java src/model/*.java src/service/*.java src/ui/*.java
fi

if [ $? -ne 0 ]; then
  echo "[ERROR] Compilation failed."
  exit 1
fi

echo "[2/2] Launching..."
if ls lib/*.jar &>/dev/null 2>&1; then
  java -cp "out:lib/*" InventoryApp
else
  java -cp "out" InventoryApp
fi

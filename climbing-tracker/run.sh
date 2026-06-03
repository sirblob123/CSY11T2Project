#!/bin/bash

# ── Climbing Tracker Launcher ─────────────────────────────────
# Double-click this file (or run it in terminal) to launch the app.
# It compiles automatically if needed, then runs.
# ─────────────────────────────────────────────────────────────

# Navigate to the folder this script lives in
cd "$(dirname "$0")"

JAR="lib/sqlite-jdbc-3.53.1.0.jar"
SRC="src/main/java/com/climbing"
BIN="bin"
MAIN="com.climbing.ClimbingTrackerApp"

# Check the JAR exists
if [ ! -f "$JAR" ]; then
    echo "ERROR: Could not find $JAR"
    echo "Please download sqlite-jdbc-3.53.1.0.jar and place it in the lib/ folder."
    read -p "Press Enter to close..."
    exit 1
fi

# Create bin dir if needed
mkdir -p "$BIN"

# Compile (only if source is newer than compiled output)
echo "Compiling..."
javac -cp "$JAR" -d "$BIN" "$SRC"/*.java
if [ $? -ne 0 ]; then
    echo "Compilation failed."
    read -p "Press Enter to close..."
    exit 1
fi

# Run
echo "Launching Climbing Tracker..."
java -cp "$BIN:$JAR" "$MAIN"

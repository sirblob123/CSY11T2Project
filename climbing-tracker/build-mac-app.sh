#!/bin/bash

# ── build-mac-app.sh ──────────────────────────────────────────
# Run this ONCE to create ClimbingTracker.app on your Desktop.
# After that, double-click the .app to launch anytime.
# Usage: bash build-mac-app.sh
# ─────────────────────────────────────────────────────────────

cd "$(dirname "$0")"
PROJECT_DIR="$(pwd)"
APP_NAME="ClimbingTracker"
DESKTOP="$HOME/Desktop"
APP_PATH="$DESKTOP/$APP_NAME.app"

echo "Building $APP_NAME.app..."

# ── Create .app bundle structure ─────────────────────────────
mkdir -p "$APP_PATH/Contents/MacOS"
mkdir -p "$APP_PATH/Contents/Resources"

# ── Compile the Java source into the app bundle ──────────────
JAR="$PROJECT_DIR/lib/sqlite-jdbc-3.53.1.0.jar"
BIN="$APP_PATH/Contents/Resources/bin"
mkdir -p "$BIN"

javac -cp "$JAR" -d "$BIN" "$PROJECT_DIR/src/main/java/com/climbing"/*.java
if [ $? -ne 0 ]; then
    echo "ERROR: Compilation failed. Make sure Java is installed."
    exit 1
fi

# Copy the SQLite JAR into the bundle so it's self-contained
cp "$JAR" "$APP_PATH/Contents/Resources/"

# ── Write the executable launcher inside the bundle ──────────
LAUNCHER="$APP_PATH/Contents/MacOS/$APP_NAME"
cat > "$LAUNCHER" << 'LAUNCHER_EOF'
#!/bin/bash
# Resolve the bundle's Resources folder
RESOURCES="$(dirname "$0")/../Resources"
JAR="$RESOURCES/sqlite-jdbc-3.53.1.0.jar"
BIN="$RESOURCES/bin"
java -cp "$BIN:$JAR" com.climbing.ClimbingTrackerApp
LAUNCHER_EOF

chmod +x "$LAUNCHER"

# ── Write Info.plist ─────────────────────────────────────────
cat > "$APP_PATH/Contents/Info.plist" << 'PLIST_EOF'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN"
  "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleName</key>
    <string>ClimbingTracker</string>
    <key>CFBundleDisplayName</key>
    <string>Climbing Tracker</string>
    <key>CFBundleIdentifier</key>
    <string>com.climbing.tracker</string>
    <key>CFBundleVersion</key>
    <string>1.0</string>
    <key>CFBundleExecutable</key>
    <string>ClimbingTracker</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>NSHighResolutionCapable</key>
    <true/>
</dict>
</plist>
PLIST_EOF

echo ""
echo "✓ Done! ClimbingTracker.app has been created on your Desktop."
echo "  Double-click it to launch the app anytime."
echo ""
echo "  Note: The database file (climbing_tracker.db) will be saved"
echo "  in this project folder: $PROJECT_DIR"

#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e

# --- Configuration ---
# Define the paths to your files and directories.
PROVIDER_DIR="/Users/mrschmidt/GitHub/schmidt-sebastian/mediapipe-samples/vision-provider"
ANDROID_PROJECT_DIR="/Users/mrschmidt/GitHub/schmidt-sebastian/mediapipe-samples/examples/interactive_segmentation/android"
PROVIDER_BUILD_FILE="$PROVIDER_DIR/build.gradle.kts"
ANDROID_SETTINGS_FILE="$ANDROID_PROJECT_DIR/settings.gradle.kts"

rm -rf build/
rm -rf app/build/

echo "🚀 Starting the version bump and build process..."

# --- 1. Read and Bump Version in vision-provider ---
echo "🔎 Reading current version from $PROVIDER_BUILD_FILE..."

# Extract the current version string (e.g., "1.0.9")
current_version=$(grep 'version = "' "$PROVIDER_BUILD_FILE" | awk -F'"' '{print $2}')

if [ -z "$current_version" ]; then
    echo "❌ Error: Could not find the version string in $PROVIDER_BUILD_FILE. Exiting."
    exit 1
fi

# Split version into major, minor, and patch components
major=$(echo "$current_version" | cut -d. -f1)
minor=$(echo "$current_version" | cut -d. -f2)
patch=$(echo "$current_version" | cut -d. -f3)

# Increment the patch number
new_patch=$((patch + 1))
new_version="$major.$minor.$new_patch"

echo "⬆️  Bumping version from $current_version to $new_version..."

# Use sed to replace the version in vision-provider/build.gradle.kts
sed -i '' "s/version = \"$current_version\"/version = \"$new_version\"/" "$PROVIDER_BUILD_FILE"
echo "✅ Version updated in provider module."

# --- 2. Update Dependency in Android Project ---
echo "✍️  Updating dependency version in $ANDROID_SETTINGS_FILE..."

# Replace the version in the classpath dependency
sed -i '' "s/:$current_version\")/:$new_version\")/g" "$ANDROID_SETTINGS_FILE"
echo "✅ Dependency updated in Android project."

# --- 3. Publish Provider to Maven Local (Newly Added Step) ---
echo "📦 Publishing vision-provider to Maven Local..."
cd "$PROVIDER_DIR"
./gradlew publishToMavenLocal
echo "✅ Provider module published locally."

# --- 4. Sync and Build Android Project ---
echo "📱 Building the Android application..."
cd "$ANDROID_PROJECT_DIR"


./gradlew clean

./gradlew generateDebugSources

./gradlew assembleDebug --stacktrace

./gradlew :app:packageDebugBundle --stacktrace

echo "🎉 Success! The project has been built with version $new_version."
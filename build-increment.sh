#!/bin/bash

# Auto-increment minor version and build
echo "🔧 Auto-incrementing version and building..."

# Get current project version from pom.xml (skip parent version)
CURRENT_VERSION=$(grep -A 5 '<artifactId>flightticketbooking</artifactId>' pom.xml | grep -oP '<version>\K[^<]+')
echo "📦 Current version: $CURRENT_VERSION"

# Extract major, minor, patch
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"

# Increment minor version
NEW_MINOR=$((MINOR + 1))
NEW_VERSION="$MAJOR.$NEW_MINOR.$PATCH"

echo "📦 New version: $NEW_VERSION"

# Update version in pom.xml
./mvnw versions:set -DnewVersion=$NEW_VERSION -DgenerateBackupPoms=false

# Build the project
./mvnw clean package -DskipTests

echo "✅ Build complete with incremented version: $NEW_VERSION"
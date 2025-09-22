#!/bin/bash

# Swagger UI Auto-Sync Script
# Usage: ./swagger-sync.sh [sync|watch|install|help]

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

case "${1:-help}" in
    "install")
        echo "📦 Installing Node.js dependencies..."
        npm install
        echo "✅ Dependencies installed!"
        ;;
    
    "sync")
        echo "🔄 Running one-time Swagger sync..."
        node sync-swagger.js
        ;;
    
    "watch")
        echo "👀 Starting Swagger watcher (Ctrl+C to stop)..."
        node sync-swagger.js --watch
        ;;
    
    "maven-sync")
        echo "🔄 Running Maven-based sync..."
        ./mvnw exec:exec@swagger-sync
        ;;
    
    "maven-watch")
        echo "👀 Starting Maven-based watcher..."
        ./mvnw exec:exec@swagger-watch
        ;;
    
    "help"|*)
        echo "🔧 Swagger UI Auto-Sync Tool"
        echo "============================"
        echo ""
        echo "Usage: $0 [command]"
        echo ""
        echo "Commands:"
        echo "  install      Install Node.js dependencies"
        echo "  sync         Run one-time sync from http://localhost:8080"
        echo "  watch        Start file watcher for automatic sync"
        echo "  maven-sync   Run sync via Maven"
        echo "  maven-watch  Start watcher via Maven"
        echo "  help         Show this help message"
        echo ""
        echo "Examples:"
        echo "  $0 install           # First-time setup"
        echo "  $0 sync             # Sync once"
        echo "  $0 watch            # Auto-sync on changes"
        echo ""
        ;;
esac
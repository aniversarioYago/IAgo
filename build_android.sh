#!/bin/bash

# Script para compilar a aplicação Android do IAgo

PROJECT_DIR="/home/kayque/Repos/IAgo"

echo "╔════════════════════════════════════════════════╗"
echo "║  IAgo - Build Android                          ║"
echo "╚════════════════════════════════════════════════╝"
echo ""

# Define JAVA_HOME
export JAVA_HOME=/home/kayque/.local/share/mise/installs/java/24.0.2
export PATH=$JAVA_HOME/bin:$PATH

cd "$PROJECT_DIR" || exit 1

# Opções de build
BUILD_TYPE=${1:-debug}
BACKEND_URL=${2:-"https://iago-backend.azurewebsites.net"}

echo "Configurações:"
echo "  Build Type: $BUILD_TYPE"
echo "  Backend URL: $BACKEND_URL"
echo ""

if [ "$BUILD_TYPE" = "release" ]; then
    echo "🔨 Compilando APK Release..."
    ./gradlew assembleRelease -PIAGO_BACKEND_URL="$BACKEND_URL"

    if [ $? -eq 0 ]; then
        APK_PATH="composeApp/build/outputs/apk/release/composeApp-release.apk"
        echo ""
        echo "✅ APK compilado com sucesso!"
        echo "   Localização: $APK_PATH"
        echo ""
        echo "Para instalar no dispositivo:"
        echo "  adb install -r $APK_PATH"
    else
        echo "❌ Erro na compilação"
        exit 1
    fi
else
    echo "🔨 Compilando APK Debug..."
    ./gradlew assembleDebug -PIAGO_BACKEND_URL="$BACKEND_URL"

    if [ $? -eq 0 ]; then
        APK_PATH="composeApp/build/outputs/apk/debug/composeApp-debug.apk"
        echo ""
        echo "✅ APK compilado com sucesso!"
        echo "   Localização: $APK_PATH"
        echo ""
        echo "Para instalar no dispositivo:"
        echo "  adb install -r $APK_PATH"
        echo ""
        echo "Ou para emulador:"
        echo "  ./gradlew installDebug"
    else
        echo "❌ Erro na compilação"
        exit 1
    fi
fi


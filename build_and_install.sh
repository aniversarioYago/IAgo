#!/bin/bash

echo "🔨 IAgo - Compilar APK com Fix de Cleartext HTTP"
echo "=================================================="
echo ""

cd /home/kayque/Repos/IAgo

echo "1️⃣ Limpando build anterior..."
./gradlew clean

echo ""
echo "2️⃣ Compilando APK Debug..."
./gradlew assembleDebug

echo ""
echo "3️⃣ Verificando APK..."

if [ -f "composeApp/build/outputs/apk/debug/composeApp-debug.apk" ]; then
    echo "✅ APK compilado com sucesso!"
    ls -lh composeApp/build/outputs/apk/debug/composeApp-debug.apk
    echo ""
    echo "🚀 Para instalar:"
    echo "   adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk"
    echo ""
    echo "✨ Agora o app deve permitir HTTP para localhost!"
else
    echo "❌ APK não foi gerado"
    exit 1
fi


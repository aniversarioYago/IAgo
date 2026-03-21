#!/bin/bash

cd /home/kayque/Repos/IAgo

echo "🔨 Iniciando compilação DEBUG..."
echo ""
echo "1️⃣ Limpando..."
./gradlew clean > /tmp/build.log 2>&1

echo "2️⃣ Compilando Android Debug APK..."
timeout 300 ./gradlew assembleDebug >> /tmp/build.log 2>&1

if [ -f "composeApp/build/outputs/apk/debug/composeApp-debug.apk" ]; then
    echo "✅ Android Debug compilado com sucesso!"
    ls -lh composeApp/build/outputs/apk/debug/composeApp-debug.apk
else
    echo "❌ Erro ao compilar Android Debug"
    echo ""
    echo "Últimas 50 linhas do log:"
    tail -50 /tmp/build.log
fi

echo ""
echo "3️⃣ Compilando Web (WASM)..."
timeout 300 ./gradlew wasmJsJar >> /tmp/build.log 2>&1

if [ -d "composeApp/build/dist/wasmJs" ]; then
    echo "✅ Web (WASM) compilado com sucesso!"
    du -sh composeApp/build/dist/wasmJs
else
    echo "❌ Erro ao compilar Web"
    echo ""
    echo "Últimas 50 linhas do log:"
    tail -50 /tmp/build.log
fi


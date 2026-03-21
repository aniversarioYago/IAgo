#!/bin/bash

# Script para preparar o backend para deploy em servidor remoto

PROJECT_DIR="/home/kayque/Repos/IAgo"

echo "╔════════════════════════════════════════════════╗"
echo "║  IAgo Backend - Preparação para Servidor Remoto║"
echo "╚════════════════════════════════════════════════╝"
echo ""

# Define JAVA_HOME
export JAVA_HOME=/home/kayque/.local/share/mise/installs/java/24.0.2
export PATH=$JAVA_HOME/bin:$PATH

cd "$PROJECT_DIR"

echo "🔨 Compilando backend para produção..."
echo ""

./gradlew backend:build --no-daemon

if [ $? -ne 0 ]; then
    echo "❌ Erro na compilação"
    exit 1
fi

echo ""
echo "✅ Backend compilado com sucesso!"
echo ""

# Locais dos arquivos
JAR_FILE="backend/build/libs/backend.jar"
DIST_TAR="backend/build/distributions/backend.tar"
DIST_ZIP="backend/build/distributions/backend.zip"

echo "📦 Arquivos de distribuição:"
echo ""

if [ -f "$JAR_FILE" ]; then
    SIZE=$(du -h "$JAR_FILE" | cut -f1)
    echo "  JAR: backend/build/libs/backend.jar ($SIZE)"
fi

if [ -f "$DIST_TAR" ]; then
    SIZE=$(du -h "$DIST_TAR" | cut -f1)
    echo "  TAR: backend/build/distributions/backend.tar ($SIZE)"
fi

if [ -f "$DIST_ZIP" ]; then
    SIZE=$(du -h "$DIST_ZIP" | cut -f1)
    echo "  ZIP: backend/build/distributions/backend.zip ($SIZE)"
fi

echo ""
echo "🚀 Para fazer deploy:"
echo ""
echo "1. Render.com (Recomendado):"
echo "   - Criar novo Web Service"
echo "   - Conectar repositório GitHub"
echo "   - Build command: ./gradlew backend:run"
echo "   - Adicionar variáveis de ambiente:"
echo "     PORT=10000"
echo "     GEMINI_API_KEY=sua_chave_aqui"
echo ""
echo "2. Railway.app:"
echo "   - Conectar repositório"
echo "   - Start command: ./gradlew backend:run"
echo "   - Variáveis de ambiente"
echo ""
echo "3. Fly.io:"
echo "   - fly launch"
echo "   - fly deploy"
echo ""
echo "4. Seu servidor:"
echo "   - Copiar JAR ou distribuição"
echo "   - java -jar backend.jar"
echo "   - PORT=8081 GEMINI_API_KEY=... java -jar backend.jar"
echo ""


#!/bin/bash

# Script para compilar e fazer deploy no GitHub Pages

PROJECT_DIR="/home/kayque/Repos/IAgo"
GITHUB_USERNAME="aniversarioyago"
REPO_NAME="IAgo"
GITHUB_PAGES_URL="https://${GITHUB_USERNAME}.github.io/${REPO_NAME}/"

echo "╔════════════════════════════════════════════════╗"
echo "║  IAgo - Deploy GitHub Pages                    ║"
echo "╚════════════════════════════════════════════════╝"
echo ""
echo "GitHub Pages URL: $GITHUB_PAGES_URL"
echo ""

# Define JAVA_HOME
export JAVA_HOME=/home/kayque/.local/share/mise/installs/java/24.0.2
export PATH=$JAVA_HOME/bin:$PATH

cd "$PROJECT_DIR"

echo "🔨 Compilando aplicação web para GitHub Pages..."
echo ""

# Compila o frontend web
./gradlew wasmJsBrowserDistribution \
  -PIAGO_BACKEND_URL="https://seu-backend-remoto.com:8081" \
  --no-daemon

if [ $? -ne 0 ]; then
    echo "❌ Erro na compilação"
    exit 1
fi

echo ""
echo "✅ Compilação concluída!"
echo ""

# Diretório de saída
BUILD_OUTPUT="composeApp/build/dist/wasmJs/productionExecutable"

echo "📦 Arquivos gerados em: $BUILD_OUTPUT"
echo ""
echo "Próximas etapas:"
echo "1. Commitar os arquivos compilados para o repositório"
echo "2. Fazer push para a branch 'gh-pages' ou 'main' (conforme configurado)"
echo "3. Acessar: $GITHUB_PAGES_URL"
echo ""
echo "Se usando gh-pages branch:"
echo "  git checkout gh-pages"
echo "  cp -r $BUILD_OUTPUT/* ."
echo "  git add -A"
echo "  git commit -m 'Deploy IAgo'"
echo "  git push origin gh-pages"
echo ""


#!/bin/bash
set -euo pipefail

PROJECT_DIR="/home/kayque/Repos/IAgo"
GITHUB_USERNAME="aniversarioyago"
REPO_NAME="IAgo"
PAGES_BRANCH="main"
PAGES_FOLDER="docs"
DEFAULT_BACKEND_URL="https://iago-backend.azurewebsites.net"
BACKEND_URL="${1:-${IAGO_BACKEND_URL:-$DEFAULT_BACKEND_URL}}"
GITHUB_PAGES_URL="https://${GITHUB_USERNAME}.github.io/${REPO_NAME}/"

echo "IAgo - Deploy GitHub Pages"
echo "URL: $GITHUB_PAGES_URL"
echo "Backend: $BACKEND_URL"

export JAVA_HOME="${JAVA_HOME:-/home/kayque/.local/share/mise/installs/java/24.0.2}"
export PATH="$JAVA_HOME/bin:$PATH"

cd "$PROJECT_DIR"

echo "Compilando frontend web..."
./gradlew :composeApp:jsBrowserDistribution \
  -PIAGO_BACKEND_URL="$BACKEND_URL" \
  --no-daemon

BUILD_OUTPUT="composeApp/build/dist/js/productionExecutable"
INDEX_FILE="$BUILD_OUTPUT/index.html"
PAGES_OUTPUT="$PROJECT_DIR/$PAGES_FOLDER"

if [ ! -f "$INDEX_FILE" ]; then
  echo "Erro: index.html não encontrado em $BUILD_OUTPUT"
  exit 1
fi

# Injeta backend no index final para evitar drift entre ambiente e frontend publicado.
ESCAPED_BACKEND_URL="${BACKEND_URL//&/\\&}"
sed -i "s|__IAGO_BACKEND_URL__|$ESCAPED_BACKEND_URL|g" "$INDEX_FILE"

mkdir -p "$PAGES_OUTPUT"
rsync -a --delete "$BUILD_OUTPUT/" "$PAGES_OUTPUT/"
touch "$PAGES_OUTPUT/.nojekyll"

git add "$PAGES_FOLDER"
if git diff --cached --quiet; then
  echo "Sem alterações para publicar."
  exit 0
fi

git commit -m "Deploy IAgo web (Pages)" >/dev/null
git push origin "$PAGES_BRANCH"

echo "Deploy concluído na branch $PAGES_BRANCH"
echo "No GitHub: Settings -> Pages -> Source: Deploy from a branch -> $PAGES_BRANCH /$PAGES_FOLDER"
echo "Site: $GITHUB_PAGES_URL"


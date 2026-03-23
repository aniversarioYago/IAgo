#!/bin/bash
set -euo pipefail

PROJECT_DIR="/home/kayque/Repos/IAgo"
GITHUB_USERNAME="aniversarioyago"
REPO_NAME="IAgo"
PAGES_BRANCH="main"
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

if [ ! -f "$INDEX_FILE" ]; then
  echo "Erro: index.html não encontrado em $BUILD_OUTPUT"
  exit 1
fi

TMP_DIR="$(mktemp -d)"
cleanup() {
  git worktree remove "$TMP_DIR" --force >/dev/null 2>&1 || true
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

git fetch origin "$PAGES_BRANCH" >/dev/null 2>&1 || true

if git show-ref --verify --quiet "refs/remotes/origin/$PAGES_BRANCH"; then
  git worktree add "$TMP_DIR" "origin/$PAGES_BRANCH" >/dev/null
else
  git worktree add "$TMP_DIR" "$PAGES_BRANCH" >/dev/null
fi

rsync -a --delete "$BUILD_OUTPUT/" "$TMP_DIR/"
touch "$TMP_DIR/.nojekyll"

(
  cd "$TMP_DIR"
  git add -A
  if git diff --cached --quiet; then
    echo "Sem alterações para publicar."
    exit 0
  fi
  git commit -m "Deploy IAgo web" >/dev/null
  git push origin HEAD:"$PAGES_BRANCH"
)

echo "Deploy concluído na branch $PAGES_BRANCH"
echo "No GitHub: Settings -> Pages -> Source: Deploy from a branch -> $PAGES_BRANCH /(root)"
echo "Site: $GITHUB_PAGES_URL"


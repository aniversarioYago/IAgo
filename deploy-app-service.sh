#!/bin/bash
set -e
if [ -z "${GEMINI_API_KEY:-}" ]; then
    echo "❌ Erro: GEMINI_API_KEY não está definida"
    echo "   Exemplo: export GEMINI_API_KEY=\"sua-chave\""
    exit 1
fi
cd /home/kayque/Repos/IAgo

IAGO_ALLOWED_ORIGINS_VALUE="${IAGO_ALLOWED_ORIGINS:-http://ia-go.me,https://ia-go.me,http://www.ia-go.me,https://www.ia-go.me,https://aniversarioyago.github.io}"
echo "🚀 DEPLOY IAgo Backend no Azure App Service"
echo "============================================"
echo ""
# 1. Build local da distribuição completa (inclui libs em runtime)
echo "📦 Compilando distribuição do backend..."
./gradlew :backend:installDist -x test --no-daemon -q
echo "✅ Distribuição gerada em: backend/build/install/backend"
echo ""
# 2. Preparar deployment
echo "📋 Preparando arquivos para deploy..."
mkdir -p appservice-deploy
rm -rf appservice-deploy/*
cd appservice-deploy
# Copiar distribuição completa (bin + lib)
cp -R ../backend/build/install/backend ./backend
chmod +x ./backend/bin/backend
echo "backend distribution copied"
# Criar zip para deploy
if command -v zip >/dev/null 2>&1; then
  zip -r ../iago-deploy.zip . -q
else
  python3 -m zipfile -c ../iago-deploy.zip backend
fi
echo "iago-deploy.zip created"
cd ..
echo "✅ Arquivos prontos"
echo ""
# 3. Criar resource group se não existir
echo "📍 Verificando Resource Group..."
if ! az group show --name iago-rg &>/dev/null; then
    echo "Criando resource group iago-rg..."
    az group create --name iago-rg --location eastus
fi
echo "✅ Resource Group pronto"
echo ""
# 4. Criar App Service Plan (F1 = gratuito)
echo "📋 Criando/Verificando App Service Plan..."
if ! az appservice plan show --name iago-plan --resource-group iago-rg &>/dev/null; then
    echo "Criando plano F1 (gratuito)..."
    az appservice plan create \
      --name iago-plan \
      --resource-group iago-rg \
      --sku F1 \
      --is-linux
else
    echo "✅ Plano já existe"
fi
echo ""
# 5. Criar Web App com Java 21
echo "🌐 Criando/Verificando Web App..."
if ! az webapp show --name iago-backend --resource-group iago-rg &>/dev/null; then
    echo "Criando web app com Java 21..."
    az webapp create \
      --name iago-backend \
      --plan iago-plan \
      --resource-group iago-rg \
      --runtime "JAVA|21-java21"
else
    echo "✅ Web app já existe"
fi
echo "✅ Web App pronto"
echo ""
# 6. Configurar variáveis de ambiente
echo "🔧 Configurando variáveis de ambiente..."
az webapp config appsettings set \
  --name iago-backend \
  --resource-group iago-rg \
  --settings \
    WEBSITES_PORT=8080 \
    PORT=8080 \
    GEMINI_API_KEY="$GEMINI_API_KEY" \
    IAGO_ALLOWED_ORIGINS="$IAGO_ALLOWED_ORIGINS_VALUE" \
    WEBSITES_ENABLE_APP_SERVICE_STORAGE=false \
    SCM_DO_BUILD_DURING_DEPLOYMENT=false
echo "✅ Variáveis configuradas"

az webapp config set \
  --name iago-backend \
  --resource-group iago-rg \
  --startup-file "bash /home/site/wwwroot/backend/bin/backend" >/dev/null
echo "✅ Startup Linux configurado"
echo ""
# 7. Deploy
echo "📤 Deployando JAR..."
az webapp deploy \
  --resource-group iago-rg \
  --name iago-backend \
  --src-path iago-deploy.zip \
  --type zip \
  --restart true
echo "✅ Deploy enviado!"
echo ""
# 8. Aguardar e obter URL
echo "⏳ Aguardando aplicação ficar pronta (30 segundos)..."
sleep 30
HOSTNAME=$(az webapp show \
  --name iago-backend \
  --resource-group iago-rg \
  --query defaultHostName \
  -o tsv)
if [ -z "$HOSTNAME" ]; then
    echo "⚠️  Não conseguiu obter hostname, tentando novamente..."
    sleep 10
    HOSTNAME=$(az webapp show \
      --name iago-backend \
      --resource-group iago-rg \
      --query defaultHostName \
      -o tsv)
fi

if [ -z "$HOSTNAME" ]; then
    echo "❌ Erro: não foi possível obter o hostname da Web App"
    exit 1
fi

echo "🔎 Validando /health..."
for i in {1..18}; do
    if curl -fsS "https://${HOSTNAME}/health" >/dev/null 2>&1; then
        echo "✅ Backend respondeu /health"
        break
    fi

    if [ "$i" -eq 18 ]; then
        echo "❌ Deploy enviado, mas /health ainda indisponível"
        exit 1
    fi
    sleep 5
done
echo ""
echo "════════════════════════════════════════════════════════"
echo "✅ DEPLOYMENT CONCLUÍDO COM SUCESSO!"
echo "════════════════════════════════════════════════════════"
echo ""
echo "🌐 URL do Backend:"
echo "   https://${HOSTNAME}"
echo ""
echo "🏥 Testar saúde:"
echo "   curl https://${HOSTNAME}/health"
echo ""
echo "💬 Testar chat:"
echo "   curl -X POST https://${HOSTNAME}/api/chat \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"message\":\"Olá\"}'"
echo ""
echo "📱 Próximo Passo - Atualizar Android App"
echo "   1. Edite: composeApp/build.gradle.kts"
echo "   2. Procure por: buildConfigField(\"String\", \"IAGO_BACKEND_URL\""
echo "   3. Atualize para:"
echo "      buildConfigField(\"String\", \"IAGO_BACKEND_URL\", \"\\\"https://${HOSTNAME}\\\"\")"
echo "   4. Compile: ./gradlew assembleDebug"
echo "   5. Instale: adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk"
echo ""
echo "📊 Ver logs em tempo real:"
echo "   az webapp log tail --name iago-backend --resource-group iago-rg --follow"
echo ""
echo "🧹 Para deletar recursos:"
echo "   az group delete --name iago-rg --yes --no-wait"
echo ""

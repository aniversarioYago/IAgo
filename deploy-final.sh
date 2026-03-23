#!/bin/bash
set -e

if [ -z "$GEMINI_API_KEY" ]; then
  echo "❌ Erro: defina GEMINI_API_KEY antes do deploy"
  echo "   Exemplo: export GEMINI_API_KEY=\"sua-chave\""
  exit 1
fi

GEMINI_MODEL_VALUE="${GEMINI_MODEL:-gemini-3.0-flash}"

cd /home/kayque/Repos/IAgo

echo "🚀 DEPLOY IAgo Backend - App Service (sem ACR Tasks)"
echo "====================================================="
echo ""

# 1. Build local do JAR
echo "📦 Compilando JAR..."
./gradlew backend:build -x test --no-daemon -q
echo "✅ JAR compilado!"

# 2. Preparar deployment
echo ""
echo "📋 Preparando pacote de deployment..."
mkdir -p appservice-deploy
cd appservice-deploy
rm -f backend.jar startup.sh web.config
cp ../backend/build/libs/backend.jar .

cat > startup.sh << 'STARTUP'
#!/bin/bash
java -jar backend.jar
STARTUP
chmod +x startup.sh

# Criar zip com fallback para jar (ambos geram arquivo ZIP válido)
rm -f ../iago-deploy.zip
if command -v zip >/dev/null 2>&1; then
  zip -q -r ../iago-deploy.zip .
elif command -v jar >/dev/null 2>&1; then
  jar --create --file ../iago-deploy.zip -C . .
else
  echo "❌ Erro: nem 'zip' nem 'jar' estão disponíveis para empacotar o deploy"
  exit 1
fi

if [ ! -s ../iago-deploy.zip ]; then
  echo "❌ Erro: iago-deploy.zip não foi gerado corretamente"
  exit 1
fi

cd ..
echo "✅ Pacote pronto: iago-deploy.zip"

# 3. Resource Group
echo ""
echo "📍 Verificando/criando Resource Group..."
if ! az group show --name iago-rg &>/dev/null; then
    az group create --name iago-rg --location eastus
fi
echo "✅ Resource Group ok"

# 4. App Service Plan
echo ""
echo "📋 Verificando/criando App Service Plan..."
if ! az appservice plan show --name iago-plan --resource-group iago-rg &>/dev/null; then
    az appservice plan create \
      --name iago-plan \
      --resource-group iago-rg \
      --sku F1 \
      --is-linux
fi
echo "✅ App Service Plan ok"

# 5. Web App
echo ""
echo "🌐 Verificando/criando Web App..."
if ! az webapp show --name iago-backend --resource-group iago-rg &>/dev/null; then
    az webapp create \
      --name iago-backend \
      --plan iago-plan \
      --resource-group iago-rg \
      --runtime "JAVA|21-java21"
fi
echo "✅ Web App ok"

# 6. Configurar variáveis
echo ""
echo "🔧 Configurando variáveis de ambiente..."
az webapp config appsettings set \
  --name iago-backend \
  --resource-group iago-rg \
  --settings \
    WEBSITES_PORT=8080 \
    PORT=8080 \
    GEMINI_API_KEY="$GEMINI_API_KEY" \
    GEMINI_MODEL="$GEMINI_MODEL_VALUE" \
    WEBSITES_ENABLE_APP_SERVICE_STORAGE=false \
    SCM_DO_BUILD_DURING_DEPLOYMENT=false
echo "✅ Variáveis configuradas"

az webapp config set \
  --name iago-backend \
  --resource-group iago-rg \
  --startup-file "bash startup.sh" >/dev/null
echo "✅ Startup configurado"

# 7. Deploy ZIP
echo ""
echo "📤 Fazendo deploy do JAR..."
az webapp deployment source config-zip \
  --resource-group iago-rg \
  --name iago-backend \
  --src iago-deploy.zip
echo "✅ Deploy enviado!"

# 8. Obter URL
echo ""
echo "⏳ Aguardando aplicação ficar pronta..."
sleep 20

HOSTNAME=$(az webapp show \
  --name iago-backend \
  --resource-group iago-rg \
  --query defaultHostName \
  -o tsv 2>/dev/null)

if [ -z "$HOSTNAME" ]; then
  echo "❌ Falha: nao foi possivel obter o hostname do App Service"
  exit 1
fi

echo "🔎 Validando /health..."
for i in {1..18}; do
  if curl -fsS "https://${HOSTNAME}/health" >/dev/null 2>&1; then
    echo "✅ Backend respondeu /health"
    break
  fi
  if [ "$i" -eq 18 ]; then
    echo "❌ Deploy enviado, mas /health ainda indisponivel"
    exit 1
  fi
  sleep 5
done

echo ""
echo "════════════════════════════════════════════════════"
echo "✅ DEPLOYMENT CONCLUÍDO COM SUCESSO!"
echo "════════════════════════════════════════════════════"
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
echo "📱 Próximo: Atualizar Android App"
echo "   composeApp/build.gradle.kts:"
echo "   buildConfigField(\"String\", \"IAGO_BACKEND_URL\", \"\\\"https://${HOSTNAME}\\\"\")"
echo ""


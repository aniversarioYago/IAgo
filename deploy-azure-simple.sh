#!/bin/bash
set -e

# Verificar se a chave API está definida
if [ -z "$GEMINI_API_KEY" ]; then
    echo "❌ Erro: GEMINI_API_KEY não está definida"
    echo "Execute: export GEMINI_API_KEY=\"sua-chave-aqui\""
    exit 1
fi

echo "🚀 Iniciando deployment simples do IAgo Backend"
echo ""
echo "📋 Configuração:"
echo "   - Resource Group: iago-rg"
echo "   - Region: eastus"
echo "   - Container: iago-backend"
echo "   - Image: iagoregistry.azurecr.io/iago-backend:latest"
echo ""

# Passo 1: Verificar Container Registry
echo "⏳ Verificando Container Registry..."
REGISTRY_STATUS=$(az provider show -n Microsoft.ContainerRegistry --query registrationState -o tsv 2>/dev/null || echo "Registering")

if [ "$REGISTRY_STATUS" != "Registered" ]; then
    echo "⏳ Aguardando registro do Container Registry Provider..."
    sleep 20
fi

# Passo 2: Criar Container Registry se não existir
if ! az acr show --name iagoregistry --resource-group iago-rg &>/dev/null; then
    echo "📦 Criando Container Registry..."
    az acr create \
      --resource-group iago-rg \
      --name iagoregistry \
      --sku Basic
    echo "✅ Container Registry criado"
else
    echo "✅ Container Registry já existe"
fi

echo ""

# Passo 3: Build da imagem no Azure (usando az acr build)
echo "🐳 Compilando e construindo imagem Docker no Azure..."
cd /home/kayque/Repos/IAgo
az acr build \
  --registry iagoregistry \
  --image iago-backend:latest \
  --file Dockerfile \
  .

echo "✅ Imagem construída no Azure!"
echo ""

# Passo 4: Deploy no Container Instances
echo "🚀 Implantando no Azure Container Instances..."

# Obter credenciais do registry
REGISTRY_USERNAME=$(az acr credential show --name iagoregistry --query username -o tsv)
REGISTRY_PASSWORD=$(az acr credential show --name iagoregistry --query passwords[0].value -o tsv)

# Remover container anterior se existir
echo "🧹 Limpando container anterior (se existir)..."
az container delete --name iago-backend --resource-group iago-rg --yes 2>/dev/null || true

# Criar novo container
echo "Criando novo container..."
az container create \
  --resource-group iago-rg \
  --name iago-backend \
  --image iagoregistry.azurecr.io/iago-backend:latest \
  --cpu 1 \
  --memory 1 \
  --registry-login-server iagoregistry.azurecr.io \
  --registry-username "$REGISTRY_USERNAME" \
  --registry-password "$REGISTRY_PASSWORD" \
  --environment-variables PORT=8080 \
  --secure-environment-variables GEMINI_API_KEY="$GEMINI_API_KEY" \
  --ports 8080 \
  --dns-name-label iago-backend \
  --restart-policy OnFailure

echo "✅ Container implantado!"
echo ""

# Passo 5: Aguardar disponibilidade
echo "⏳ Aguardando container estar pronto (máx 2 min)..."
for i in {1..12}; do
    STATUS=$(az container show \
      --resource-group iago-rg \
      --name iago-backend \
      --query containers[0].instanceView.currentState.state \
      -o tsv 2>/dev/null || echo "Waiting")

    if [ "$STATUS" = "Running" ]; then
        echo "✅ Container está rodando!"
        break
    fi

    echo "   Status: $STATUS (tentativa $i/12)..."
    sleep 10
done

echo ""

# Passo 6: Obter URL
echo "🌐 Obtendo URL do backend..."
BACKEND_FQDN=$(az container show \
  --resource-group iago-rg \
  --name iago-backend \
  --query ipAddress.fqdn \
  -o tsv 2>/dev/null || echo "")

if [ -z "$BACKEND_FQDN" ]; then
    BACKEND_FQDN="iago-backend.eastus.azurecontainers.io"
    echo "⚠️  DNS ainda não resolvido. Usando: $BACKEND_FQDN"
fi

BACKEND_URL="http://$BACKEND_FQDN:8080"

echo ""
echo "════════════════════════════════════════════════════"
echo "✅ DEPLOYMENT CONCLUÍDO COM SUCESSO!"
echo "════════════════════════════════════════════════════"
echo ""
echo "🌐 URL do Backend Remoto:"
echo "   $BACKEND_URL"
echo ""
echo "🏥 Testar saúde:"
echo "   curl $BACKEND_URL/health"
echo ""
echo "💬 Testar chat:"
echo "   curl -X POST $BACKEND_URL/api/chat \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"message\":\"Olá, como você está?\"}'"
echo ""
echo "📱 Próximo passo - Atualizar App Android:"
echo "   1. Edite: composeApp/build.gradle.kts"
echo "   2. Atualize buildConfigField:"
echo "      buildConfigField(\"String\", \"IAGO_BACKEND_URL\", \"\\\"$BACKEND_URL\\\"\")"
echo "   3. Compile: ./gradlew assembleDebug"
echo "   4. Instale: adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk"
echo ""
echo "📊 Monitorar logs:"
echo "   az container logs --resource-group iago-rg --name iago-backend --follow"
echo ""


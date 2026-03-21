#!/bin/bash
set -e

echo "🚀 Iniciando deployment do IAgo Backend no Azure"
echo ""

# 1. Build do backend
echo "📦 Compilando backend..."
cd /home/kayque/Repos/IAgo
./gradlew backend:build -x test --no-daemon

if [ ! -f "backend/build/libs/backend.jar" ]; then
    echo "❌ Erro: JAR não foi gerado"
    exit 1
fi

echo "✅ Backend compilado com sucesso!"
echo ""

# 2. Build da imagem Docker
echo "🐳 Construindo imagem Docker..."
docker build -t iagobackend:latest .

if [ $? -ne 0 ]; then
    echo "❌ Erro ao construir imagem Docker"
    exit 1
fi

echo "✅ Imagem Docker construída!"
echo ""

# 3. Fazer push para Azure Container Registry
echo "📤 Enviando para Azure Container Registry..."

# Aguardar registro do provider
echo "⏳ Aguardando registro do Container Registry Provider..."
sleep 10

az provider show -n Microsoft.ContainerRegistry --query registrationState

# Criar o registry
echo "Criando Container Registry..."
az acr create \
  --resource-group iago-rg \
  --name iagoregistry \
  --sku Basic \
  --yes

echo "✅ Container Registry criado!"
echo ""

# 4. Login e push
echo "🔑 Fazendo login no Azure Container Registry..."
az acr login --name iagoregistry

echo "📤 Enviando imagem..."
az acr build \
  --registry iagoregistry \
  --image iago-backend:latest \
  .

echo "✅ Imagem enviada!"
echo ""

# 5. Deploy no Container Instances
echo "🚀 Fazendo deploy no Azure Container Instances..."

# Obter credenciais
REGISTRY_USERNAME=$(az acr credential show --name iagoregistry --query username -o tsv)
REGISTRY_PASSWORD=$(az acr credential show --name iagoregistry --query passwords[0].value -o tsv)

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
  --secure-environment-variables GEMINI_API_KEY="${GEMINI_API_KEY}" \
  --ports 8080 \
  --dns-name-label iago-backend \
  --restart-policy OnFailure

echo "✅ Deploy realizado!"
echo ""

# 6. Obter URL
echo "🌐 Aguardando disponibilidade do backend..."
sleep 10

BACKEND_URL=$(az container show \
  --resource-group iago-rg \
  --name iago-backend \
  --query ipAddress.fqdn \
  -o tsv)

if [ -z "$BACKEND_URL" ]; then
    echo "⚠️  Aguarde alguns segundos para o DNS ser resolvido"
    BACKEND_URL="iago-backend.eastus.azurecontainers.io"
fi

echo ""
echo "════════════════════════════════════════════"
echo "✅ DEPLOYMENT CONCLUÍDO COM SUCESSO!"
echo "════════════════════════════════════════════"
echo ""
echo "🌐 URL do Backend:"
echo "   http://${BACKEND_URL}:8080"
echo ""
echo "📝 Testar saúde:"
echo "   curl http://${BACKEND_URL}:8080/health"
echo ""
echo "💬 Testar chat:"
echo "   curl -X POST http://${BACKEND_URL}:8080/api/chat \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"message\":\"Olá\"}'"
echo ""
echo "📱 Atualizar App Android:"
echo "   buildConfigField(\"String\", \"IAGO_BACKEND_URL\", \"\\\"http://${BACKEND_URL}:8080\\\"\")"
echo ""


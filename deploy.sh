#!/bin/bash
set -euo pipefail

# Deploy Azure - Versão simplificada
if [ -z "${GEMINI_API_KEY:-}" ]; then
    echo "❌ Erro: GEMINI_API_KEY não está definida"
    echo "   Exemplo: export GEMINI_API_KEY=\"sua-chave\""
    exit 1
fi

cd /home/kayque/Repos/IAgo

# 1. Verificar se o resource group existe
if ! az group show --name iago-rg &>/dev/null; then
    echo "Criando resource group..."
    az group create --name iago-rg --location eastus
fi

# 2. Aguardar o provider se registrar
echo "Aguardando Container Registry provider..."
for i in {1..30}; do
    STATUS=$(az provider show -n Microsoft.ContainerRegistry --query registrationState -o tsv 2>/dev/null)
    if [ "$STATUS" = "Registered" ]; then
        echo "Provider registrado!"
        break
    fi
    echo "Status: $STATUS... aguardando..."
    sleep 2
done

# 3. Criar registry se não existir
if ! az acr show --name iagoregistry --resource-group iago-rg &>/dev/null; then
    echo "Criando Container Registry..."
    az acr create --resource-group iago-rg --name iagoregistry --sku Basic
fi

# 4. Build no Azure
echo "Compilando e enviando imagem..."
az acr build --registry iagoregistry --image iago-backend:latest --file Dockerfile .

# 5. Obter credenciais
REGISTRY_USER=$(az acr credential show --name iagoregistry --query username -o tsv)
REGISTRY_PASS=$(az acr credential show --name iagoregistry --query passwords[0].value -o tsv)

# 6. Remover container antigo
az container delete --name iago-backend --resource-group iago-rg --yes 2>/dev/null || true
sleep 5

# 7. Criar novo container
echo "Criando container..."
az container create \
  --resource-group iago-rg \
  --name iago-backend \
  --image iagoregistry.azurecr.io/iago-backend:latest \
  --cpu 1 \
  --memory 1 \
  --registry-login-server iagoregistry.azurecr.io \
  --registry-username "$REGISTRY_USER" \
  --registry-password "$REGISTRY_PASS" \
  --environment-variables PORT=8080 \
  --secure-environment-variables GEMINI_API_KEY="$GEMINI_API_KEY" \
  --ports 8080 \
  --dns-name-label iago-backend \
  --restart-policy OnFailure

# 8. Aguardar e obter URL
echo "Aguardando container estar pronto..."
sleep 15

FQDN=$(az container show --resource-group iago-rg --name iago-backend --query ipAddress.fqdn -o tsv 2>/dev/null)

if [ -z "$FQDN" ]; then
    echo "❌ Falha: não foi possível obter o FQDN do container"
    exit 1
fi

echo ""
echo "✅ DEPLOYMENT CONCLUÍDO!"
echo "URL: http://${FQDN}:8080"
echo "Health: curl http://${FQDN}:8080/health"



# 🚀 Deploy do Backend IAgo no Azure

## 📋 Pré-requisitos

1. **Conta Azure** (criar em https://azure.microsoft.com/pt-br/free/)
2. **Azure CLI** instalado (https://learn.microsoft.com/pt-br/cli/azure/install-azure-cli)
3. **Docker** instalado localmente (opcional, para testes)
4. **Chave Gemini API** (variável de ambiente)

---

## ✅ Opção 1: Deploy com Azure Container Instances (Mais Rápido)

### Passo 1: Preparar no GitHub

```bash
# 1. Fazer push do código para GitHub
cd /home/kayque/Repos/IAgo
git add Dockerfile .github/workflows/deploy-azure.yml
git commit -m "chore: Add Azure deployment configuration"
git push origin main
```

### Passo 2: Configurar Secrets no GitHub

Vá para: **GitHub Repo → Settings → Secrets and variables → Actions**

Adicione:
```
GEMINI_API_KEY = seu-valor-aqui
AZURE_RESOURCE_GROUP = seu-resource-group-aqui
```

### Passo 3: Deploy Automático

O GitHub Actions fará o deploy automaticamente quando você fizer push.

**Monitorar**:
- Vá para **Actions** na repo
- Veja o workflow `Deploy to Azure Container Instances`

---

## ✅ Opção 2: Deploy Manual com Azure CLI (Mais Controle)

### Passo 1: Fazer login no Azure

```bash
az login
```

### Passo 2: Criar Resource Group (se não existir)

```bash
az group create \
  --name iago-rg \
  --location eastus
```

### Passo 3: Criar Registry (se não existir)

```bash
az acr create \
  --resource-group iago-rg \
  --name iagoregistry \
  --sku Basic
```

### Passo 4: Build e Push da Imagem Docker

```bash
# Fazer login no seu Registry
az acr login --name iagoregistry

# Build
az acr build \
  --registry iagoregistry \
  --image iago-backend:latest \
  .
```

### Passo 5: Deploy no Container Instances

```bash
az container create \
  --resource-group iago-rg \
  --name iago-backend \
  --image iagoregistry.azurecr.io/iago-backend:latest \
  --cpu 1 \
  --memory 1 \
  --registry-login-server iagoregistry.azurecr.io \
  --registry-username <seu-usuario> \
  --registry-password <sua-senha> \
  --environment-variables PORT=8080 \
  --secure-environment-variables GEMINI_API_KEY=sua-chave-aqui \
  --ports 8080 \
  --dns-name-label iago-backend
```

### Passo 6: Obter URL Pública

```bash
az container show \
  --resource-group iago-rg \
  --name iago-backend \
  --query ipAddress.fqdn
```

**Resultado**: `iago-backend.eastus.azurecontainers.io:8080`

---

## ✅ Opção 3: Deploy com App Service (Mais Fácil)

### Passo 1: Criar App Service

```bash
# Criar plano
az appservice plan create \
  --name iago-plan \
  --resource-group iago-rg \
  --sku B1 \
  --is-linux

# Criar webapp com Docker
az webapp create \
  --name iago-backend \
  --plan iago-plan \
  --resource-group iago-rg \
  --deployment-container-image-name-user iagoregistry \
  --deployment-container-image-name iago-backend:latest
```

### Passo 2: Configurar Variáveis de Ambiente

```bash
az webapp config appsettings set \
  --name iago-backend \
  --resource-group iago-rg \
  --settings \
    WEBSITES_PORT=8080 \
    GEMINI_API_KEY=sua-chave-aqui
```

### Passo 3: Obter URL

```bash
az webapp show \
  --name iago-backend \
  --resource-group iago-rg \
  --query defaultHostName
```

**Resultado**: `iago-backend.azurewebsites.net`

---

## 🔄 Atualizar URL no App Android

Após obter a URL do Azure, atualize o buildConfigField:

### Arquivo: `composeApp/build.gradle.kts`

```kotlin
defaultConfig {
    // ...
    buildConfigField("String", "IAGO_BACKEND_URL", "\"https://seu-app.azurewebsites.net\"")
    // Exemplo real:
    // buildConfigField("String", "IAGO_BACKEND_URL", "\"https://iago-backend.azurewebsites.net\"")
}
```

### Recompile:

```bash
cd /home/kayque/Repos/IAgo
./gradlew clean assembleDebug
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

---

## 📊 Monitorar Backend no Azure

### Ver Logs

```bash
# Container Instances
az container logs \
  --resource-group iago-rg \
  --name iago-backend

# App Service
az webapp log tail \
  --name iago-backend \
  --resource-group iago-rg
```

### Ver Métricas

No **Azure Portal**:
1. Vá para seu Container/App Service
2. **Monitoring → Metrics**
3. Veja CPU, Memória, Requisições

---

## 🔐 Variáveis de Ambiente no Azure

### Container Instances
```bash
az container create \
  --secure-environment-variables \
    GEMINI_API_KEY=valor \
    PORT=8080
```

### App Service
```bash
az webapp config appsettings set \
  --settings \
    GEMINI_API_KEY=valor \
    PORT=8080
```

---

## 💰 Custos Estimados

| Serviço | Custo Mensal |
|---------|-------------|
| Container Instances (1 CPU, 1GB RAM) | ~$15-20 |
| App Service (B1) | ~$13 |
| App Service (F1) | Gratuito (limitado) |

**Dica**: Use **F1 (Free)** para testes iniciais!

---

## 🧪 Testar Backend Remoto Localmente

```bash
# Obter URL (exemplo)
BACKEND_URL="https://iago-backend.azurewebsites.net"

# Testar saúde
curl $BACKEND_URL/health

# Testar chat
curl -X POST $BACKEND_URL/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Olá"}'
```

---

## ✅ Checklist de Deploy

- [ ] Criou conta Azure
- [ ] Instalou Azure CLI
- [ ] Criou Resource Group
- [ ] Fez push do código com Dockerfile
- [ ] Configurou Secrets no GitHub (opcional)
- [ ] Realizou deploy (automático ou manual)
- [ ] Obteve URL do backend
- [ ] Atualizou buildConfigField no app Android
- [ ] Recompilou APK
- [ ] Testou no celular

---

## 🔗 Referências

- [Azure Container Instances](https://learn.microsoft.com/pt-br/azure/container-instances/)
- [Azure App Service](https://learn.microsoft.com/pt-br/azure/app-service/)
- [Docker no Azure](https://learn.microsoft.com/pt-br/azure/container-instances/container-instances-using-azure-container-registry)
- [GitHub Actions para Azure](https://learn.microsoft.com/pt-br/azure/container-instances/container-instances-github-action)

---

## 🆘 Troubleshooting

### Erro: "Image not found"
```bash
# Verificar images disponíveis
az acr repository list --name iagoregistry
```

### Erro: "Permission denied"
```bash
# Obter credenciais
az acr credential show --name iagoregistry
```

### Backend não responde
```bash
# Ver logs
az container logs --resource-group iago-rg --name iago-backend

# Verificar se está rodando
az container show --resource-group iago-rg --name iago-backend
```

---

**Próximo Passo**: Escolha uma opção acima e comece o deploy!

Recomendo **Opção 2 (Azure CLI)** para mais controle, ou **Opção 3 (App Service)** para simplicidade.



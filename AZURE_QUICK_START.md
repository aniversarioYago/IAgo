# 🚀 IAgo com Azure - Guia Rápido

## ✅ O que foi configurado

1. ✅ **Dockerfile** - Para containerizar o backend
2. ✅ **GitHub Actions** - CI/CD automático para Azure
3. ✅ **Guia de Deploy** - Documentação completa (AZURE_DEPLOYMENT_GUIDE.md)
4. ✅ **Config File** - backend.config.properties para gerenciar URLs

---

## 🚀 3 Passos para Começar

### Passo 1: Preparar no GitHub

```bash
cd /home/kayque/Repos/IAgo

git add Dockerfile .github/workflows/deploy-azure.yml backend.config.properties
git commit -m "chore: Add Azure deployment configuration"
git push origin main
```

### Passo 2: Criar Conta/Recursos no Azure

Vá para: https://azure.microsoft.com/pt-br/free/

- Crie uma conta (R$ 200 crédito grátis por 30 dias)
- Depois use F1 (Free) do App Service

### Passo 3: Fazer Deploy

**Opção A: Automático (GitHub Actions)**
- Vai fazer deploy automaticamente quando você fizer push

**Opção B: Manual (Azure CLI)**
```bash
# Siga o guia em AZURE_DEPLOYMENT_GUIDE.md (Opção 2)
```

---

## 📍 URLs Esperadas

Após o deploy, você terá uma URL como:

- **App Service**: `https://iago-backend.azurewebsites.net`
- **Container Instances**: `https://iago-backend.azurecontainers.io:8080`

---

## 🔄 Atualizar App Android com Nova URL

### Arquivo: `composeApp/build.gradle.kts`

```kotlin
defaultConfig {
    buildConfigField("String", "IAGO_BACKEND_URL", "\"https://sua-url-azure.azurewebsites.net\"")
}
```

Depois recompile:
```bash
./gradlew clean assembleDebug
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

---

## 📚 Recursos Criados

| Arquivo | Propósito |
|---------|-----------|
| `Dockerfile` | Containerizar backend |
| `.github/workflows/deploy-azure.yml` | CI/CD automático |
| `AZURE_DEPLOYMENT_GUIDE.md` | Guia completo (39 KiB) |
| `backend.config.properties` | Gerenciar URLs |

---

## 🆘 Próximas Etapas

1. ✅ Você está aqui: Leu o guia rápido
2. ⏭️ Próximo: Ler `AZURE_DEPLOYMENT_GUIDE.md` para detalhes
3. ⏭️ Depois: Fazer deploy escolhendo Opção 2 ou 3
4. ⏭️ Por fim: Atualizar URL no app e recompilar

---

## 💡 Recomendação

Para começar **rápido e gratuito**:

1. Use **App Service - F1 (Free)** no Azure
2. Faça deploy **manual com Azure CLI** (Opção 2)
3. Teste tudo funcionando
4. Depois suba para plano pago se necessário

---

## ✅ Próximo: Leia o guia completo

Abra: `AZURE_DEPLOYMENT_GUIDE.md`

Ele tem:
- Instruções passo a passo
- Comandos prontos para copiar/colar
- Explicações de cada opção
- Troubleshooting

**Sucesso!** 🎉



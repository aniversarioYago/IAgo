# 🚀 IAgo - Deploy GitHub Pages

## URL do GitHub Pages

```
https://aniversarioyago.github.io/IAgo/
```

---

## ✅ Alterações Realizadas

### 1. Backend CORS ✅
Atualizado para aceitar requisições do GitHub Pages:
```kotlin
origin.startsWith("https://aniversarioyago.github.io") ||
origin.contains("github.io")
```

### 2. Frontend index.html ✅
Agora detecta o ambiente automaticamente e configura a URL do backend

### 3. Script de Deploy ✅
Criado `deploy_github_pages.sh` para compilar e fazer deploy

---

## 🔧 Configuração Necessária

### Pré-requisitos
1. Repositório GitHub criado: `aniversarioyago/IAgo`
2. GitHub Pages habilitado (Settings → Pages)
3. Branch selecionada: `gh-pages` ou `main`

### Habilitar GitHub Pages
1. Acesse: https://github.com/aniversarioyago/IAgo
2. Settings → Pages
3. Source: Deploy from a branch
4. Branch: `gh-pages` (ou `main` se preferir)
5. Salvar

---

## 🚀 Como Fazer Deploy

### Opção 1: Script Automático

```bash
cd /home/kayque/Repos/IAgo
chmod +x deploy_github_pages.sh
./deploy_github_pages.sh
```

### Opção 2: Manual

```bash
cd /home/kayque/Repos/IAgo

# Compilar
./gradlew wasmJsBrowserDistribution \
  -PIAGO_BACKEND_URL="https://seu-backend.com:8081"

# Clonar o repositório com gh-pages
git clone --branch gh-pages https://github.com/aniversarioyago/IAgo.git deploy

# Copiar arquivos compilados
cp -r composeApp/build/dist/wasmJs/productionExecutable/* deploy/

# Commitar e fazer push
cd deploy
git add -A
git commit -m "Deploy IAgo"
git push origin gh-pages
```

### Opção 3: GitHub Actions (Automático)

Criar arquivo `.github/workflows/deploy.yml`:

```yaml
name: Deploy to GitHub Pages

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup JDK
        uses: actions/setup-java@v3
        with:
          java-version: '21'
      
      - name: Build
        run: |
          ./gradlew wasmJsBrowserDistribution \
            -PIAGO_BACKEND_URL="https://seu-backend.com:8081"
      
      - name: Deploy
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./composeApp/build/dist/wasmJs/productionExecutable
```

---

## 🌐 Backend Remoto

Como o GitHub Pages é estático, o backend DEVE estar em um servidor remoto:

### Opções:

1. **Render.com** (Recomendado - Grátis)
   ```bash
   ./build_android.sh debug "https://seu-app.onrender.com"
   ```

2. **Railway.app**
   ```bash
   ./build_android.sh debug "https://seu-app.railway.app"
   ```

3. **Fly.io**
   ```bash
   ./build_android.sh debug "https://seu-app.fly.dev"
   ```

4. **Seu próprio servidor**
   ```bash
   ./build_android.sh debug "https://seu-servidor.com:8081"
   ```

---

## 🔐 Configurar Backend Remoto

### Variável de Ambiente

Atualizar `index.html`:
```javascript
window.IAGO_BACKEND_URL = "https://seu-backend-remoto.com:8081";
```

Ou no `build.gradle.kts`:
```bash
./gradlew wasmJsBrowserDistribution \
  -PIAGO_BACKEND_URL="https://seu-backend-remoto.com:8081"
```

---

## 📋 Checklist GitHub Pages

- [ ] Repositório criado em GitHub
- [ ] GitHub Pages habilitado
- [ ] Branch `gh-pages` criada ou `main` selecionada
- [ ] Backend remoto configurado
- [ ] Backend URL atualizada no index.html
- [ ] Compilado com deploy_github_pages.sh
- [ ] Arquivos enviados para GitHub
- [ ] Website acessível em https://aniversarioyago.github.io/IAgo/

---

## 🧪 Testar Deploy

Após fazer push:

```bash
# Aguardar 2-5 minutos

# Acessar a URL
curl https://aniversarioyago.github.io/IAgo/

# Ou abrir no navegador
open https://aniversarioyago.github.io/IAgo/
```

---

## 🐛 Troubleshooting

### "Página não encontrada"
- Certifique-se que GitHub Pages está habilitado
- Verifique se a branch está correta
- Aguarde 5 minutos após push

### "Falha ao conectar com backend"
- Certifique-se que backend remoto está rodando
- Verifique CORS no backend
- Verifique URL em index.html

### "Arquivo não encontrado"
- Verifique se composeApp.js foi copiado
- Verifique se todos os arquivos foram enviados
- Git push foi bem-sucedido?

---

## 📱 Versão Android com Backend Remoto

```bash
./build_android.sh debug "https://seu-backend-remoto.com:8081"
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

---

## 🔄 Atualizar Deploy

Sempre que fizer alterações:

```bash
# 1. Compilar
./gradlew wasmJsBrowserDistribution \
  -PIAGO_BACKEND_URL="https://seu-backend-remoto.com:8081"

# 2. Commitar e fazer push
git add -A
git commit -m "Atualizar IAgo"
git push origin gh-pages
```

---

## ✨ Resumo

**GitHub Pages URL**: https://aniversarioyago.github.io/IAgo/

**Próximas etapas**:
1. Configurar backend remoto
2. Atualizar URL no código
3. Executar `./deploy_github_pages.sh`
4. Fazer push para GitHub
5. Acessar a URL

**Status**: ✅ Pronto para fazer deploy!


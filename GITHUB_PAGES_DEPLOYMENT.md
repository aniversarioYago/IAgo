# 🚀 GitHub Pages Deployment - Setup Completo

## URL do GitHub Pages

```
https://aniversarioyago.github.io/IAgo/
```

---

## ✅ O que foi Feito

### 1. Backend CORS Atualizado ✅
**Arquivo**: `backend/src/main/kotlin/io/github/iago/backend/Main.kt`

Função `isOriginAllowed` agora aceita:
- `https://aniversarioyago.github.io`
- Qualquer domínio `github.io`

### 2. Frontend HTML Dinâmico ✅
**Arquivo**: `composeApp/src/webMain/resources/index.html`

Detecta ambiente automaticamente:
- Localhost: `http://localhost:8081`
- GitHub Pages: URL remota (configurável)
- Outro servidor: `http://localhost:8081`

---

## 🛠️ Scripts Criados

1. **deploy_github_pages.sh** - Compila frontend WASM
2. **prepare_backend_deploy.sh** - Prepara backend para servidor remoto

---

## 📋 Próximos Passos

### 1. Criar Repositório GitHub
```
https://github.com/new
Nome: IAgo
Dono: aniversarioyago
```

### 2. Habilitar GitHub Pages
Settings → Pages → Source: `gh-pages` branch

### 3. Compilar Frontend
```bash
cd /home/kayque/Repos/IAgo
./deploy_github_pages.sh
```

### 4. Fazer Push para GitHub
```bash
git clone --branch gh-pages https://github.com/aniversarioyago/IAgo.git deploy
cd deploy
cp -r ../composeApp/build/dist/wasmJs/productionExecutable/* .
git add -A && git commit -m "Deploy IAgo" && git push origin gh-pages
```

### 5. Deploy Backend (Render.com recomendado)
- URL: https://render.com
- Conectar repositório GitHub
- Build: `./gradlew backend:build`
- Start: `PORT=$PORT GEMINI_API_KEY=$GEMINI_API_KEY ./gradlew backend:run`

### 6. Atualizar URL do Backend
Editar `index.html`:
```javascript
window.IAGO_BACKEND_URL = "https://seu-backend-remoto.com:8081";
```

---

## 📊 URLs Finais

| Serviço | URL |
|---------|-----|
| Frontend | https://aniversarioyago.github.io/IAgo/ |
| Backend | https://seu-backend-remoto.com:8081 |

---

## ✅ Checklist

- [ ] Repositório criado
- [ ] GitHub Pages habilitado
- [ ] Frontend compilado
- [ ] Frontend enviado para GitHub
- [ ] Backend deployado em servidor remoto
- [ ] URL do backend atualizada
- [ ] Testes realizados

---

## 🎉 Status

**Pronto para fazer deploy!**


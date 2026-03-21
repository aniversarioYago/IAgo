# 🚀 IAgo - GitHub Pages Quick Commands

## Setup Rápido - Copie e Cole

### 1. Criar repositório (na web em github.com)
```
Nome: IAgo
Dono: aniversarioyago
Público
```

### 2. Compilar Frontend
```bash
cd /home/kayque/Repos/IAgo
./deploy_github_pages.sh
```

### 3. Fazer Deploy no GitHub
```bash
# Clonar gh-pages
git clone --branch gh-pages https://github.com/aniversarioyago/IAgo.git deploy
cd deploy

# Copiar arquivos compilados
cp -r /home/kayque/Repos/IAgo/composeApp/build/dist/wasmJs/productionExecutable/* .

# Commitar e fazer push
git add -A
git commit -m "Deploy IAgo - Frontend"
git push origin gh-pages
```

### 4. Testar Frontend
```bash
# Aguardar 2-5 minutos e acessar
open https://aniversarioyago.github.io/IAgo/

# Ou via curl
curl https://aniversarioyago.github.io/IAgo/
```

### 5. Deploy Backend - Render.com

#### Criar conta e conectar repositório
1. https://render.com
2. New → Web Service
3. Conectar GitHub
4. Build: `./gradlew backend:build`
5. Start: `PORT=$PORT GEMINI_API_KEY=$GEMINI_API_KEY ./gradlew backend:run`
6. Environment: 
   - PORT: 10000 (ou qualquer)
   - GEMINI_API_KEY: AIzaSyBdCccCTRoAJW7ss58Eclc7IwypNTphS0o

#### URL será algo como
```
https://seu-app-name.onrender.com:8081
```

### 6. Atualizar URL do Backend

**Editar arquivo**: `composeApp/src/webMain/resources/index.html`

```javascript
// Alterar de:
window.IAGO_BACKEND_URL = "http://localhost:8081";

// Para:
window.IAGO_BACKEND_URL = "https://seu-app-name.onrender.com:8081";
```

### 7. Recompilar e Fazer Push
```bash
cd /home/kayque/Repos/IAgo
./deploy_github_pages.sh

# Copiar para deploy
cd deploy
cp -r /home/kayque/Repos/IAgo/composeApp/build/dist/wasmJs/productionExecutable/* .

# Push
git add -A
git commit -m "Update backend URL"
git push origin gh-pages
```

### 8. Testar Integração
```bash
# Frontend deve estar acessível
open https://aniversarioyago.github.io/IAgo/

# Backend deve estar respondendo
curl https://seu-app-name.onrender.com:8081/health

# Chat deve funcionar
curl -X POST https://seu-app-name.onrender.com:8081/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Olá"}'
```

### 9. Build Android com Backend Remoto
```bash
cd /home/kayque/Repos/IAgo
./build_android.sh debug "https://seu-app-name.onrender.com:8081"

# Instalar
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

---

## URLs Finais

```
Frontend: https://aniversarioyago.github.io/IAgo/
Backend:  https://seu-app-name.onrender.com:8081
Health:   https://seu-app-name.onrender.com:8081/health
```

---

## Troubleshooting Rápido

### Frontend não carrega
```bash
# Aguardar 5 minutos após push
# Limpar cache: Ctrl+Shift+Delete
# Verificar: Settings → Pages (GitHub)
```

### Backend não responde
```bash
# Verificar saúde
curl https://seu-app-name.onrender.com:8081/health

# Verificar logs (Render Dashboard)
# Verificar ambiente variables
```

### Chat falha
```bash
# Verificar URL em index.html
# Verificar GEMINI_API_KEY
# Testar endpoint direto:
curl -X POST https://seu-app-name.onrender.com:8081/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Teste"}'
```

---

✅ Pronto!


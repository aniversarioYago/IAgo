# ✅ SOLUÇÃO - Erro "socket failed: EPERM"

## 🔴 Problema
```
socket failed: EPERM (Operation not permitted)
```

Ocorria ao tentar conectar ao backend no Android.

## ✅ Causa Identificada
Faltava a permissão de INTERNET no AndroidManifest.xml

## ✅ Solução Aplicada

### Arquivo: `AndroidManifest.xml`
**Localização**: `composeApp/src/androidMain/AndroidManifest.xml`

**Mudança**: Adicionada permissão de INTERNET
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 🚀 Para Instalar o Novo APK

```bash
cd /home/kayque/Repos/IAgo

# Instalar o novo APK compilado
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

**O novo APK está pronto em**: 
```
composeApp/build/outputs/apk/debug/composeApp-debug.apk (16 MB)
```

---

## 🧪 Teste Agora

Após instalar:

1. **Abra o app**
2. **Digite uma mensagem no chat**
3. **Clique "Enviar"**
4. **Deve funcionar sem o erro EPERM!**

---

## 📋 Checklist

- [x] Adicionada permissão INTERNET no AndroidManifest.xml
- [x] APK recompilado (16 MB)
- [x] Network security config mantido para cleartext
- [x] BuildConfig com URL backend: `http://localhost:8081`

---

## ℹ️ Configuração Atual

```
✅ Android Manifest: Permissão INTERNET adicionada
✅ Network Config: Permite cleartext para localhost
✅ Backend URL: http://localhost:8081
✅ Build Config: IAGO_BACKEND_URL configurado
✅ HttpClient: ktor-client-android otimizado
```

---

**Status**: ✅ **APK PRONTO PARA INSTALAR**

O erro EPERM deve estar resolvido agora!



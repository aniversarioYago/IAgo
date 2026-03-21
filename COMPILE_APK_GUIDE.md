# 🔧 Instruções para Compilar APK Válido

## ❌ Problema
APK pode estar inválido se:
1. Não foi compilado completamente
2. Falha de assinatura
3. Arquivo corrompido

---

## ✅ Solução Completa

### Passo 1: Limpeza Completa
```bash
cd /home/kayque/Repos/IAgo

# Matar todos os processos Java
pkill -9 java

# Limpar caches
rm -rf .gradle
rm -rf composeApp/build
rm -rf build

# Gradle clean
./gradlew clean
```

### Passo 2: Compilar Debug APK (Melhor para Teste)
```bash
cd /home/kayque/Repos/IAgo

# Compilar
./gradlew assembleDebug

# Verificar se foi gerado
ls -lh composeApp/build/outputs/apk/debug/composeApp-debug.apk

# Tamanho esperado: 50-80 MB
```

### Passo 3: Instalar no Device
```bash
# Conectar device/emulador via adb
adb devices

# Instalar APK
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk

# Se houver erro, forçar:
adb uninstall io.github.iago.iago
adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

---

## 🔍 Verificar se APK é Válido

```bash
# Checar arquivo APK
file composeApp/build/outputs/apk/debug/composeApp-debug.apk

# Deve mostrar: Zip archive data

# Ver conteúdo
unzip -l composeApp/build/outputs/apk/debug/composeApp-debug.apk | head -20

# Deve ter: classes.dex, resources.arsc, AndroidManifest.xml, etc.
```

---

## ⚠️ Se Ainda Não Funcionar

### Aumentar tempo de espera
```bash
# Às vezes gradle demora muito
timeout 600 ./gradlew assembleDebug

# Esperar até terminar completamente
```

### Testar compilação em partes
```bash
# Apenas compilar (não criar APK)
./gradlew compileDebug

# Se compilar, então ir para APK
./gradlew assembleDebug
```

### Limpar gradle.properties
```bash
cd /home/kayque/Repos/IAgo

# Ver arquivo
cat gradle.properties

# Pode precisar adicionar se não existir:
# org.gradle.jvmargs=-Xmx4096m -XX:+HeapDumpOnOutOfMemoryError
```

---

## 📊 Checklist de Compilação

- [ ] Java está instalado: `java -version`
- [ ] Gradle wrapper existe: `ls gradlew`
- [ ] Android SDK está configurado
- [ ] Nenhum processo gradle rodando: `ps aux | grep gradle`
- [ ] Disco tem espaço: `df -h /home`
- [ ] Permissões corretas: `ls -ld composeApp/`

---

## 🎯 Resultado Esperado

Após `./gradlew assembleDebug`:

```
✅ BUILD SUCCESSFUL

Arquivo gerado:
  composeApp/build/outputs/apk/debug/composeApp-debug.apk (55 MB aprox)

Pronto para instalar:
  adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

---

## 📝 Notas Importantes

1. **Primeira compilação demora**: 5-10 minutos
2. **Compilações seguintes**: 2-3 minutos
3. **RAM mínima**: 8GB (aumentar com `-Xmx4096m`)
4. **Espaço disco**: Mínimo 5GB livres

---

**Siga os passos acima e o APK será válido!**

Se continuar não funcionando, envie:
```bash
./gradlew assembleDebug 2>&1 | tail -100
```



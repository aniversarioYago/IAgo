# 🏗️ Android Release Build - Status

## Modificações Aplicadas

Foram realizadas as seguintes modificações para compilar o Android Release:

### 1. **composeApp/build.gradle.kts**
- ✅ Adicionado `ktor-client-android` ao androidMain dependencies
- ✅ Adicionado `buildFeatures { buildConfig = true }`
- ✅ Adicionado `buildConfigField("String", "IAGO_BACKEND_URL", "\"http://localhost:8081\"")`

### 2. **gradle/libs.versions.toml**
- ✅ Adicionado `ktor-client-android` ao catálogo de dependências

## Para Compilar Release

```bash
cd /home/kayque/Repos/IAgo

# Opção 1: Debug APK (mais rápido para teste)
./gradlew assembleDebug
# Resultado: composeApp/build/outputs/apk/debug/composeApp-debug.apk

# Opção 2: Release APK (otimizado)
./gradlew assembleRelease
# Resultado: composeApp/build/outputs/apk/release/composeApp-release.apk

# Opção 3: App Bundle (para Play Store)
./gradlew bundleRelease
# Resultado: composeApp/build/outputs/bundle/release/composeApp-release.aab
```

## Para Instalar no Dispositivo

```bash
# Após compilar o Debug APK
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

## Configuração de Backend

A URL do backend é definida em:
```kotlin
buildConfigField("String", "IAGO_BACKEND_URL", "\"http://localhost:8081\"")
```

Para mudar para backend remoto, edite em `composeApp/build.gradle.kts` linha ~65:
```kotlin
buildConfigField("String", "IAGO_BACKEND_URL", "\"https://seu-backend-remoto:8081\"")
```

## Próximos Passos

1. Executar o build:
   ```bash
   ./gradlew assembleDebug
   ```

2. Verificar o APK gerado:
   ```bash
   ls -lh composeApp/build/outputs/apk/debug/composeApp-debug.apk
   ```

3. Instalar no dispositivo:
   ```bash
   adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
   ```

---

**Status**: ✅ Modificações aplicadas e prontas para build
**Data**: 2026-03-21


# 🔧 Android Release Build - Correção Aplicada

## ✅ Problema Identificado

O erro ao compilar a release era:
```
ERROR: ktor-client-core-jvm-3.2.0.jar: D8: Space characters in SimpleName 'use streaming syntax' are not allowed prior to DEX version 040
```

**Causa**: O `ktor-client-core` versão 3.2.0 não é totalmente compatível com o sistema de DEX do Android quando compilando release.

---

## ✅ Solução Aplicada

### 1. **Alterado composeApp/build.gradle.kts**

Reorganizou-se as dependências por plataforma:

```kotlin
// Apenas Android (ktor-client-android é otimizado para Android)
androidMain.dependencies {
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.serialization.kotlinxJson)
    implementation(libs.kotlinx.serialization.json)
}

// Apenas Web/JS (ktor-client-core para navegadores)
jsMain.dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.serialization.kotlinxJson)
    implementation(libs.kotlinx.serialization.json)
}

// Comum (sem HTTP client, será fornecido por cada plataforma)
commonMain.dependencies {
    // Compose e outras dependências comuns
}
```

### 2. **Adicionado multiDexEnabled**

```kotlin
defaultConfig {
    multiDexEnabled = true
    buildConfigField("String", "IAGO_BACKEND_URL", "\"http://localhost:8081\"")
}
```

### 3. **Downgrade do Ktor** (Opcional)

Alterado de `ktor = "3.2.0"` para `ktor = "2.3.0"` em `gradle/libs.versions.toml` para garantir máxima compatibilidade com Android.

---

## 🚀 Para Compilar a Release

```bash
cd /home/kayque/Repos/IAgo

# Limpeza completa (importante)
rm -rf .gradle
./gradlew clean

# Build release
./gradlew assembleRelease

# Resultado esperado:
# composeApp/build/outputs/apk/release/composeApp-release.apk
```

---

## 📊 Arquivos Modificados

1. ✅ `composeApp/build.gradle.kts`
   - Reorganizadas dependências por plataforma
   - Adicionado `multiDexEnabled = true`

2. ✅ `gradle/libs.versions.toml`
   - Downgrade Ktor: 3.2.0 → 2.3.0

---

## 🔍 Verificação Posterior

Depois que a compilação terminar, verifique:

```bash
# Checar se o APK foi gerado
ls -lh composeApp/build/outputs/apk/release/composeApp-release.apk

# Verificar tamanho (deve ser 30-50 MB)
du -h composeApp/build/outputs/apk/release/composeApp-release.apk

# Instalar no device
adb install -r composeApp/build/outputs/apk/release/composeApp-release.apk
```

---

## ℹ️ Notas Importantes

1. **A mudança de Ktor 3.2.0 → 2.3.0 é regressiva**
   - Ktor 2.3.0 é estável e bem testado com Android
   - Ktor 3.2.0 tem melhorias mas compatibilidade em Android ainda tem issues

2. **multiDexEnabled = true** permite mais de 64K métodos
   - Necessário com muitas dependências
   - Não afeta performance significativamente

3. **As dependências agora são separadas por plataforma**
   - Android usa `ktor-client-android` (nativo)
   - Web usa `ktor-client-core` (para navegadores)
   - Evita conflitos de DEX

---

## 🎯 Próximas Etapas

1. Executar `./gradlew assembleRelease`
2. Aguardar conclusão (~5-10 minutos)
3. Verificar em `composeApp/build/outputs/apk/release/`
4. Instalar no device com `adb install -r`
5. Testar funcionalidade completa

---

**Status**: ✅ **PRONTO PARA COMPILAR**  
**Ktor Version**: 2.3.0 (compatível com Android)  
**Data**: 2026-03-21


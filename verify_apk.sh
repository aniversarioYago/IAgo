#!/bin/bash

echo "🔍 Verificando Status do APK"
echo ""

cd /home/kayque/Repos/IAgo

# 1. Verificar se APK existe
if [ -f "composeApp/build/outputs/apk/debug/composeApp-debug.apk" ]; then
    echo "✅ APK Encontrado"
    ls -lh composeApp/build/outputs/apk/debug/composeApp-debug.apk
    echo ""

    # 2. Verificar se é um arquivo ZIP válido (APK é um ZIP)
    echo "🔍 Verificando integridade do APK..."
    if unzip -t composeApp/build/outputs/apk/debug/composeApp-debug.apk > /dev/null 2>&1; then
        echo "✅ APK é um arquivo ZIP válido"
        echo ""

        # 3. Verificar conteúdo crítico
        echo "📦 Verificando conteúdo crítico..."
        CLASSES=$(unzip -l composeApp/build/outputs/apk/debug/composeApp-debug.apk | grep -c "classes.dex")
        MANIFEST=$(unzip -l composeApp/build/outputs/apk/debug/composeApp-debug.apk | grep -c "AndroidManifest.xml")
        RESOURCES=$(unzip -l composeApp/build/outputs/apk/debug/composeApp-debug.apk | grep -c "resources.arsc")

        if [ $CLASSES -gt 0 ] && [ $MANIFEST -gt 0 ] && [ $RESOURCES -gt 0 ]; then
            echo "✅ Conteúdo crítico presente:"
            echo "   - classes.dex: SIM"
            echo "   - AndroidManifest.xml: SIM"
            echo "   - resources.arsc: SIM"
            echo ""
            echo "🎉 APK VÁLIDO E PRONTO PARA INSTALAR!"
        else
            echo "❌ Faltam arquivos críticos"
            echo "   - classes.dex: $([ $CLASSES -gt 0 ] && echo 'SIM' || echo 'NÃO')"
            echo "   - AndroidManifest.xml: $([ $MANIFEST -gt 0 ] && echo 'SIM' || echo 'NÃO')"
            echo "   - resources.arsc: $([ $RESOURCES -gt 0 ] && echo 'SIM' || echo 'NÃO')"
        fi
    else
        echo "❌ APK não é um arquivo ZIP válido"
    fi
else
    echo "❌ APK não encontrado"
    echo ""
    echo "📊 Status da compilação:"

    if grep -q "BUILD SUCCESSFUL" /tmp/build.log 2>/dev/null; then
        echo "✅ Compilação terminou com sucesso"
    else
        echo "❌ Compilação pode ter falhado"
    fi

    echo ""
    echo "📂 Diretório de saída:"
    ls -la composeApp/build/outputs/ 2>/dev/null || echo "Não existe"
fi

echo ""
echo "📝 Para instalar:"
echo "   adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk"


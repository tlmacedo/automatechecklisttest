#!/bin/bash

# Script para compilar e executar testes instrumentados via Gradle
# Use este script quando criar um teste novo ou modificar o código, pois ele recompila o projeto.
# Uso: ./run_gradle_test.sh NomeDaClasse
# Uso: ./run_gradle_test.sh NomeDaClasse#NomeDoMetodo

PACKAGE_BASE="com.samsung.requirements.automatechecklisttest"
TEST_PACKAGE_PATH="${PACKAGE_BASE}.tests"

TARGET=$1

if [ -z "$TARGET" ]; then
    echo "Erro: Informe o nome da classe de teste."
    echo "Exemplo: ./run_gradle_test.sh NfcTest"
    exit 1
fi

# Se não contiver ponto, assume-se que é apenas o nome da classe e adiciona o pacote padrão
if [[ "$TARGET" != *.* ]]; then
    TARGET="${TEST_PACKAGE_PATH}.${TARGET}"
fi

echo "Compilando e executando via Gradle: $TARGET"
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=$TARGET
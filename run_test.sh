#!/bin/bash

# Script para executar testes específicos via ADB
# Uso: ./run_test.sh NomeDaClasse
# Exemplo: ./run_test.sh MobileNetworkIconTest

CLASS_NAME=$1

if [ -z "$CLASS_NAME" ]; then
    echo "Erro: Nome da classe não informado."
    echo "Uso: ./run_test.sh <NomeDaClasse>"
    echo "Exemplo: ./run_test.sh RegulatoryInfoTest"
    exit 1
fi

FULL_CLASS_PATH="com.samsung.requirements.automatechecklisttest.tests.$CLASS_NAME"

echo "Executando teste: $FULL_CLASS_PATH ..."

adb shell am instrument -w -e class "$FULL_CLASS_PATH" com.samsung.requirements.automatechecklisttest.test/androidx.test.runner.AndroidJUnitRunner

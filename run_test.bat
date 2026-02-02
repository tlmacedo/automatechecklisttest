@echo off
:: Script para executar testes específicos via ADB no Windows
:: Uso: run_test.bat NomeDaClasse
:: Exemplo: run_test.bat MobileNetworkIconTest

set CLASS_NAME=%1

if "%CLASS_NAME%"=="" (
    echo Erro: Nome da classe nao informado.
    echo Uso: run_test.bat ^<NomeDaClasse^>
    echo Exemplo: run_test.bat RegulatoryInfoTest
    exit /b 1
)

set FULL_CLASS_PATH=com.samsung.requirements.automatechecklisttest.tests.%CLASS_NAME%

echo Executando teste: %FULL_CLASS_PATH% ...

adb shell am instrument -w -e class "%FULL_CLASS_PATH%" com.samsung.requirements.automatechecklisttest.test/androidx.test.runner.AndroidJUnitRunner

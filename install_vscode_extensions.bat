@echo off
rem Este script instala uma lista de extensoes do VSCode no Windows.
rem Salve este arquivo em outra maquina e execute-o.

echo "Instalando extensoes do VSCode..."

call code --install-extension dotjoshjohnson.xml
call code --install-extension eamodio.gitlens
call code --install-extension fwcd.kotlin
call code --install-extension google.geminicodeassist
call code --install-extension mechatroner.rainbow-csv
call code --install-extension ms-vscode.anycode-kotlin
call code --install-extension redhat.java
call code --install-extension saoudrizwan.claude-dev
call code --install-extension vscjava.vscode-gradle
call code --install-extension vscjava.vscode-java-debug
call code --install-extension vscjava.vscode-java-dependency
call code --install-extension vscjava.vscode-java-pack
call code --install-extension vscjava.vscode-java-test
call code --install-extension vscjava.vscode-maven

echo "Instalacao de extensoes concluida!"
pause
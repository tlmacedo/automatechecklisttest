#!/bin/bash
# Este script instala uma lista de extensões do VSCode.
# Copie este arquivo para outra máquina e execute-o para replicar o ambiente.

echo "Instalando extensões do VSCode..."

code --install-extension dotjoshjohnson.xml
code --install-extension eamodio.gitlens
code --install-extension fwcd.kotlin
code --install-extension google.geminicodeassist
code --install-extension mechatroner.rainbow-csv
code --install-extension ms-vscode.anycode-kotlin
code --install-extension redhat.java
code --install-extension saoudrizwan.claude-dev
code --install-extension vscjava.vscode-gradle
code --install-extension vscjava.vscode-java-debug
code --install-extension vscjava.vscode-java-dependency
code --install-extension vscjava.vscode-java-pack
code --install-extension vscjava.vscode-java-test
code --install-extension vscjava.vscode-maven

echo "Instalação de extensões concluída!"
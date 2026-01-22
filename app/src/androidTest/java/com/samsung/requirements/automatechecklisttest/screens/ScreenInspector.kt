package com.samsung.requirements.automatechecklisttest.screens

import android.util.Log
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

class ScreenInspector(private val device: UiDevice, private val tag: String = "ScreenInspector") {

    companion object {
        private const val DEFAULT_TIMEOUT = 5000L // 5 segundos
    }

    /**
     * Encontra todos os elementos visíveis na tela e imprime suas propriedades.
     * @param packageName Opcional: Filtra elementos apenas do pacote especificado.
     */
    fun logAllVisibleElements(packageName: String? = null) {
        Log.d(tag, "--- Iniciando inspeção da tela ---")
        println("--- Iniciando inspeção da tela ---")
        if (packageName != null) {
            Log.d(tag, "Filtro de pacote aplicado: $packageName")
            println("Filtro de pacote aplicado: $packageName")
            // Espera até que o pacote esteja em primeiro plano
            device.wait(Until.hasObject(By.pkg(packageName).depth(0)), DEFAULT_TIMEOUT)
        } else {
            // Pequena espera para garantir que a tela esteja estável
            Thread.sleep(1000)
        }

        val rootElements = device.findObjects(By.depth(0)) // Pega os nós raiz (janelas)

        if (rootElements.isEmpty()) {
            Log.d(tag, "Nenhum elemento raiz encontrado na tela.")
            return
        }

        Log.d(tag, "Número de janelas/raízes encontradas: ${rootElements.size}")

        rootElements.forEachIndexed { index, rootElement ->
            Log.d(tag, "--- Janela/Raiz ${index + 1} ---")
            // Se um nome de pacote foi fornecido e o elemento raiz não pertence a ele, pule.
            // Nota: UiObject2 não tem um getPackageName() direto para a janela em si,
            // mas podemos verificar os filhos ou assumir que o primeiro filho relevante tem o pacote.
            // Para simplificar, vamos iterar se não houver filtro ou se for uma sobreposição do sistema.
            printElementHierarchy(rootElement, 0, packageName)
        }
        Log.d(tag, "--- Fim da inspeção da tela ---")
    }

    private fun printElementHierarchy(element: UiObject2?, depth: Int, targetPackageName: String?) {
        if (element == null) return

        // Filtrar por pacote se especificado
        if (targetPackageName != null && element.applicationPackage != targetPackageName) {
            // Se o elemento não pertence ao pacote alvo, não o imprima
            // e não explore seus filhos, a menos que seja um container genérico
            // que possa ter filhos do pacote alvo (complexo de determinar sem mais contexto).
            // Para uma inspeção geral, podemos decidir pular ou continuar com os filhos.
            // Por enquanto, vamos pular a impressão deste elemento específico.
            // return // Descomente se quiser pular completamente os nós de outros pacotes
        }

        val indent = "  ".repeat(depth)
        val properties = mutableListOf<String>()
        properties.add("Texto: '${element.text}'")
        if (element.resourceName != null) properties.add("ID Recurso: '${element.resourceName}'")
        if (element.contentDescription != null) properties.add("Desc Conteúdo: '${element.contentDescription}'")
        properties.add("Classe: '${element.className}'")
        if (element.applicationPackage != null) properties.add("Pacote: '${element.applicationPackage}'")
        properties.add("Clicável: ${element.isClickable}")
        properties.add("Habilitado: ${element.isEnabled}")
        properties.add("Focável: ${element.isFocusable}")
        properties.add("Focado: ${element.isFocused}")
        properties.add("Scrollable: ${element.isScrollable}")
        properties.add("Checável: ${element.isCheckable}")
        properties.add("Checado: ${element.isChecked}")
        properties.add("Visível Bounds: ${element.visibleBounds}")


        Log.d(tag, "$indent ${properties.joinToString(", ")}")

        element.children?.forEach { child ->
            printElementHierarchy(child, depth + 1, targetPackageName)
        }
    }

    /**
     * Imprime a hierarquia de uma janela específica.
     * Normalmente chamado internamente, mas pode ser útil.
     */
    fun dumpWindowHierarchy(windowRoot: UiObject2) {
        Log.d(tag, "--- Dump da Hierarquia da Janela ---")
        printElementHierarchy(windowRoot, 0, null)
        Log.d(tag, "--- Fim do Dump da Hierarquia da Janela ---")
    }
}
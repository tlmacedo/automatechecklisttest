package com.samsung.requirements.automatechecklisttest.tests

import android.util.Log
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import com.samsung.requirements.automatechecklisttest.R
import com.samsung.requirements.automatechecklisttest.base.BaseTest
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Teste para obter o modelo do dispositivo através das Configurações do sistema.
 */
class DeviceModelTest : BaseTest() {

    @Test
    fun getDeviceModelFromSettings() {
        // 1. Inicia a activity de Configurações
        launchActivity("com.android.settings", "com.android.settings.Settings")

        // 2. Clica em "Sobre o telefone" (traduzido automaticamente)
        scrollAndClick(R.string.about_phone)

        // 3. Aguarda a tela "Sobre o telefone" carregar (traduzido automaticamente)
        waitVisible(R.string.model_name, DEFAULT_TIMEOUT)
        
        screenInspector.logAllVisibleElements("com.android.settings")

        // 4. Localiza o rótulo e extrai o nome do modelo
        val modelLabel = waitAndFindObject(By.text(appContext.getString(R.string.model_name)))
        val modelName = extractModelName(modelLabel)
        
        Log.i("DeviceModelTest", "Nome do modelo detectado: $modelName")
        assertNotNull("O nome do modelo não foi encontrado na tela 'Sobre o telefone'", modelName)
    }

    private fun extractModelName(label: UiObject2): String? {
        return try {
            val parent = label.parent
            val summary = parent?.findObject(By.res("android:id/summary"))
            if (summary != null && summary.text.isNotBlank()) return summary.text

            val siblings = parent?.children ?: emptyList()
            val labelIndex = siblings.indexOf(label)
            if (labelIndex != -1 && labelIndex + 1 < siblings.size) {
                val valueObj = siblings[labelIndex + 1]
                if (valueObj.text.isNotBlank()) return valueObj.text
            }

            val genericModel = device.findObject(By.textContains("SM-"))
            if (genericModel != null) return genericModel.text

            null
        } catch (e: Exception) {
            Log.e("DeviceModelTest", "Erro ao extrair o nome do modelo: \${e.message}")
            null
        }
    }
}

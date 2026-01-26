package com.samsung.requirements.automatechecklisttest.tests

import android.util.Log
import androidx.test.uiautomator.By
import com.samsung.requirements.automatechecklisttest.R
import com.samsung.requirements.automatechecklisttest.base.BaseTest
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Teste para verificar as Informações Regulatórias do dispositivo.
 */
class RegulatoryInfoTest : BaseTest() {

    @Test
    fun verifyRegulatoryInfo() {
        Log.i("RegulatoryInfoTest", "--- Iniciando Teste de Informações Regulatórias ---")

        // 1. Inicia a activity de Configurações
        launchActivity("com.android.settings", "com.android.settings.Settings")

        // 2. Busca e clica em "Sobre o telefone"
        scrollAndClick(R.string.about_phone)

        // 3. Busca e clica em "Informações regulatórias"
        scrollAndClick(R.string.regulatory_info)

        // 4. Aguarda abrir uma imagem (comumente exibida nesta tela)
        // Tentamos localizar um ImageView que represente o selo/imagem regulatória
        val isImageVisible = waitVisible(By.clazz("android.widget.ImageView"), LONG_TIMEOUT)
        
        if (isImageVisible) {
            Log.i("RegulatoryInfoTest", "Imagem de informações regulatórias detectada com sucesso.")
        } else {
            Log.e("RegulatoryInfoTest", "Imagem de informações regulatórias não foi encontrada.")
            // Logar elementos para debug caso falhe
            screenInspector.logAllVisibleElements(null)
        }

        assertTrue("A tela de informações regulatórias não exibiu a imagem esperada.", isImageVisible)
    }
}

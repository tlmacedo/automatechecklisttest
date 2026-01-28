package com.samsung.requirements.automatechecklisttest.tests

import android.util.Log
import androidx.test.uiautomator.By
import com.samsung.requirements.automatechecklisttest.R
import com.samsung.requirements.automatechecklisttest.base.BaseTest
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Teste para verificar o acesso e o conteúdo das Informações Regulatórias do dispositivo.
 */
class RegulatoryInfoTest : BaseTest() {

    companion object {
        private const val TAG = "RegulatoryInfoTest"
    }

    @Test
    fun verifyRegulatoryInformationIsAccessible() {
        Log.i(TAG, "--- Iniciando Teste de Informações Regulatórias ---")

        // 1. Inicia as Configurações
        Log.d(TAG, "Abrindo a tela de Configurações...")
        launchActivity("com.android.settings", "com.android.settings.Settings")

        // 2. Navega para "Sobre o telefone" e verifica se a tela carregou
        Log.d(TAG, "Navegando para 'Sobre o telefone'...")
        scrollAndClick(R.string.about_phone)
        val aboutPhoneScreenLoaded = waitVisible(By.text(appContext.getString(R.string.about_phone)), DEFAULT_TIMEOUT)
        if (!aboutPhoneScreenLoaded) {
            takeScreenshot("AboutPhone_Screen_Failed")
            fail("A tela 'Sobre o telefone' não foi carregada com sucesso.")
        }
        Log.d(TAG, "Tela 'Sobre o telefone' carregada.")
        takeScreenshot("Settings_AboutPhone_Screen")

        // 3. Navega para "Informações regulatórias" e verifica se a tela carregou
        Log.d(TAG, "Navegando para 'Informações regulatórias'...")
        scrollAndClick(R.string.regulatory_info)
        val regulatoryScreenLoaded = waitVisible(By.text(appContext.getString(R.string.regulatory_info)), DEFAULT_TIMEOUT)
        if (!regulatoryScreenLoaded) {
            takeScreenshot("RegulatoryInfo_Screen_Failed")
            fail("A tela 'Informações regulatórias' não foi carregada com sucesso.")
        }
        Log.d(TAG, "Tela 'Informações regulatórias' carregada.")

        // 4. Verifica o conteúdo da tela, que pode ser uma imagem ou uma WebView
        Log.d(TAG, "Verificando conteúdo da tela de informações regulatórias...")
        val imageViewSelector = By.clazz("android.widget.ImageView")
        val webViewSelector = By.clazz("android.webkit.WebView")

        // Espera por uma imagem OU um webview, que são os formatos mais comuns para esta tela.
        val contentFound = waitAnyHasObject(LONG_TIMEOUT, imageViewSelector, webViewSelector)
        
        takeScreenshot("Regulatory_Info_Content")

        if (contentFound) {
            Log.i(TAG, "Conteúdo regulatório (Imagem ou WebView) detectado com sucesso.")
        } else {
            Log.w(TAG, "Nenhum conteúdo esperado (Imagem ou WebView) foi encontrado.")
            // Tira um print e loga os elementos visíveis para facilitar o debug.
            screenInspector.logAllVisibleElements(null)
        }

        assertTrue("A tela de informações regulatórias não exibiu o conteúdo esperado (imagem ou webview).", contentFound)
        Log.i(TAG, "--- Teste de Informações Regulatórias Concluído com Sucesso ---")
    }
}

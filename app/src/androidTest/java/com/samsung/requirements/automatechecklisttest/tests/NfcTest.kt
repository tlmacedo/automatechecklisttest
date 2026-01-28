package com.samsung.requirements.automatechecklisttest.tests

import android.os.Build
import android.util.Log
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.samsung.requirements.automatechecklisttest.R
import com.samsung.requirements.automatechecklisttest.base.BaseTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class NfcTest : BaseTest() {

    companion object {
        private const val TAG = "NfcTest"
        private const val NFC_SCREEN_TIMEOUT = 10000L // Aumentado para mais robustez
        private const val SWITCH_WAIT_TIMEOUT = 5000L
    }

    @Test
    fun testNfcFunctionalityAndQuickPanel() {
        // 1) Abre a tela de configurações
        Log.i(TAG, "Abrindo tela de configurações...")
        launchActivity("com.android.settings", "com.android.settings.Settings")

        // 2) Aguarda a tela carregar e clica em "Conexões"
        Log.i(TAG, "Navegando para Conexões...")
        scrollAndClick(R.string.connections)
        
        // Aguarda a tela de Conexões carregar
        waitForConnectionsScreen()

        // 3) Decide o recurso de string do menu NFC baseado na versão do Android
        val nfcMenuRes = if (Build.VERSION.SDK_INT >= 36) {
            R.string.nfc_contactless_payments
        } else {
            R.string.nfc
        }
        val nfcMenuText = appContext.getString(nfcMenuRes)
        Log.i(TAG, "Navegando para NFC (menu: $nfcMenuText)...")

        // Clica no menu NFC
        scrollAndClick(nfcMenuRes)

        // 4) Aguarda a tela de NFC carregar e localiza o switch
        val nfcSwitch = waitForAndFindNfcSwitch(NFC_SCREEN_TIMEOUT)
        assertNotNull("Switch de NFC não encontrado na tela de NFC", nfcSwitch)

        val initialState = nfcSwitch!!.isChecked
        Log.i(TAG, "Estado inicial do NFC: $initialState")

        // Tira print da tela de NFC
        takeScreenshot("NFC_State_Initial")

        // 5) Valida no Quick Panel o estado inicial
        validateNfcInQuickPanel(initialState, "Initial")

        // Retorna para a tela de NFC após validação no Quick Panel
        returnToNfcScreen(nfcMenuRes)

        // 6) Re-localiza o switch após retornar (o objeto pode estar stale)
        val nfcSwitchAfterReturn = waitForAndFindNfcSwitch(SWITCH_WAIT_TIMEOUT)
        assertNotNull("Switch de NFC não encontrado após retornar do Quick Panel", nfcSwitchAfterReturn)

        // 7) Altera o estado do NFC
        Log.i(TAG, "Alterando estado do NFC para: ${!initialState}")
        safeClick(nfcSwitchAfterReturn)
        
        // Aguarda a mudança de estado
        val stateChanged = waitCheckedState(nfcSwitchAfterReturn, !initialState, DEFAULT_TIMEOUT)
        assertTrue("O estado do NFC não foi alterado corretamente", stateChanged)

        takeScreenshot("NFC_State_Changed")

        // 8) Valida no Quick Panel o novo estado
        validateNfcInQuickPanel(!initialState, "Changed")

        // Retorna para a tela de NFC
        returnToNfcScreen(nfcMenuRes)

        // 9) Re-localiza o switch e retorna ao estado inicial
        val nfcSwitchFinal = waitForAndFindNfcSwitch(SWITCH_WAIT_TIMEOUT)
        assertNotNull("Switch de NFC não encontrado para restaurar estado", nfcSwitchFinal)

        Log.i(TAG, "Restaurando estado inicial do NFC: $initialState")
        safeClick(nfcSwitchFinal)
        
        val stateRestored = waitCheckedState(nfcSwitchFinal, initialState, DEFAULT_TIMEOUT)
        assertTrue("O estado do NFC não foi restaurado corretamente", stateRestored)

        takeScreenshot("NFC_State_Restored")
        Log.i(TAG, "Teste concluído com sucesso!")
    }

    /**
     * Aguarda a tela de Conexões carregar, verificando a presença do item de menu NFC.
     * Falha o teste se o item não for encontrado.
     */
    private fun waitForConnectionsScreen() {
        Log.d(TAG, "Aguardando tela de Conexões carregar...")
        device.waitForIdle(SHORT_TIMEOUT)

        val nfcMenuRes = if (Build.VERSION.SDK_INT >= 36) {
            R.string.nfc_contactless_payments
        } else {
            R.string.nfc
        }
        val nfcMenuText = appContext.getString(nfcMenuRes)
        
        val nfcItemFound = device.wait(
            Until.hasObject(By.textContains(nfcMenuText)),
            DEFAULT_TIMEOUT
        )
        
        if (!nfcItemFound) {
            takeScreenshot("Connections_Screen_Load_Failed")
            fail("A tela de Conexões não carregou ou o item NFC não foi encontrado.")
        }
        
        Log.d(TAG, "Tela de Conexões carregada com sucesso.")
    }

    /**
     * Aguarda a tela de NFC carregar e localiza o switch de NFC.
     * A presença do switch é o principal indicador de que a tela está pronta.
     * @param timeout Tempo máximo de espera em milissegundos.
     * @return O objeto UiObject2 para o switch, ou null se não for encontrado.
     */
    private fun waitForAndFindNfcSwitch(timeout: Long): UiObject2? {
        Log.d(TAG, "Aguardando e localizando o switch de NFC por até ${timeout}ms...")

        device.waitForIdle()
        
        val switchSelector = By.clazz("android.widget.Switch")
        val switchObj = device.wait(Until.findObject(switchSelector), timeout)

        if (switchObj == null) {
            Log.e(TAG, "Switch de NFC não foi encontrado no tempo especificado.")
            takeScreenshot("NFC_Screen_Switch_Not_Found")
        } else {
            Log.d(TAG, "Switch de NFC encontrado com sucesso.")
        }
        return switchObj
    }

    /**
     * Retorna para a tela de NFC após sair (ex: do Quick Panel).
     */
    private fun returnToNfcScreen(nfcMenuRes: Int) {
        Log.d(TAG, "Retornando para a tela de NFC...")
        
        if (device.hasObject(By.clazz("android.widget.Switch"))) {
            Log.d(TAG, "Já estamos na tela de NFC.")
            return
        }
        
        Log.d(TAG, "Não estamos na tela de NFC, navegando novamente...")
        closeQuickSettings()
        device.pressBack()
        device.waitForIdle()

        val nfcMenuText = appContext.getString(nfcMenuRes)
        val onNfcScreen = device.wait(Until.hasObject(By.text(nfcMenuText)), SHORT_TIMEOUT)

        if(onNfcScreen) {
            val nfcSwitch = waitForAndFindNfcSwitch(NFC_SCREEN_TIMEOUT)
            if (nfcSwitch != null) {
                Log.d(TAG, "Retorno para a tela NFC bem-sucedido.")
                return
            }
        }

        Log.d(TAG, "Não foi possível retornar para a tela de NFC, tentando do início...")
        launchActivity("com.android.settings", "com.android.settings.Settings")
        
        scrollAndClick(R.string.connections)
        waitForConnectionsScreen()
        
        scrollAndClick(nfcMenuRes)
        
        val nfcSwitch = waitForAndFindNfcSwitch(NFC_SCREEN_TIMEOUT)
        assertNotNull("Não foi possível retornar e encontrar o switch de NFC", nfcSwitch)
    }

    /**
     * Valida o estado do NFC no Quick Panel.
     */
    private fun validateNfcInQuickPanel(shouldBeVisible: Boolean, label: String) {
        Log.d(TAG, "Validando NFC no Quick Panel (esperado visível: $shouldBeVisible)...")
        
        // Abaixa o Quick Panel
        openQuickSettings()

        // Aguarda o Quick Panel carregar completamente
        device.waitForIdle(SHORT_TIMEOUT)

        // Validação do ícone de NFC na barra de status do Quick Panel
        val nfcIconSelector = By.descContains("NFC")

        // Aguarda um pouco para os ícones carregarem
        val isVisible = device.wait(
            Until.hasObject(nfcIconSelector),
            SHORT_TIMEOUT
        ) ?: false

        takeScreenshot("QuickPanel_$label")

        if (shouldBeVisible) {
            assertTrue("O ícone de NFC deveria estar visível no Quick Panel ($label)", isVisible)
        } else {
            assertFalse(
                "O ícone de NFC NÃO deveria estar visível no Quick Panel ($label)",
                isVisible
            )
        }

        // Fecha o Quick Panel
        closeQuickSettings()
        
        // Aguarda o Quick Panel fechar completamente
        device.waitForIdle(SHORT_TIMEOUT)
    }
}

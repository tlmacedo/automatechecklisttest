package com.samsung.requirements.automatechecklisttest.tests

import android.os.Build
import android.util.Log
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.samsung.requirements.automatechecklisttest.R
import com.samsung.requirements.automatechecklisttest.base.BaseTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NfcTest : BaseTest() {

    @Test
    fun testNfcFunctionalityAndQuickPanel() {
        // 1) Abre a tela de configurações
        launchActivity("com.android.settings", "com.android.settings.Settings")

        // 2) Aguarda a tela carregar e clica em "Conexões"
        scrollAndClick(R.string.connections)

        // 3) Decide o recurso de string do menu NFC baseado na versão do Android
        // Se Android 16 (API 36+): "NFC e Pagamentos sem contato". Caso contrário: "NFC"
        val nfcMenuRes = if (Build.VERSION.SDK_INT >= 36) {
            R.string.nfc_contactless_payments
        } else {
            R.string.nfc
        }

        // Clica no menu NFC
        scrollAndClick(nfcMenuRes)

        // 4) Verifica e guarda o estado inicial do NFC
        val nfcSwitch = device.wait(
            Until.findObject(By.clazz("android.widget.Switch")),
            DEFAULT_TIMEOUT
        )
        val initialState = nfcSwitch.isChecked
        Log.i("NfcTest", "Estado inicial do NFC: $initialState")

        // Tira print da tela de NFC
        takeScreenshot("NFC_State_Initial")

        // 5) Valida no Quick Panel o estado inicial
        validateNfcInQuickPanel(initialState, "Initial")

        // 6) Altera o estado do NFC
        Log.i("NfcTest", "Alterando estado do NFC para: ${!initialState}")
        safeClick(nfcSwitch)
        waitCheckedState(nfcSwitch, !initialState, SHORT_TIMEOUT)

        device.wait(Until.findObject(By.checked(!initialState)), DEFAULT_TIMEOUT)
        takeScreenshot("NFC_State_Changed")

        // 7) Valida no Quick Panel o novo estado
        validateNfcInQuickPanel(!initialState, "Changed")

        // 8) Retorna ao estado inicial
        Log.i("NfcTest", "Retornando ao estado inicial: $initialState")
        safeClick(nfcSwitch)
        waitCheckedState(nfcSwitch, initialState, SHORT_TIMEOUT)
//        takeScreenshot("NFC_State_Restored")
    }

    private fun validateNfcInQuickPanel(shouldBeVisible: Boolean, label: String) {
        // Abaixa o Quick Panel
        openQuickSettings()

        // Validação do ícone de NFC na barra de status do Quick Panel
        val nfcIconSelector = By.descContains("NFC")

        // Aguarda um pouco para os ícones carregarem
        val isVisible = device.hasObject(nfcIconSelector)

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
    }
}

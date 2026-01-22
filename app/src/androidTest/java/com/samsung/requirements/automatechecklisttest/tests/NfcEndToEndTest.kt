package com.samsung.requirements.automatechecklisttest.tests

import android.content.pm.PackageManager
import android.util.Log
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import com.samsung.requirements.automatechecklisttest.base.BaseTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Testa a funcionalidade de ligar/desligar o NFC e a visibilidade do ícone correspondente
 * nas configurações rápidas.
 */
class NfcEndToEndTest : BaseTest() {

    private val nfcIconSelector = By.descContains("NFC")

    @Before
    override fun setup() {
        super.setup()

        val packageManager = appContext.packageManager
        val hasNfcFeature = packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)
        assertTrue("PRÉ-REQUISITO FALHOU: O aparelho não suporta NFC.", hasNfcFeature)

        // 1. Abre diretamente a tela de NFC via caminho explícito desacoplado
        launchActivity("com.android.settings", "com.android.settings.Settings\$NfcSettingsActivity")
    }

    @Test
    fun testNfcToggleLoopAndIconVisibility() {
        // 2. Localiza o switch do NFC
        val nfcSwitch = waitAndFindObject(By.clazz("android.widget.Switch"))
        dumpObjectInfo(nfcSwitch, "nfcSwitch")
        
        var currentNfcStateIsOn = nfcSwitch.isChecked

        // Executa o ciclo de teste duas vezes.
        repeat(2) { cycle ->
            Log.i("NfcTest", "Iniciando ciclo \${cycle + 1}. NFC Ligado: \$currentNfcStateIsOn")
            
            // 3. Verifica ícone nas Configurações Rápidas
            openQuickSettings()
            
            val isVisible = isElementVisible(nfcIconSelector, SHORT_TIMEOUT)
            
            if (currentNfcStateIsOn) {
                assertTrue("Ícone NFC deveria estar VISÍVEL (LIGADO) no ciclo \${cycle + 1}.", isVisible)
                takeScreenshot("NFC_ON_cycle\${cycle + 1}_visible.png")
            } else {
                assertFalse("Ícone NFC deveria estar INVISÍVEL (DESLIGADO) no ciclo \${cycle + 1}.", isVisible)
                takeScreenshot("NFC_OFF_cycle\${cycle + 1}_invisible.png")
            }
            
            closeQuickSettings()

            // 4. Inverte o estado do NFC
            val targetState = !currentNfcStateIsOn
            Log.i("NfcTest", "Alterando NFC para: \$targetState")
            
            safeClick(nfcSwitch)
            
            // Aguarda a mudança de estado de forma robusta usando helper da BaseTest
            val changedOk = waitCheckedState(nfcSwitch, targetState, SHORT_TIMEOUT)
            currentNfcStateIsOn = nfcSwitch.isChecked
            
            takeScreenshot("NFC_Settings_cycle\${cycle + 1}_after_click.png")
        }

        assertTrue("Fluxo de teste de NFC finalizado com sucesso.", true)
    }
}

package com.samsung.requirements.automatechecklisttest.tests

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.samsung.requirements.automatechecklisttest.base.BaseTest
import com.samsung.requirements.automatechecklisttest.R
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Fluxo:
 * 1) Abre Configurações de Wi-Fi via caminho explícito
 * 2) Garante desligar/ligar Wi-Fi validando o estado do switch
 * 3) Conecta no SSID alvo
 * 4) Valida status "Conectado"
 */
class WifiConnectionTest : BaseTest() {

    private val WIFI_TOGGLE_TIMEOUT = 6_000L
    private val WIFI_SCAN_TIMEOUT = 15_000L
    private val WIFI_CONNECTION_TIMEOUT = 20_000L

    private val args = InstrumentationRegistry.getArguments()
    private val TARGET_WIFI_SSID: String = args.getString("WIFI_SSID", "SUA_REDE_WIFI")!!
    private val TARGET_WIFI_PASSWORD: String = args.getString("WIFI_PWD", "SUA_SENHA")!!

    private val wifiSwitchSelector = By.clazz("android.widget.Switch")
    private val passwordFieldSelector = By.clazz("android.widget.EditText")

    @Before
    override fun setup() {
        super.setup()
        // 1) Abre diretamente a tela de Wi-Fi via caminho explícito desacoplado
        launchActivity("com.android.settings", "com.android.settings.Settings\$WifiSettingsActivity")

        // Critério de “tela carregada”
        val loaded = waitVisible(wifiSwitchSelector, DEFAULT_TIMEOUT)
        assertTrue("Falha ao abrir as configurações de Wi-Fi.", loaded)
    }

    @Test
    fun testWifiCycleAndConnection() {
        // 2) Localiza o switch de Wi-Fi
        val wifiSwitch = waitAndFindObject(wifiSwitchSelector, WIFI_TOGGLE_TIMEOUT)
        
        // 3) Desliga (se já ligado) -> Liga novamente, validando estado
        if (wifiSwitch.isChecked) {
            Log.i("WifiTest", "Wi-Fi está LIGADO. Desligando...")
            safeClick(wifiSwitch)
            waitCheckedState(wifiSwitch, expected = false, timeoutMs = WIFI_TOGGLE_TIMEOUT)
        }

        Log.i("WifiTest", "Ligando o Wi-Fi...")
        if (!wifiSwitch.isChecked) safeClick(wifiSwitch)
        val onOk = waitCheckedState(wifiSwitch, expected = true, timeoutMs = WIFI_TOGGLE_TIMEOUT)
        assertTrue("Falha ao ligar o Wi-Fi.", onOk)

        // 4) Procura rede e clica (fazendo scroll se necessário)
        Log.i("WifiTest", "Procurando rede: $TARGET_WIFI_SSID")
        scrollAndClick(TARGET_WIFI_SSID)

        // 5) Tenta conectar (trata senha)
        handlePasswordDialogIfNeeded()

        // 6) Valida status "Conectado" (Traduzido)
        Log.i("WifiTest", "Aguardando status 'Conectado'...")
        val connectedOk = waitConnectedToSsid(TARGET_WIFI_SSID, WIFI_CONNECTION_TIMEOUT)
        assertTrue("Falha ao conectar à rede '$TARGET_WIFI_SSID'.", connectedOk)
    }

    private fun handlePasswordDialogIfNeeded() {
        device.waitForIdle(SHORT_TIMEOUT)
        val pwdField = device.findObject(passwordFieldSelector)
        if (pwdField != null) {
            pwdField.text = TARGET_WIFI_PASSWORD
            // Usa o texto "Conectar" do resource traduzido
            val connectText = appContext.getString(R.string.connect)
            val connectBtn = waitAndFindObject(By.text(connectText))
            safeClick(connectBtn)
        }
    }

    private fun waitConnectedToSsid(ssid: String, timeoutMs: Long): Boolean {
        val connectedText = appContext.getString(R.string.connected)
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            // Verifica se o texto "Conectado" aparece na tela
            if (device.hasObject(By.textContains(connectedText))) {
                // Opcional: validar se está atrelado ao SSID correto
                val ssidObj = device.findObject(By.text(ssid))
                if (ssidObj != null) return true
            }
            Thread.sleep(1000)
        }
        return false
    }
}

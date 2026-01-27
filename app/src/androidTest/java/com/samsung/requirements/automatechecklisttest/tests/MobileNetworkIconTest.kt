package com.samsung.requirements.automatechecklisttest.tests

import android.content.Context
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.samsung.requirements.automatechecklisttest.R
import com.samsung.requirements.automatechecklisttest.base.BaseTest
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class MobileNetworkIconTest : BaseTest() {

    private var initialBandName: String? = null

    @Test
    fun testMobileDataIconsAcrossBands() {
        try {
            // 1. Validar SimCard e Desativar Wi-Fi
            prepareDeviceForMobileDataTest()

            // 2. Abrir Configurações -> Conexões -> Redes Móveis
            launchActivity("com.android.settings", "com.android.settings.Settings")
            scrollAndClick(R.string.connections)
            scrollAndClick(R.string.mobile_networks)

            // 3. Identificar e guardar a banda inicial
            clickNetworkModeMenu()
            val initialOptions = getBandOptions()
            initialBandName = initialOptions?.find { it.isChecked }?.text
            Log.i("MobileNetworkTest", "Banda inicial detectada: $initialBandName")
            
            // Fecha o menu para iniciar o loop do zero ou reutiliza a lista
            device.pressBack() 

            // 4. Obter a lista de opções de banda
            clickNetworkModeMenu()
            val bandOptions = getBandOptions()

            if (bandOptions.isNullOrEmpty()) {
                fail("Não foi possível encontrar as opções de banda no menu.")
            }

            val optionsCount = bandOptions!!.size
            Log.i("MobileNetworkTest", "Encontradas $optionsCount opções de banda.")

            // Para cada banda disponível no menu
            for (i in 0 until optionsCount) {
                val currentOptions = getBandOptions()
                if (currentOptions.isNullOrEmpty() || i >= currentOptions.size) break

                val option = currentOptions[i]
                val bandName = option.text
                Log.i("MobileNetworkTest", "Testando banda: $bandName")

                // Clica na banda
                safeClick(option)

                // Monitoramento dinâmico do status bar
                Log.i("MobileNetworkTest", "Aguardando ícone correspondente no status bar...")
                val success = waitForNetworkIcon(bandName)

                // Tira print
                takeScreenshot("NetworkIcon_${bandName.replace("/", "_")}")

                if (!success) {
                    Log.w("MobileNetworkTest", "Não foi possível detectar o ícone esperado para $bandName dentro do timeout.")
                }

                // Se não for a última opção, clica novamente no menu para abrir a próxima
                if (i < optionsCount - 1) {
                    clickNetworkModeMenu()
                    device.wait(Until.hasObject(By.clazz("android.widget.CheckedTextView")), DEFAULT_TIMEOUT)
                }
            }
        } finally {
            restoreInitialBand()
        }
    }

    private fun restoreInitialBand() {
        if (initialBandName == null) return
        
        Log.i("MobileNetworkTest", "Restaurando banda inicial: $initialBandName")
        try {
            // Garante que estamos na tela de Redes Móveis
            launchActivity("com.android.settings", "com.android.settings.Settings")
            scrollAndClick(R.string.connections)
            scrollAndClick(R.string.mobile_networks)
            
            clickNetworkModeMenu()
            val options = getBandOptions()
            val target = options?.find { it.text == initialBandName }
            
            if (target != null) {
                if (!target.isChecked) {
                    safeClick(target)
                    Log.i("MobileNetworkTest", "Banda restaurada com sucesso.")
                } else {
                    Log.i("MobileNetworkTest", "Banda já era a inicial. Fechando menu.")
                    device.pressBack()
                }
            } else {
                Log.e("MobileNetworkTest", "Não foi possível encontrar a banda inicial para restaurar.")
                device.pressBack()
            }
        } catch (e: Exception) {
            Log.e("MobileNetworkTest", "Erro ao restaurar banda inicial: ${e.message}")
        }
    }

    /**
     * Monitora o status bar aguardando o ícone de rede esperado baseado na banda selecionada.
     */
    private fun waitForNetworkIcon(bandName: String): Boolean {
        val expectedIcons = when {
            bandName.contains("5G", ignoreCase = true) -> listOf("5G")
            bandName.contains("LTE", ignoreCase = true) || bandName.contains("4G", ignoreCase = true) -> listOf("4G+", "4G", "LTE+", "LTE")
            bandName.contains("3G", ignoreCase = true) -> listOf("3G", "H+", "H", "3G+")
            else -> emptyList()
        }

        if (expectedIcons.isEmpty()) {
            Log.w("MobileNetworkTest", "Nenhum mapeamento de ícone definido para a banda: $bandName")
            return false
        }

        val timeout = 30000L // Timeout de 30 segundos para troca de rede
        val start = System.currentTimeMillis()

        while (System.currentTimeMillis() - start < timeout) {
            for (iconText in expectedIcons) {
                val iconSelector = By.pkg("com.android.systemui").descContains(iconText)
                if (device.hasObject(iconSelector)) {
                    Log.i("MobileNetworkTest", "Ícone '$iconText' detectado no status bar!")
                    return true
                }
            }
            Thread.sleep(500)
        }
        return false
    }

    private fun getBandOptions(): List<UiObject2>? {
        return device.wait(
            Until.findObjects(By.clazz("android.widget.CheckedTextView")),
            DEFAULT_TIMEOUT
        )
    }

    private fun clickNetworkModeMenu() {
        val objects = device.wait(
            Until.findObjects(
                By.textContains(appContext.getString(R.string.network_mode))
            ), DEFAULT_TIMEOUT
        )
        if (objects != null && objects.isNotEmpty()) {
            objects.first().click()
        } else {
            scrollAndClickContains(R.string.network_mode)
        }
    }

    private fun prepareDeviceForMobileDataTest() {
        val telephonyManager =
            appContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (telephonyManager.simState != TelephonyManager.SIM_STATE_READY) {
            fail("O teste requer um SimCard inserido e pronto.")
        }

        if (wifiManager.isWifiEnabled) {
            device.executeShellCommand("svc wifi disable")
            Thread.sleep(2000)
        }

        device.executeShellCommand("svc data enable")
        Thread.sleep(2000)
    }
}

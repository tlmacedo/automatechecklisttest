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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class MobileNetworkIconTest : BaseTest() {

    companion object {
        private const val TAG = "MobileNetworkIconTest"
    }

    private var initialBandName: String? = null
    private var initialWifiState: Boolean = false

    @Test
    fun testMobileDataIconsAcrossBands() {
        try {
            // 1. Preparar o dispositivo para o teste de dados móveis
            prepareDeviceForMobileDataTest()

            // 2. Navegar para a tela de seleção de modo de rede
            navigateToNetworkModeScreen()

            // 3. Obter a lista de nomes de bandas e guardar a inicial
            clickNetworkModeMenu()
            val bandOptions = getBandOptions()
            assertNotNull("Não foi possível encontrar as opções de banda no menu.", bandOptions)

            initialBandName = bandOptions!!.find { it.isChecked }?.text
            assertNotNull("Não foi possível detectar a banda inicial selecionada.", initialBandName)
            Log.i(TAG, "Banda inicial detectada: $initialBandName")

            // Usar uma lista de nomes para iterar, evitando problemas com objetos Stale
            val bandNames = bandOptions.map { it.text }
            device.pressBack() // Fechar o menu para começar o loop
            Log.i(TAG, "Encontradas ${bandNames.size} opções de banda para testar: $bandNames")

            // 4. Iterar sobre cada banda, selecionar e verificar o ícone
            for (bandName in bandNames) {
                Log.i(TAG, "==> Iniciando teste para a banda: '$bandName' <==")
                // selectBand abre o menu, seleciona a banda, e o menu fecha.
                selectBand(bandName)

                Log.i(TAG, "Aguardando ícone de rede para '$bandName' no status bar...")
                val iconFound = waitForNetworkIcon(bandName)
                assertTrue("Ícone de rede para a banda '$bandName' não foi encontrado a tempo.", iconFound)

                // Reabre o menu para tirar o print com ele aberto.
                Log.d(TAG, "Reabrindo o menu de bandas para o screenshot...")
                clickNetworkModeMenu()

                takeScreenshot("NetworkIcon_${bandName.replace(Regex("[/ ]"), "_")}")

                // Fecha o menu para a próxima iteração ter um estado limpo.
                device.pressBack()
            }
        } finally {
            restoreInitialBand()
            restoreWifiState()
        }
    }

    private fun navigateToNetworkModeScreen() {
        Log.i(TAG, "Navegando para a tela de 'Redes Móveis'...")
        launchActivity("com.android.settings", "com.android.settings.Settings")

        scrollAndClick(R.string.connections)
        val connectionsScreen = device.wait(Until.hasObject(By.text(appContext.getString(R.string.connections))), DEFAULT_TIMEOUT)
        assertTrue("A tela de 'Conexões' não carregou.", connectionsScreen)

        scrollAndClick(R.string.mobile_networks)
        val mobileNetworksScreen = device.wait(Until.hasObject(By.text(appContext.getString(R.string.mobile_networks))), DEFAULT_TIMEOUT)
        assertTrue("A tela de 'Redes Móveis' não carregou.", mobileNetworksScreen)
        Log.i(TAG, "Chegou na tela de 'Redes Móveis'.")
    }

    private fun selectBand(bandName: String) {
        Log.d(TAG, "Selecionando a banda: $bandName")
        clickNetworkModeMenu() // Abre o menu

        val bandOption = device.wait(Until.findObject(By.text(bandName)), DEFAULT_TIMEOUT)
        assertNotNull("Opção de banda '$bandName' não encontrada no menu.", bandOption)

        if (!bandOption.isChecked) {
            safeClick(bandOption)
            Log.i(TAG, "Banda '$bandName' selecionada.")
            device.waitForIdle(DEFAULT_TIMEOUT)
        } else {
            Log.i(TAG, "Banda '$bandName' já está selecionada. Fechando menu.")
            device.pressBack()
        }
    }

    private fun restoreInitialBand() {
        if (initialBandName == null) {
            Log.d(TAG, "Nenhuma banda inicial foi salva, restauração não necessária.")
            return
        }

        Log.i(TAG, "Restaurando banda inicial para: $initialBandName")
        try {
            // A tela de 'Redes Móveis' deve estar ativa ao final do loop de teste.
            selectBand(initialBandName!!)
            Log.i(TAG, "Banda inicial restaurada com sucesso.")
        } catch (e: Exception) {
            Log.e(TAG, "Ocorreu uma exceção ao tentar restaurar a banda inicial: ${e.message}")
            takeScreenshot("Restore_Band_Failed")
        }
    }

    private fun restoreWifiState() {
        if (initialWifiState) {
            Log.i(TAG, "Restaurando estado inicial do Wi-Fi (ativando)...")
            try {
                val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                if (!wifiManager.isWifiEnabled) {
                    device.executeShellCommand("svc wifi enable")
                    val wifiEnabled = (0..10).any {
                        Thread.sleep(500)
                        wifiManager.isWifiEnabled
                    }
                    if (wifiEnabled) {
                        Log.i(TAG, "Wi-Fi reativado com sucesso.")
                    } else {
                        Log.w(TAG, "Não foi possível confirmar a reativação do Wi-Fi.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ocorreu uma exceção ao tentar restaurar o Wi-Fi: ${e.message}")
            }
        }
    }

    private fun clickNetworkModeMenu() {
        Log.d(TAG, "Procurando e clicando em 'Modo de Rede'...")
        scrollAndClickContains(R.string.network_mode)

        val menuVisible = device.wait(Until.hasObject(By.clazz("android.widget.CheckedTextView")), DEFAULT_TIMEOUT)
        if (!menuVisible) {
            takeScreenshot("NetworkModeMenu_Not_Opened")
            fail("O menu 'Modo de Rede' não abriu após o clique.")
        }
        Log.d(TAG, "Menu 'Modo de Rede' aberto com sucesso.")
    }

    private fun getBandOptions(): List<UiObject2>? {
        return device.wait(
            Until.findObjects(By.clazz("android.widget.CheckedTextView")),
            DEFAULT_TIMEOUT
        )
    }

    private fun waitForNetworkIcon(bandName: String): Boolean {
        val expectedIcons = when {
            bandName.contains("5G", ignoreCase = true) -> listOf("5G")
            bandName.contains("LTE", ignoreCase = true) || bandName.contains("4G", ignoreCase = true) -> listOf("4G+", "4G", "LTE+", "LTE")
            bandName.contains("3G", ignoreCase = true) -> listOf("3G", "H+", "H", "3G+")
            else -> emptyList()
        }

        if (expectedIcons.isEmpty()) {
            Log.w(TAG, "Nenhum mapeamento de ícone definido para a banda: '$bandName'. Verificação pulada.")
            return true
        }

        Log.d(TAG, "Procurando por um dos seguintes ícones para a banda '$bandName': $expectedIcons")
        val timeout = 30000L // Timeout de 30 segundos para troca de rede
        val start = System.currentTimeMillis()

        while (System.currentTimeMillis() - start < timeout) {
            for (iconText in expectedIcons) {
                val iconSelector = By.pkg("com.android.systemui").descContains(iconText)
                if (device.hasObject(iconSelector)) {
                    Log.i(TAG, "Ícone de rede '$iconText' detectado com sucesso no status bar!")
                    return true
                }
            }
            Thread.sleep(1000)
        }

        Log.e(TAG, "Timeout: Nenhum ícone de rede esperado foi detectado para a banda '$bandName'.")
        return false
    }

    private fun prepareDeviceForMobileDataTest() {
        Log.d(TAG, "Preparando dispositivo para teste de dados móveis...")
        val telephonyManager = appContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (telephonyManager.simState != TelephonyManager.SIM_STATE_READY) {
            fail("O teste requer um SimCard inserido e pronto para uso.")
        }

        val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        initialWifiState = wifiManager.isWifiEnabled
        if (initialWifiState) {
            Log.d(TAG, "Desativando Wi-Fi para o teste...")
            device.executeShellCommand("svc wifi disable")
            val wifiDisabled = (0..5).any {
                Thread.sleep(500)
                !wifiManager.isWifiEnabled
            }
            if (!wifiDisabled) {
                fail("Não foi possível desativar o Wi-Fi.")
            }
            Log.d(TAG, "Wi-Fi desativado.")
        }

        Log.d(TAG, "Ativando dados móveis...")
        device.executeShellCommand("svc data enable")
        Thread.sleep(2000) // Pausa para garantir que a conexão de dados seja estabelecida
        Log.d(TAG, "Dispositivo pronto.")
    }
}

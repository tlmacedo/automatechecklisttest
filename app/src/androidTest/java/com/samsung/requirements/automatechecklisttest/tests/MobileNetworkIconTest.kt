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
    //./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.samsung.requirements.automatechecklisttest.tests.MobileNetworkIconTest
    @Test
    fun testMobileDataIconsAcrossBands() {
        // 1. Validar SimCard e Desativar Wi-Fi
        prepareDeviceForMobileDataTest()

        // 2. Abrir Configurações -> Conexões -> Redes Móveis
        launchActivity("com.android.settings", "com.android.settings.Settings")
        scrollAndClick(R.string.connections)
        scrollAndClick(R.string.mobile_networks)

        // 3. Procurar e clicar em "Seleção de banda" (Network Mode)
//        scrollAndClick(R.string.network_mode)
        device.wait(
            Until.findObjects(By.textContains(appContext.getString(R.string.network_mode))),
            DEFAULT_TIMEOUT
        ).first().click()

        // 4. Obter a lista de opções de banda no menu/dialog que abriu
        // Geralmente é um ListView ou um conjunto de CheckedTextViews
        val bandOptions = device.wait(
            Until.findObjects(By.clazz("android.widget.CheckedTextView")),
            DEFAULT_TIMEOUT
        )

        if (bandOptions.isNullOrEmpty()) {
            fail("Não foi possível encontrar as opções de banda no menu.")
        }

        val optionsCount = bandOptions.size
        Log.i("MobileNetworkTest", "Encontradas $optionsCount opções de banda.")

        // Para cada banda disponível no menu
        for (i in 0 until optionsCount) {
            // Re-localiza as opções pois a tela pode ter mudado
            val currentOptions = device.findObjects(By.clazz("android.widget.CheckedTextView"))
            if (i >= currentOptions.size) break

            val option = currentOptions[i]
            val bandName = option.text
            Log.i("MobileNetworkTest", "Testando banda: $bandName")

            // Clica na banda
            safeClick(option)

            // Aguarda a mudança de rede e o ícone no status bar
            // Aguardamos até 15 segundos para a rede estabilizar
            Thread.sleep(8000)

            // Tira print focando no status bar
            takeScreenshot("NetworkIcon_$bandName")

            // Se não for a última opção, clica novamente no menu para abrir a próxima
            if (i < optionsCount - 1) {
                scrollAndClick(R.string.network_mode)
                device.wait(
                    Until.hasObject(By.clazz("android.widget.CheckedTextView")),
                    DEFAULT_TIMEOUT
                )
            }
        }
    }

    private fun prepareDeviceForMobileDataTest() {
        val telephonyManager =
            appContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // Validar SimCard
        if (telephonyManager.simState != TelephonyManager.SIM_STATE_READY) {
            Log.e(
                "MobileNetworkTest",
                "SimCard não detectado ou não está pronto. Estado: ${telephonyManager.simState}"
            )
            fail("O teste requer um SimCard inserido e pronto.")
        }

        // Desativar Wi-Fi se estiver ligado
        if (wifiManager.isWifiEnabled) {
            Log.i("MobileNetworkTest", "Wi-Fi detectado. Desativando para teste de dados móveis...")
            try {
                // Em versões recentes do Android, desativar via código pode ser restrito.
                // Usamos o shell como alternativa se necessário, ou avisamos o usuário.
                device.executeShellCommand("svc wifi disable")
                Thread.sleep(2000)
            } catch (e: Exception) {
                Log.e("MobileNetworkTest", "Erro ao tentar desativar Wi-Fi via shell: ${e.message}")
            }

            if (wifiManager.isWifiEnabled) {
                Log.w(
                    "MobileNetworkTest",
                    "Não foi possível desativar o Wi-Fi via comando. O teste pode ser afetado."
                )
            }
        }

        // Garante que os dados móveis estão ativos (opcional, mas recomendado)
        device.executeShellCommand("svc data enable")
        Thread.sleep(2000)
    }
}

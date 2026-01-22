package com.samsung.requirements.automatechecklisttest.ut

import android.os.Build
import android.util.Log
import com.samsung.requirements.automatechecklisttest.base.BaseTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Testes de Unidade/Integração de baixo nível para validar informações do dispositivo
 * sem o uso de ferramentas de UI como o UIAutomator.
 */
class DeviceModelUT: BaseTest() {

    @Test
    fun validateDeviceModelLowLevel() {
        // Obtendo informações diretamente das APIs de Build do Android
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER
        val brand = Build.BRAND
        val device = Build.DEVICE
        val product = Build.PRODUCT
        val hardware = Build.HARDWARE
        val display = Build.DISPLAY
        val buildId = Build.ID

        Log.i("DeviceModelUT", "--- Device Info (Low Level) ---")
        Log.i("DeviceModelUT", "Manufacturer: $manufacturer")
        Log.i("DeviceModelUT", "Brand: $brand")
        Log.i("DeviceModelUT", "Model: $model")
        Log.i("DeviceModelUT", "Device: $device")
        Log.i("DeviceModelUT", "Product: $product")
        Log.i("DeviceModelUT", "Hardware: $hardware")
        Log.i("DeviceModelUT", "Display ID: $display")
        Log.i("DeviceModelUT", "Build ID: $buildId")
        Log.i("DeviceModelUT", "-------------------------------")

        // Validações básicas
        assertNotNull("O modelo não deve ser nulo", model)
        assertFalse("O modelo não deve estar vazio", model.isEmpty())
        
        // Verifica se é um dispositivo Samsung
        if (manufacturer.equals("samsung", ignoreCase = true)) {
            Log.i("DeviceModelUT", "Dispositivo Samsung detectado.")
        }
    }

    @Test
    fun checkSystemPropertiesAndBinaryInfo() {
        Log.i("DeviceModelUT", "--- Binary & Release Info (System Properties) ---")
        
        // Lista de propriedades comuns para identificação de build/binário em aparelhos Samsung
        val propertiesToCheck = listOf(
            "ro.build.id",
            "ro.build.display.id",
            "ro.build.version.incremental",
            "ro.build.qb.id",           // Quick Build ID
            "ro.build.official.release",
            "ro.build.changelist",
            "ro.build.PDA",              // Samsung Specific: Software version
            "ro.build.CP",               // Samsung Specific: Modem version
            "ro.build.CSC"               // Samsung Specific: Region version
        )

        propertiesToCheck.forEach { prop ->
            val value = getSystemProperty(prop)
            Log.i("DeviceModelUT", "$prop: $value")
        }
        Log.i("DeviceModelUT", "--------------------------------------------------")
    }
}

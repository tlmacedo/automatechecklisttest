package com.samsung.requirements.automatechecklisttest.ut

import android.util.Log
import com.samsung.requirements.automatechecklisttest.base.BaseTest
import org.junit.Test

/**
 * Teste de baixo nível para validar a existência de arquivos de customização da Carrier (operadora).
 * Verifica arquivos de informação regulatória (regulatory_info.png) baseando-se no Sales Code e OMC Path.
 */
class CarrierCustomizationUT : BaseTest() {

    @Test
    fun validateRegulatoryInfoFilesBySalesCode() {
        Log.i("CarrierCustomizationUT", "--- Iniciando Validação de Arquivos da Carrier ---")

        // 1. Obtém informações de customização (Sales Code e OMC Path)
        val salesCode = getSystemProperty("ro.csc.sales_code")
            .ifBlank { getSystemProperty("ro.boot.sales_code") }
            .ifBlank { getSystemProperty("persist.sys.prev_salescode") }
        
        val omcPath = getSystemProperty("persist.sys.omc_path")
            .ifBlank { getSystemProperty("ro.csc.omc_path") }

        Log.i("CarrierCustomizationUT", "Sales Code detectado: $salesCode")
        Log.i("CarrierCustomizationUT", "OMC Path detectado: $omcPath")

        // 2. Constrói caminhos dinâmicos baseados nas propriedades detectadas
        val pathsToCheck = mutableSetOf(
            "/carrier/data/regulatory_info.png",
            "/carrier/data/regulatory_info_ds.png",
            "/system/carrier/regulatory_info.png",
            "/efs/carrier/regulatory_info.png"
        )

        // Adiciona caminhos baseados no OMC Path se disponível
        if (omcPath.isNotBlank()) {
            pathsToCheck.add("$omcPath/etc/regulatory_info.png")
            pathsToCheck.add("$omcPath/etc/regulatory_info_ds.png")
            pathsToCheck.add("$omcPath/res/media/regulatory_info.png")
        }

        // Adiciona caminhos baseados no Sales Code se disponível
        if (salesCode.isNotBlank()) {
            pathsToCheck.add("/system/csc/$salesCode/regulatory_info.png")
            pathsToCheck.add("/data/omc/$salesCode/etc/regulatory_info.png")
            pathsToCheck.add("/data/csc/$salesCode/regulatory_info.png")
        }

        var foundAtLeastOne = false

        pathsToCheck.forEach { path ->
            if (checkFileExistsViaShell(path)) {
                Log.i("CarrierCustomizationUT", "[SUCESSO] Arquivo encontrado em: $path")
                foundAtLeastOne = true
                getFileDetails(path)
            } else {
                Log.d("CarrierCustomizationUT", "[AVISO] Arquivo não encontrado em: $path")
            }
        }

        if (!foundAtLeastOne) {
            Log.e("CarrierCustomizationUT", "[ERRO] Nenhum arquivo regulatório encontrado para Sales Code ($salesCode) ou OMC Path ($omcPath)")
        }
        
        Log.i("CarrierCustomizationUT", "--------------------------------------------------")
    }

    private fun getFileDetails(path: String) {
        try {
            val process = Runtime.getRuntime().exec("ls -l $path")
            val details = process.inputStream.bufferedReader().use { it.readText() }.trim()
            Log.i("CarrierCustomizationUT", "Detalhes: $details")
        } catch (e: Exception) {
            Log.e("CarrierCustomizationUT", "Erro ao obter detalhes: ${e.message}")
        }
    }
}

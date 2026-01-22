package com.samsung.requirements.automatechecklisttest.ut

import android.util.Log
import com.samsung.requirements.automatechecklisttest.base.BaseUT
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Teste para validar a customização da Carrier (Operadora).
 * Verifica a existência de imagens regulatórias e se as TAGs no CSC estão corretas.
 */
class CarrierCustomizationUT : BaseUT() {

    @Test
    fun validateRegulatoryInfoAndCscTags() {
        Log.i("CarrierCustomizationUT", "--- Início da Validação de Customização ---")
        Log.i("CarrierCustomizationUT", "Sales Code detectado via BaseUT: $salesCode")

        // 1. Validação de Arquivo de Imagem no /prism
        val imagePath = "/prism/etc/carriers/single/$salesCode/regulatory_info.png"
        val imageExists = checkFileExistsViaShell(imagePath)
        
        Log.i("CarrierCustomizationUT", "Imagem Regulatória em $imagePath: ${if (imageExists) "ENCONTRADA" else "NÃO ENCONTRADA"}")

        // 2. Validação de TAG carregada automaticamente pela BaseUT
        // A BaseUT já parseou cscfeature.xml, customer_carrier_feature.json e customer.xml
        val supportRegulatoryTag = "CscFeature_Setting_SupportRegulatoryInfo"
        val tagValue = getCscTag(supportRegulatoryTag)

        Log.i("CarrierCustomizationUT", "TAG $supportRegulatoryTag encontrada no CSC: $tagValue")

        // 3. Asserções do Teste
        assertTrue("Erro: Imagem regulatória não encontrada em $imagePath", imageExists)
        assertTrue("Erro: TAG $supportRegulatoryTag deveria estar como TRUE no CSC", 
            tagValue?.equals("TRUE", ignoreCase = true) == true)

        Log.i("CarrierCustomizationUT", "--- Fim da Validação de Customização (SUCESSO) ---")
    }
}

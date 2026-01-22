package com.samsung.requirements.automatechecklisttest.base

import android.util.Log
import com.samsung.requirements.automatechecklisttest.helpers.CscDecoderHelper
import org.junit.Before

/**
 * Classe base para testes de Unidade (UT) que requerem acesso às configurações do CSC.
 * Realiza a descriptografia e o parse dos arquivos de configuração no setup.
 */
open class BaseUT : BaseTest() {

    protected var cscFeatureMap: Map<String, String> = emptyMap()
    protected var customerCarrierFeatureMap: Map<String, String> = emptyMap()
    protected var customerMap: Map<String, String> = emptyMap()
    
    protected lateinit var salesCode: String

    @Before
    override fun setup() {
        super.setup()
        
        // 1. Identifica o Sales Code
        salesCode = getSystemProperty("ro.csc.sales_code")
            .ifBlank { getSystemProperty("ro.boot.sales_code") }
        
        if (salesCode.isBlank()) {
            Log.w("BaseUT", "Sales Code não detectado. Mapas de CSC estarão vazios.")
            return
        }

        val decoderHelper = CscDecoderHelper(appContext)
        val basePath = "/optics/configs/carriers/single/$salesCode/conf"

        // 2. Processa cscfeature.xml (Criptografado)
        val cscFeaturePath = "$basePath/system/cscfeature.xml"
        cscFeatureMap = decoderHelper.parseXmlToMap(decoderHelper.decryptFile(cscFeaturePath))

        // 3. Processa customer_carrier_feature.json (Criptografado)
        val jsonPath = "$basePath/system/customer_carrier_feature.json"
        customerCarrierFeatureMap = decoderHelper.parseJsonToMap(decoderHelper.decryptFile(jsonPath))

        // 4. Processa customer.xml (Já descriptografado)
        val customerPath = "$basePath/customer.xml"
        customerMap = decoderHelper.parseXmlToMap(decoderHelper.decryptFile(customerPath))

        Log.i("BaseUT", "CSC Configs carregadas para o Sales Code: $salesCode")
        Log.d("BaseUT", "Tags em cscfeature: ${cscFeatureMap.size}")
        Log.d("BaseUT", "Tags em customer_carrier_feature: ${customerCarrierFeatureMap.size}")
        Log.d("BaseUT", "Tags em customer: ${customerMap.size}")
    }

    /**
     * Busca uma TAG em qualquer um dos mapas carregados.
     */
    protected fun getCscTag(tagName: String): String? {
        return cscFeatureMap[tagName] 
            ?: customerCarrierFeatureMap[tagName] 
            ?: customerMap[tagName]
    }
}

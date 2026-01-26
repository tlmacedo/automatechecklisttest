package com.samsung.requirements.automatechecklisttest.base;

import android.util.Log;

import com.samsung.requirements.automatechecklisttest.helpers.CscDecoderHelper;

import org.junit.Before;

import java.util.Collections;
import java.util.Map;

/**
 * Classe base para testes de Unidade (UT) que requerem acesso às configurações do CSC.
 * Realiza a descriptografia e o parse dos arquivos de configuração no setup.
 */
public class BaseUT extends BaseTest {

    protected Map<String, String> cscFeatureMap = Collections.emptyMap();
    protected Map<String, String> customerCarrierFeatureMap = Collections.emptyMap();
    protected Map<String, String> customerMap = Collections.emptyMap();
    
    protected String salesCode;

    @Before
    @Override
    public void setup() {
        super.setup();
        
        // 1. Identifica o Sales Code
        salesCode = getSystemProperty("ro.csc.sales_code");
        if (salesCode == null || salesCode.isEmpty()) {
            salesCode = getSystemProperty("ro.boot.sales_code");
        }
        
        if (salesCode == null || salesCode.isEmpty()) {
            Log.w("BaseUT", "Sales Code não detectado. Mapas de CSC estarão vazios.");
            return;
        }

        CscDecoderHelper decoderHelper = new CscDecoderHelper(appContext);
        String basePath = "/optics/configs/carriers/single/" + salesCode + "/conf";

        // 2. Processa cscfeature.xml (Criptografado)
        String cscFeaturePath = basePath + "/system/cscfeature.xml";
        cscFeatureMap = decoderHelper.parseXmlToMap(decoderHelper.decryptFile(cscFeaturePath));

        // 3. Processa customer_carrier_feature.json (Criptografado)
        String jsonPath = basePath + "/system/customer_carrier_feature.json";
        customerCarrierFeatureMap = decoderHelper.parseJsonToMap(decoderHelper.decryptFile(jsonPath));

        // 4. Processa customer.xml (Pode estar descriptografado ou não)
        String customerPath = basePath + "/customer.xml";
        customerMap = decoderHelper.parseXmlToMap(decoderHelper.decryptFile(customerPath));

        Log.i("BaseUT", "CSC Configs carregadas para o Sales Code: " + salesCode);
        Log.d("BaseUT", "Tags em cscfeature: " + cscFeatureMap.size());
        Log.d("BaseUT", "Tags em customer_carrier_feature: " + customerCarrierFeatureMap.size());
        Log.d("BaseUT", "Tags em customer: " + customerMap.size());
    }

    /**
     * Busca uma TAG em qualquer um dos mapas carregados.
     * 
     * @param tagName Nome da TAG a ser buscada.
     * @return Valor da TAG ou null se não encontrada.
     */
    protected String getCscTag(String tagName) {
        String value = cscFeatureMap.get(tagName);
        if (value == null) {
            value = customerCarrierFeatureMap.get(tagName);
        }
        if (value == null) {
            value = customerMap.get(tagName);
        }
        return value;
    }
}

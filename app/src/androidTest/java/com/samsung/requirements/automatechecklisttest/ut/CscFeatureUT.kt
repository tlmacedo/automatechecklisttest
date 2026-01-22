package com.samsung.requirements.automatechecklisttest.ut

import android.util.Log
import com.samsung.requirements.automatechecklisttest.base.BaseTest
import com.sec.omc.decoder.OmcTextDecoder
import org.junit.Test
import java.io.File

/**
 * Teste para verificar TAGs no arquivo cscfeature.xml.
 * O arquivo reside em /optics/configs/carriers/single/$salesCode/conf/system/cscfeature.xml
 * e é descriptografado usando a lib omc-text-decoder-1.2.jar (com.sec.omc.decoder.OmcTextDecoder).
 */
class CscFeatureUT : BaseTest() {

    @Test
    fun verifyCscFeatureXml() {
        Log.i("CscFeatureUT", "--- Iniciando Verificação de CSC Feature ---")

        val salesCode = getSystemProperty("ro.csc.sales_code")
            .ifBlank { getSystemProperty("ro.boot.sales_code") }
        
        if (salesCode.isBlank()) {
            Log.e("CscFeatureUT", "Sales Code não encontrado.")
            return
        }

        val cscFilePath = "/optics/configs/carriers/single/$salesCode/conf/system/cscfeature.xml"
        Log.i("CscFeatureUT", "Caminho do arquivo: $cscFilePath")

        // Verifica existência via shell (mais garantido em pastas de sistema)
        if (!checkFileExistsViaShell(cscFilePath)) {
            Log.e("CscFeatureUT", "Arquivo cscfeature.xml não encontrado em: $cscFilePath")
            return
        }

        try {
            val decoder = OmcTextDecoder()
            val cscFile = File(cscFilePath)

            // Verifica se está codificado antes de tentar o decode
            if (decoder.isXmlEncoded(cscFile)) {
                Log.i("CscFeatureUT", "Arquivo detectado como CODIFICADO. Iniciando decode...")
                
                val decodedBytes = decoder.decode(cscFile)
                if (decodedBytes != null) {
                    val decodedContent = String(decodedBytes)
                    Log.i("CscFeatureUT", "--- CONTEÚDO DESCRIPTOGRAFADO ---")
                    Log.i("CscFeatureUT", decodedContent)
                    Log.i("CscFeatureUT", "---------------------------------")
                } else {
                    Log.e("CscFeatureUT", "O decoder retornou nulo. Tentando fallback via cópia...")
                    tryFallbackWithCopy(cscFilePath)
                }
            } else {
                Log.i("CscFeatureUT", "Arquivo já é XML plano (não codificado).")
                // Se não estiver codificado, tenta ler diretamente (pode exigir root/permissão)
                val content = readRawViaShell(cscFilePath)
                Log.i("CscFeatureUT", "--- CONTEÚDO (PLAIN XML) ---")
                Log.i("CscFeatureUT", content)
            }

        } catch (e: Exception) {
            Log.e("CscFeatureUT", "Erro ao processar arquivo: ${e.message}")
            tryFallbackWithCopy(cscFilePath)
        }
    }

    /**
     * Fallback: Copia o arquivo para o diretório de cache do app para garantir 
     * que a lib tenha permissão de leitura via FileInputStream.
     */
    private fun tryFallbackWithCopy(originalPath: String) {
        Log.i("CscFeatureUT", "Iniciando fallback com cópia para cache...")
        val tempFile = File(appContext.cacheDir, "temp_cscfeature.xml")
        
        try {
            // Copia via shell para evitar AccessDeniedException do Java
            Runtime.getRuntime().exec("cp $originalPath ${tempFile.absolutePath}").waitFor()
            Runtime.getRuntime().exec("chmod 666 ${tempFile.absolutePath}").waitFor()

            val decoder = OmcTextDecoder()
            val decodedBytes = decoder.decode(tempFile)
            
            if (decodedBytes != null) {
                Log.i("CscFeatureUT", "--- CONTEÚDO (VIA FALLBACK) ---")
                Log.i("CscFeatureUT", String(decodedBytes))
                Log.i("CscFeatureUT", "-------------------------------")
            } else {
                Log.e("CscFeatureUT", "Falha no decode mesmo via fallback.")
            }
        } catch (e: Exception) {
            Log.e("CscFeatureUT", "Erro no fallback: ${e.message}")
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }

    private fun readRawViaShell(path: String): String {
        return try {
            val process = Runtime.getRuntime().exec("cat $path")
            process.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "Erro ao ler via shell: ${e.message}"
        }
    }
}

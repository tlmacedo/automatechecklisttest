package com.samsung.requirements.automatechecklisttest.helpers

import android.content.Context
import android.util.Log
import com.sec.omc.decoder.OmcTextDecoder
import org.json.JSONObject
import java.io.File

/**
 * Helper para descriptografar e parsear arquivos de configuração CSC (XML e JSON).
 */
class CscDecoderHelper(private val context: Context) {

    private val decoder = OmcTextDecoder()

    /**
     * Descriptografa um arquivo e retorna seu conteúdo como String.
     * Se o arquivo já for XML plano (não codificado), lê diretamente.
     */
    fun decryptFile(filePath: String): String? {
        val originalFile = File(filePath)
        
        try {
            // Se for XML plano, lê direto via shell para evitar problemas de permissão
            if (!decoder.isXmlEncoded(originalFile)) {
                return readRawViaShell(filePath)
            }
            
            // Tenta decode direto
            val decodedBytes = decoder.decode(originalFile)
            if (decodedBytes != null) return String(decodedBytes)
        } catch (e: Exception) {
            Log.w("CscDecoderHelper", "Acesso direto negado para $filePath, tentando fallback...")
        }

        // Fallback: Copia para cache do app
        return try {
            val tempFile = File(context.cacheDir, "temp_config_decode")
            Runtime.getRuntime().exec("cp $filePath ${tempFile.absolutePath}").waitFor()
            Runtime.getRuntime().exec("chmod 666 ${tempFile.absolutePath}").waitFor()
            
            val decodedBytes = decoder.decode(tempFile)
            tempFile.delete()
            
            decodedBytes?.let { String(it) } ?: readRawViaShell(filePath)
        } catch (e: Exception) {
            Log.e("CscDecoderHelper", "Erro ao descriptografar $filePath: ${e.message}")
            null
        }
    }

    /**
     * Converte XML (cscfeature ou customer) em um Map.
     */
    fun parseXmlToMap(xmlContent: String?): Map<String, String> {
        if (xmlContent == null) return emptyMap()
        val featureMap = mutableMapOf<String, String>()
        try {
            // Regex para capturar <Tag>Value</Tag>
            val regex = Regex("<(\\w+)>([^<]+)</\\1>")
            regex.findAll(xmlContent).forEach { match ->
                featureMap[match.groupValues[1]] = match.groupValues[2]
            }
        } catch (e: Exception) {
            Log.e("CscDecoderHelper", "Erro ao parsear XML: ${e.message}")
        }
        return featureMap
    }

    /**
     * Converte o JSON em um Map.
     */
    fun parseJsonToMap(jsonContent: String?): Map<String, String> {
        if (jsonContent == null) return emptyMap()
        val featureMap = mutableMapOf<String, String>()
        try {
            val jsonObject = JSONObject(jsonContent)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                featureMap[key] = jsonObject.optString(key)
            }
        } catch (e: Exception) {
            Log.e("CscDecoderHelper", "Erro ao parsear JSON: ${e.message}")
        }
        return featureMap
    }

    private fun readRawViaShell(path: String): String {
        return try {
            val process = Runtime.getRuntime().exec("cat $path")
            process.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) { "" }
    }
}

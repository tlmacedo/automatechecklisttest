package com.samsung.requirements.automatechecklisttest.helpers;

import android.content.Context;
import android.util.Log;

import com.sec.omc.decoder.OmcTextDecoder;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper para descriptografar e parsear arquivos de configuração CSC (XML e JSON).
 */
public class CscDecoderHelper {

    private static final String TAG = "CscDecoderHelper";
    private final Context context;
    private final OmcTextDecoder decoder;

    public CscDecoderHelper(Context context) {
        this.context = context;
        this.decoder = new OmcTextDecoder();
    }

    /**
     * Descriptografa um arquivo e retorna seu conteúdo como String.
     * Se o arquivo já for XML plano (não codificado), lê diretamente.
     */
    public String decryptFile(String filePath) {
        File originalFile = new File(filePath);

        try {
            // Se não estiver codificado, lê diretamente
            if (!decoder.isXmlEncoded(originalFile)) {
                return readRawViaShell(filePath);
            }

            // Tenta decodificar o arquivo
            byte[] decodedBytes = decoder.decode(originalFile);
            if (decodedBytes != null) {
                return new String(decodedBytes);
            }
        } catch (Exception e) {
            Log.w(TAG, "Acesso direto negado para " + filePath + ", tentando fallback via cache...");
        }

        // Fallback: Copia para o cache do app para tentar decodificar com permissões locais
        try {
            File tempFile = new File(context.getCacheDir(), "temp_config_decode");
            Runtime.getRuntime().exec("cp " + filePath + " " + tempFile.getAbsolutePath()).waitFor();
            Runtime.getRuntime().exec("chmod 666 " + tempFile.getAbsolutePath()).waitFor();

            byte[] decodedBytes = decoder.decode(tempFile);
            tempFile.delete();

            if (decodedBytes != null) {
                return new String(decodedBytes);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao descriptografar " + filePath + ": " + e.getMessage());
        }

        // Última tentativa: ler o conteúdo bruto via shell
        return readRawViaShell(filePath);
    }

    /**
     * Converte XML (cscfeature ou customer) em um Map.
     * Utiliza Regex para extrair tags simples <Tag>Valor</Tag>.
     */
    public Map<String, String> parseXmlToMap(String xmlContent) {
        Map<String, String> featureMap = new HashMap<>();
        if (xmlContent == null || xmlContent.isEmpty()) {
            return featureMap;
        }

        try {
            // Regex para capturar <Tag>Value</Tag>
            Pattern pattern = Pattern.compile("<(\\w+)>([^<]+)</\\1>");
            Matcher matcher = pattern.matcher(xmlContent);
            while (matcher.find()) {
                featureMap.put(matcher.group(1), matcher.group(2));
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao parsear XML: " + e.getMessage());
        }
        return featureMap;
    }

    /**
     * Converte uma String JSON em um Map.
     */
    public Map<String, String> parseJsonToMap(String jsonContent) {
        Map<String, String> featureMap = new HashMap<>();
        if (jsonContent == null || jsonContent.isEmpty()) {
            return featureMap;
        }

        try {
            JSONObject jsonObject = new JSONObject(jsonContent);
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                featureMap.put(key, jsonObject.optString(key));
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao parsear JSON: " + e.getMessage());
        }
        return featureMap;
    }

    /**
     * Lê o conteúdo de um arquivo via comando 'cat' do shell.
     */
    private String readRawViaShell(String path) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("cat " + path);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao ler via shell: " + path);
        }
        return output.toString().trim();
    }
}

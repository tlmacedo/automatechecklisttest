package com.example.helloapp;

import android.content.Context;
import android.util.Log;

import com.sec.omc.decoder.OmcTextDecoder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class CscDecoderHelper {

    private static final String TAG = "CscDecoderHelper";
    private final Context context;
    private final OmcTextDecoder decoder;

    public CscDecoderHelper(Context context) {
        this.context = context;
        this.decoder = new OmcTextDecoder();
    }

    public String decryptFile(String filePath) {
        File originalFile = new File(filePath);
        try {
            if (!decoder.isXmlEncoded(originalFile)) {
                return readRawViaShell(filePath);
            }
            byte[] decodedBytes = decoder.decode(originalFile);
            if (decodedBytes != null) {
                return new String(decodedBytes);
            }
        } catch (Exception e) {
            Log.w(TAG, "Erro decodificação: " + filePath);
        }
        return readRawViaShell(filePath);
    }

    public Map<String, List<Map.Entry<String, String>>> parseXmlToGroups(String xmlContent) {
        Map<String, List<Map.Entry<String, String>>> groups = new LinkedHashMap<>();
        if (xmlContent == null || xmlContent.isEmpty()) return groups;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("*");
            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (isTextNode(element)) {
                        String fullPath = getParentPath(element);
                        List<Map.Entry<String, String>> items = groups.computeIfAbsent(fullPath, k -> new ArrayList<>());
                        items.add(new HashMap.SimpleEntry<>(element.getTagName(), element.getTextContent().trim()));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro parse XML: " + e.getMessage());
        }
        return groups;
    }

    public Map<String, String> parseXmlToMap(String xmlContent) {
        Map<String, String> featureMap = new TreeMap<>();
        if (xmlContent == null || xmlContent.isEmpty()) return featureMap;
        try {
            Pattern pattern = Pattern.compile("<(\\w+)>([^<]+)</\\1>");
            Matcher matcher = pattern.matcher(xmlContent);
            while (matcher.find()) {
                featureMap.put(matcher.group(1), matcher.group(2));
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro XML simples: " + e.getMessage());
        }
        return featureMap;
    }

    public Map<String, String> parseJsonToMap(String jsonContent) {
        Map<String, String> featureMap = new TreeMap<>();
        if (jsonContent == null || jsonContent.isEmpty()) return featureMap;
        try {
            JSONObject jsonObject = new JSONObject(jsonContent);
            flattenJsonToMap(jsonObject, "", featureMap);
        } catch (Exception e) {
            Log.e(TAG, "Erro JSON simples: " + e.getMessage());
        }
        return featureMap;
    }

    private void flattenJsonToMap(Object element, String prefix, Map<String, String> map) {
        try {
            if (element instanceof JSONObject) {
                JSONObject obj = (JSONObject) element;
                Iterator<String> keys = obj.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = obj.get(key);
                    String newPrefix = prefix.isEmpty() ? key : prefix + "." + key;
                    flattenJsonToMap(value, newPrefix, map);
                }
            } else if (element instanceof JSONArray) {
                JSONArray array = (JSONArray) element;
                for (int i = 0; i < array.length(); i++) {
                    flattenJsonToMap(array.get(i), prefix + "[" + i + "]", map);
                }
            } else {
                map.put(prefix, element.toString());
            }
        } catch (Exception e) { }
    }

    private boolean isTextNode(Element element) {
        NodeList children = element.getChildNodes();
        if (children.getLength() == 1 && children.item(0).getNodeType() == Node.TEXT_NODE) return true;
        return children.getLength() == 0;
    }

    private String getParentPath(Node node) {
        Node parent = node.getParentNode();
        if (parent == null || parent.getNodeType() == Node.DOCUMENT_NODE || parent.getNodeName().equals("#document")) {
            return "Raiz";
        }
        String parentName = parent.getNodeName();
        String path = getParentPath(parent);
        return path.equals("Raiz") ? parentName : path + " > " + parentName;
    }

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
        } catch (Exception e) { }
        return output.toString().trim();
    }
}

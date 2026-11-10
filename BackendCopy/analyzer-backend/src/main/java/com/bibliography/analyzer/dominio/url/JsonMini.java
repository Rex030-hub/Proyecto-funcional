package com.bibliography.analyzer.dominio.url;

import java.util.ArrayList;
import java.util.List;

public final class JsonMini {
    private JsonMini() {}

    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public static String field(String json, String fieldName) {
        int q = findStringStart(json, fieldName, 0);
        return q >= 0 ? parseStringAt(json, q) : "";
    }

    public static List<String> urlsFromResults(String json) {
        List<String> urls = new ArrayList<>();
        int key = json.indexOf("\"results\"");
        if (key < 0) return urls;
        int open = json.indexOf('[', key);
        if (open < 0) return urls;
        int i = open + 1, depth = 0, objStart = -1;
        boolean inString = false, escaped = false;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (inString) {
                if (escaped) escaped = false;
                else if (c == '\\') escaped = true;
                else if (c == '"') inString = false;
            } else {
                if (c == '"') inString = true;
                else if (c == '{') { if (depth == 0) objStart = i; depth++; }
                else if (c == '}') {
                    depth--;
                    if (depth == 0 && objStart >= 0) {
                        String url = field(json.substring(objStart, i + 1), "url");
                        if (!url.isEmpty()) urls.add(url);
                        objStart = -1;
                    }
                }
                else if (c == ']' && depth == 0) break;
            }
            i++;
        }
        return urls;
    }

    private static int findStringStart(String json, String fieldName, int from) {
        String key = "\"" + fieldName + "\"";
        int k = json.indexOf(key, from);
        while (k >= 0) {
            int j = k + key.length();
            while (j < json.length() && Character.isWhitespace(json.charAt(j))) j++;
            if (j < json.length() && json.charAt(j) == ':') {
                j++;
                while (j < json.length() && Character.isWhitespace(json.charAt(j))) j++;
                if (j < json.length() && json.charAt(j) == '"') return j;
                return -1;
            }
            k = json.indexOf(key, k + 1);
        }
        return -1;
    }

    private static String parseStringAt(String json, int quoteIndex) {
        StringBuilder sb = new StringBuilder();
        int i = quoteIndex + 1;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '"') return sb.toString();
            if (c == '\\' && i + 1 < json.length()) {
                char e = json.charAt(i + 1);
                switch (e) {
                    case '"'  -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/'  -> sb.append('/');
                    case 'n'  -> sb.append('\n');
                    case 't'  -> sb.append('\t');
                    case 'r'  -> sb.append('\r');
                    case 'b'  -> sb.append('\b');
                    case 'f'  -> sb.append('\f');
                    case 'u'  -> {
                        if (i + 5 < json.length()) {
                            sb.append((char) Integer.parseInt(json.substring(i + 2, i + 6), 16));
                            i += 4;
                        }
                    }
                    default -> sb.append(e);
                }
                i += 2;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }
}
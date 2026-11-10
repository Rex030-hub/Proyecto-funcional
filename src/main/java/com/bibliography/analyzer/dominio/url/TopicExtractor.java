package com.bibliography.analyzer.dominio.url;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class TopicExtractor {
    private TopicExtractor() {}

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
        "de", "la", "que", "el", "en", "y", "a", "los", "del", "se", "las", "por", "un", "para", "con", "no", "una",
        "su", "al", "lo", "como", "más", "pero", "sus", "le", "ya", "o", "este", "sí", "porque", "esta", "entre",
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i", "it", "for", "not", "on", "with", "he", "as",
        "you", "do", "at", "this", "but", "his", "by", "from", "they", "we", "say", "her", "she", "or", "an", "will"
    ));

    public static String extractMainTopic(String text, String title) {
        if (text == null || text.isBlank()) {
            return title != null ? title : "Indeterminado";
        }
        String[] words = text.toLowerCase()
                .replaceAll("[^a-záéíóúñü0-9\\s]", " ")
                .split("\\s+");
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String word : words) {
            if (word.length() > 3 && !STOPWORDS.contains(word)) {
                frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
            }
        }
        List<String> topKeywords = frequencyMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (topKeywords.isEmpty()) return title;
        return topKeywords.stream()
                .map(w -> w.substring(0, 1).toUpperCase() + w.substring(1))
                .collect(Collectors.joining(" / "));
    }
}
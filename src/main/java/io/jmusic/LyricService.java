package io.jmusic;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class LyricService {

    public List<VerseAnalysis> analyzeLyrics(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // Split by empty lines (one or more)
        String[] verseBlocks = text.split("(?m)^\\s*$");
        List<VerseAnalysis> results = new ArrayList<>();

        for (int i = 0; i < verseBlocks.length; i++) {
            String block = verseBlocks[i].trim();
            if (!block.isEmpty()) {
                results.add(analyzeVerseBlock(block, results.size() + 1));
            }
        }
        
        return results;
    }

    private VerseAnalysis analyzeVerseBlock(String block, int verseNum) {
        String[] lines = block.split("\\R");
        List<LineAnalysis> lineResults = new ArrayList<>();
        Map<String, String> rhymeMap = new HashMap<>();
        char nextRhymeLabel = 'A';

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            int syllables = countSyllablesWithSinalefa(trimmed);
            String rhymePart = extractRhymePart(trimmed);
            boolean hasSinalefa = detectSinalefa(trimmed);
            
            LineAnalysis la = new LineAnalysis(syllables, syllables, rhymePart, suggestRhymes(rhymePart), hasSinalefa, "");
            lineResults.add(la);
        }

        // Assign rhyme labels (A, B, C...) within the verse
        for (int i = 0; i < lineResults.size(); i++) {
            LineAnalysis la = lineResults.get(i);
            String rhyme = la.rhyme();
            if (rhyme.isEmpty()) continue;

            if (!rhymeMap.containsKey(rhyme)) {
                rhymeMap.put(rhyme, String.valueOf(nextRhymeLabel++));
            }
            lineResults.set(i, la.withRhymeTag(rhymeMap.get(rhyme)));
        }

        return new VerseAnalysis(verseNum, lineResults);
    }

    private int countSyllablesWithSinalefa(String text) {
        text = text.toLowerCase().trim();
        if (text.isEmpty()) return 0;
        
        String[] words = text.split("\\s+");
        int total = 0;
        for (String word : words) {
            total += countWordSyllables(word);
        }
        
        int sinalefas = countSinalefas(words);
        return Math.max(1, total - sinalefas);
    }

    private int countWordSyllables(String word) {
        word = word.replaceAll("[^a-záéíóúüñ]", "");
        if (word.isEmpty()) return 0;
        Pattern p = Pattern.compile("[aeiouáéíóúü]+");
        Matcher m = p.matcher(word);
        int count = 0;
        while (m.find()) {
            count++;
        }
        return count;
    }

    private boolean detectSinalefa(String verse) {
        String[] words = verse.toLowerCase().trim().split("\\s+");
        return countSinalefas(words) > 0;
    }

    private int countSinalefas(String[] words) {
        if (words.length < 2) return 0;
        int count = 0;
        for (int i = 0; i < words.length - 1; i++) {
            if (isSinalefa(words[i], words[i+1])) {
                count++;
            }
        }
        return count;
    }

    private boolean isSinalefa(String w1, String w2) {
        if (w1.isEmpty() || w2.isEmpty()) return false;
        char last = w1.charAt(w1.length() - 1);
        char first = w2.charAt(0);
        String vowels = "aeiouáéíóúü";
        boolean endsInVowel = vowels.indexOf(last) != -1 || last == 'y';
        if (w2.startsWith("h") && w2.length() > 1) {
            first = w2.charAt(1);
        }
        boolean startsInVowel = vowels.indexOf(first) != -1;
        return endsInVowel && startsInVowel;
    }

    private String extractRhymePart(String verse) {
        String lastWord = getLastWord(verse);
        if (lastWord.isEmpty()) return "";
        int len = lastWord.length();
        if (len <= 2) return lastWord;
        return lastWord.substring(len - 3);
    }

    private String getLastWord(String verse) {
        String[] words = verse.trim().split("\\s+");
        return words.length > 0 ? words[words.length - 1].replaceAll("[^a-záéíóúüñ]", "").toLowerCase() : "";
    }

    private List<String> suggestRhymes(String rhymePart) {
        if (rhymePart.isEmpty()) return new ArrayList<>();
        return Arrays.asList("vida", "herida", "partida", "medida"); 
    }

    public static record LineAnalysis(
        int syllables, 
        int metric, 
        String rhyme, 
        List<String> suggestions, 
        boolean hasSinalefa, 
        String rhymeTag
    ) {
        public LineAnalysis withRhymeTag(String tag) {
            return new LineAnalysis(syllables, metric, rhyme, suggestions, hasSinalefa, tag);
        }
    }

    public static record VerseAnalysis(int verseNumber, List<LineAnalysis> lines) {}
}

import java.util.*;

/** Basic NLP preprocessing: lowercase, strip punctuation, remove stop words, normalize, stem. */
public class Tokenizer {
    private static final Set<String> STOP = new HashSet<>(Arrays.asList(
            "a", "an", "the", "is", "are", "was", "were", "be", "what", "whats", "how", "do", "does", "did",
            "i", "you", "me", "my", "we", "to", "of", "in", "on", "and", "or", "can", "could", "tell",
            "about", "please", "it", "this", "that", "with", "will", "should", "get", "there", "if", "so"));

    private static final Map<String, String> SYNONYMS = new HashMap<>();
    static {
        SYNONYMS.put("submission", "submit");
        SYNONYMS.put("intern", "internship");
        SYNONYMS.put("cert", "certificate");
        SYNONYMS.put("explain", "define");
        SYNONYMS.put("meaning", "define");
        SYNONYMS.put("difference", "differ");
        SYNONYMS.put("vs", "differ");
        SYNONYMS.put("versus", "differ");
    }

    public static List<String> tokens(String text) {
        List<String> out = new ArrayList<>();
        for (String w : text.toLowerCase().replaceAll("[^a-z0-9 ]", " ").split("\\s+")) {
            if (w.isEmpty() || STOP.contains(w)) continue;
            out.add(stem(SYNONYMS.getOrDefault(w, w)));
        }
        return out;
    }

    /** Light suffix-stripping stemmer. */
    static String stem(String w) {
        if (w.length() <= 3) return w;
        if (w.endsWith("ies") && w.length() > 4) return w.substring(0, w.length() - 3) + "y";
        if (w.endsWith("sses")) return w.substring(0, w.length() - 2);
        if (w.endsWith("ing") && w.length() > 5) return undouble(w.substring(0, w.length() - 3));
        if (w.endsWith("ed") && w.length() > 4) return undouble(w.substring(0, w.length() - 2));
        if (w.endsWith("s") && !w.endsWith("ss") && !w.endsWith("us")) return w.substring(0, w.length() - 1);
        return w;
    }

    private static String undouble(String w) {
        int n = w.length();
        if (n > 2 && w.charAt(n - 1) == w.charAt(n - 2) && "tnpgdm".indexOf(w.charAt(n - 1)) >= 0)
            return w.substring(0, n - 1);
        return w;
    }
}
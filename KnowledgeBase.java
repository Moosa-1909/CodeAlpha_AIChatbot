import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Question/answer store with TF-IDF vectors. A user message is matched to the closest
 * stored question using cosine similarity. New entries can be taught at runtime.
 */
public class KnowledgeBase {
    public static class Match {
        public final String question;
        public final String answer;
        public final double score;

        Match(String question, String answer, double score) {
            this.question = question;
            this.answer = answer;
            this.score = score;
        }
    }

    private static class Entry {
        final String question, answer;
        final List<String> tokens;
        Map<String, Double> vector = new HashMap<>();

        Entry(String q, String a) {
            question = q;
            answer = a;
            tokens = Tokenizer.tokens(q);
        }
    }

    private final List<Entry> entries = new ArrayList<>();
    private final Map<String, Double> idf = new HashMap<>();
    private final Path learnedFile;

    public KnowledgeBase(Path learnedFile) { this.learnedFile = learnedFile; }

    public int size() { return entries.size(); }

    /** Loads lines of the form: question | answer  (# comments and blank lines are skipped). */
    public void load(Path file) {
        if (!Files.exists(file)) return;
        try {
            for (String line : Files.readAllLines(file)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("|")) continue;
                int i = line.indexOf('|');
                entries.add(new Entry(line.substring(0, i).trim(), line.substring(i + 1).trim()));
            }
        } catch (IOException e) {
            System.out.println("Warning: could not read " + file + ": " + e.getMessage());
        }
        rebuild();
    }

    /** Teaches a new Q/A pair and saves it so it is remembered after a restart. */
    public void teach(String question, String answer) {
        question = question.replace("|", "/").trim();
        answer = answer.replace("|", "/").trim();
        if (Tokenizer.tokens(question).isEmpty()) throw new IllegalArgumentException("That question is too vague to learn.");
        entries.add(new Entry(question, answer));
        rebuild();
        try {
            Files.write(learnedFile, Collections.singletonList(question + " | " + answer),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Warning: could not save learned entry: " + e.getMessage());
        }
    }

    private void rebuild() {
        Map<String, Integer> df = new HashMap<>();
        for (Entry e : entries)
            for (String t : new HashSet<>(e.tokens)) df.merge(t, 1, Integer::sum);
        idf.clear();
        int n = entries.size();
        for (Map.Entry<String, Integer> d : df.entrySet())
            idf.put(d.getKey(), Math.log((n + 1.0) / (d.getValue() + 1.0)) + 1.0);
        for (Entry e : entries) e.vector = vectorize(e.tokens);
    }

    private Map<String, Double> vectorize(List<String> tokens) {
        Map<String, Double> v = new HashMap<>();
        for (String t : tokens) {
            Double w = idf.get(t);
            if (w != null) v.merge(t, w, Double::sum); // tf * idf
        }
        double norm = 0;
        for (double x : v.values()) norm += x * x;
        norm = Math.sqrt(norm);
        if (norm > 0) for (Map.Entry<String, Double> x : v.entrySet()) x.setValue(x.getValue() / norm);
        return v;
    }

    /** Returns the best matching entry, or null if nothing shares any vocabulary with the query. */
    public Match find(String query) {
        Map<String, Double> q = vectorize(correctTypos(Tokenizer.tokens(query)));
        if (q.isEmpty()) return null;
        Entry best = null;
        double bestScore = 0;
        for (Entry e : entries) {
            double dot = 0;
            for (Map.Entry<String, Double> x : q.entrySet()) {
                Double w = e.vector.get(x.getKey());
                if (w != null) dot += x.getValue() * w;
            }
            if (dot > bestScore) { bestScore = dot; best = e; }
        }
        return best == null ? null : new Match(best.question, best.answer, bestScore);
    }

    /** Replaces unknown words with the closest known word (small edit distance) to tolerate typos. */
    private List<String> correctTypos(List<String> tokens) {
        List<String> out = new ArrayList<>();
        for (String t : tokens) {
            if (idf.containsKey(t) || t.length() < 5) { out.add(t); continue; }
            int limit = t.length() >= 7 ? 2 : 1;
            String best = null;
            int bestDist = limit + 1;
            for (String known : idf.keySet()) {
                if (known.length() < 4 || Math.abs(known.length() - t.length()) > limit) continue;
                int d = editDistance(t, known);
                if (d < bestDist) { bestDist = d; best = known; }
            }
            out.add(best != null ? best : t);
        }
        return out;
    }

    private static int editDistance(String a, String b) {
        int[] prev = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            int[] cur = new int[b.length() + 1];
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            prev = cur;
        }
        return prev[b.length()];
    }
}
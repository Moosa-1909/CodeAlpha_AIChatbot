import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Conversation logic: rule-based intents first, then NLP-based FAQ matching, then a fallback. */
public class ChatBot {
    private static final double CONFIDENT = 0.45;
    private static final double MAYBE = 0.20;

    private static final Pattern GREETING = Pattern.compile("(?i)^\\s*(hi+|hello+|hey+|namaste|good (morning|afternoon|evening))\\b.*");
    private static final Pattern BYE = Pattern.compile("(?i)^\\s*(bye|goodbye|see you|exit|quit)\\b.*");
    private static final Pattern THANKS = Pattern.compile("(?i).*\\b(thanks|thank you|thx)\\b.*");
    private static final Pattern SET_NAME = Pattern.compile("(?i)^\\s*(my name is|call me)\\s+([A-Za-z]+).*");
    private static final Pattern GET_NAME = Pattern.compile("(?i).*\\b(what('s| is) my name|who am i)\\b.*");
    private static final Pattern TIME = Pattern.compile("(?i).*\\b(what time|current time|time now|time is it)\\b.*");
    private static final Pattern DATE = Pattern.compile("(?i).*\\b(today'?s date|what('s| is) the date|what day)\\b.*");
    private static final Pattern HELP = Pattern.compile("(?i)^\\s*(help|what can you do)\\s*\\??\\s*$");
    private static final Pattern TEACH = Pattern.compile("(?i)^\\s*(teach|learn)\\s*:\\s*(.+?)\\s*=>\\s*(.+)$");

    private final KnowledgeBase kb;
    private final Map<String, String> names = new HashMap<>(); // per-session memory
    private final Random random = new Random();

    public ChatBot() {
        kb = new KnowledgeBase(Paths.get("learned.txt"));
        kb.load(Paths.get("faqs.txt"));
        kb.load(Paths.get("learned.txt"));
    }

    public int knowledgeSize() { return kb.size(); }

    public synchronized String reply(String sessionId, String message) {
        String msg = message == null ? "" : message.trim();
        if (msg.isEmpty()) return "Say something and I'll do my best to help!";

        Matcher m;
        if ((m = TEACH.matcher(msg)).matches()) {
            try {
                kb.teach(m.group(2), m.group(3));
                return "Got it! I've learned that. Ask me again and I'll answer.";
            } catch (IllegalArgumentException e) {
                return e.getMessage();
            }
        }
        if ((m = SET_NAME.matcher(msg)).matches()) {
            String n = capitalize(m.group(2));
            names.put(sessionId, n);
            return "Nice to meet you, " + n + "!";
        }
        if (GET_NAME.matcher(msg).matches()) {
            String n = names.get(sessionId);
            return n != null ? "Your name is " + n + "." : "I don't know yet. Tell me with: my name is <name>";
        }
        if (GREETING.matcher(msg).matches()) {
            String n = names.get(sessionId);
            return pick("Hello", "Hi there", "Hey") + (n != null ? ", " + n : "") + "! Ask me about Java, the internship, or type 'help'.";
        }
        if (BYE.matcher(msg).matches()) return "Goodbye! Happy coding.";
        if (THANKS.matcher(msg).matches()) return pick("You're welcome!", "Happy to help!", "Anytime!");
        if (TIME.matcher(msg).matches()) return "It's " + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) + " on the server.";
        if (DATE.matcher(msg).matches()) return "Today is " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")) + ".";
        if (HELP.matcher(msg).matches())
            return "I can answer questions about Java, OOP and the CodeAlpha internship. "
                    + "I also know the time and date, and I remember your name (say: my name is ...). "
                    + "You can teach me new things with:  teach: your question => the answer";

        KnowledgeBase.Match best = kb.find(msg);
        if (best != null && best.score >= CONFIDENT) return best.answer;
        if (best != null && best.score >= MAYBE)
            return "I'm not sure I understood. Did you mean: \"" + best.question + "\"? Ask it again and I'll answer.";
        return "Sorry, I don't know that yet. You can teach me:  teach: your question => the answer";
    }

    private String pick(String... options) { return options[random.nextInt(options.length)]; }

    private static String capitalize(String s) { return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase(); }
}

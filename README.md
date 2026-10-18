# 🤖 AI Chatbot

A Java chatbot with a real **Natural Language Processing** pipeline and a **web interface**, built with zero external dependencies. It answers questions about Java, OOP and the CodeAlpha internship, tolerates typos, remembers your name, and **learns new answers at runtime**.

> Built as **Task 3** of the **CodeAlpha Java Programming Internship**.

---

## ✨ Features

| Feature | Details |
|---|---|
| **NLP pipeline** | Lowercasing, punctuation removal, stop-word filtering, synonym mapping and stemming |
| **Machine-learning style matching** | TF-IDF vectors compared with cosine similarity to find the closest known question |
| **Rule-based intents** | Greetings, thanks, goodbye, time, date, help, name memory |
| **Typo tolerance** | Unknown words are corrected using edit distance against the known vocabulary |
| **Trainable** | Teach it at runtime with `teach: question => answer`; learned answers are saved and survive restarts |
| **Confidence handling** | Confident match → answers; weak match → asks "Did you mean ...?"; no match → asks to be taught |
| **Web interface** | Clean chat UI (light/dark mode) served by Java's built-in HTTP server |
| **Per-session memory** | Each browser tab keeps its own conversation context (e.g. your name) |
| **Console mode** | Run in the terminal with `java Main console` |

---

## 🧠 How it works

```
User message
     │
     ▼
Rule-based intents ── greeting / name / time / date / help / teach
     │ (no rule matched)
     ▼
Tokenizer ── lowercase → strip punctuation → remove stop words → synonyms → stem
     │
     ▼
Typo correction ── edit distance against known vocabulary
     │
     ▼
TF-IDF vector ── cosine similarity against every stored question
     │
     ▼
score ≥ 0.45 → answer  |  0.20–0.45 → "Did you mean...?"  |  else → ask to teach me
```

- **TF-IDF** weights each word by how often it appears in a question (term frequency) times how rare it is across all questions (inverse document frequency), so distinctive words like *polymorphism* count more than common ones.
- **Cosine similarity** measures the angle between the message vector and each stored question vector; closer to 1 means more similar.

---

## 🧱 Project Structure

```
.
├── Main.java           # Entry point: web mode (default) or console mode
├── WebServer.java      # HTTP server: serves the page and the /chat endpoint
├── ChatBot.java        # Conversation logic: intents, thresholds, fallback
├── KnowledgeBase.java  # TF-IDF index, cosine similarity, typo correction, learning
├── Tokenizer.java      # Text preprocessing and stemming
├── faqs.txt            # Training data (question | answer), easy to extend
├── index.html          # Chat web interface
└── README.md
```

---

## 🚀 Getting Started

### Requirements

- Java **11 or higher** (JDK)

### Run the web chatbot

```bash
git clone https://github.com/Moosa-1909/CodeAlpha_AIChatbot.git
cd CodeAlpha_AIChatbot
javac *.java
java Main
```

Then open **http://localhost:8080** in your browser. To use another port: `java Main 9000`.

> Run it from the project folder so it can find `faqs.txt` and `index.html`.

### Run in the terminal instead

```bash
java Main console
```

### Run in IntelliJ IDEA

Open the folder, open `Main.java`, and click ▶ next to `main`. Add `console` under *Run → Edit Configurations → Program arguments* for terminal mode.

---

## 💬 Example conversation

```
You: hello
Bot: Hello! Ask me about Java, the internship, or type 'help'.

You: my name is Moosa
Bot: Nice to meet you, Moosa!

You: what is the difference between jdk and jre
Bot: The JVM runs bytecode, the JRE is the JVM plus the standard libraries needed
     to run programs, and the JDK is the JRE plus development tools like javac.

You: Whats inheritence            ← typo handled
Bot: Inheritance lets a class reuse and extend another class using the extends keyword...

You: what is gradle
Bot: Sorry, I don't know that yet. You can teach me:  teach: your question => the answer

You: teach: what is gradle => A build automation tool for Java projects.
Bot: Got it! I've learned that. Ask me again and I'll answer.

You: tell me about gradle
Bot: A build automation tool for Java projects.
```

### Extending the knowledge base

Add lines to `faqs.txt` in the format `question | answer`, or teach the bot live. Live-taught entries are stored in `learned.txt`.

---

## 🔮 Possible Improvements

- Replace TF-IDF with word embeddings or a call to a language-model API
- Persist conversation history per user
- Add intent classification with a trained model
- Unit tests for the tokenizer and similarity scoring
- Admin page to review and edit learned answers

---

## 👤 Author

**Moosa**
B.E. Computer Science, Muffakham Jah College of Engineering and Technology
GitHub: [@Moosa-1909](https://github.com/Moosa-1909)

*Created as part of the CodeAlpha Internship program.*

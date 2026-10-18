import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/** Serves the chat page and a /chat endpoint using the JDK's built-in HTTP server. */
public class WebServer {
    private final ChatBot bot;
    private final int port;

    public WebServer(ChatBot bot, int port) {
        this.bot = bot;
        this.port = port;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", ex -> {
            if (!ex.getRequestURI().getPath().equals("/")) { send(ex, 404, "text/plain; charset=utf-8", "Not found"); return; }
            try {
                byte[] html = Files.readAllBytes(Paths.get("index.html"));
                send(ex, 200, "text/html; charset=utf-8", html);
            } catch (IOException e) {
                send(ex, 500, "text/plain; charset=utf-8", "index.html not found. Run the app from the project folder.");
            }
        });

        server.createContext("/chat", ex -> {
            if (!ex.getRequestMethod().equalsIgnoreCase("POST")) { send(ex, 405, "text/plain; charset=utf-8", "Use POST"); return; }
            String session = ex.getRequestHeaders().getFirst("X-Session");
            if (session == null || session.length() > 64) session = "anonymous";
            String message = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (message.length() > 500) message = message.substring(0, 500);
            send(ex, 200, "text/plain; charset=utf-8", bot.reply(session, message));
        });

        server.start();
        System.out.println("Chatbot running at http://localhost:" + port + "  (Ctrl+C to stop)");
    }

    private static void send(HttpExchange ex, int code, String type, String body) throws IOException {
        send(ex, code, type, body.getBytes(StandardCharsets.UTF_8));
    }

    private static void send(HttpExchange ex, int code, String type, byte[] body) throws IOException {
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(code, body.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(body); }
    }
}

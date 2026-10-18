import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        ChatBot bot = new ChatBot();
        System.out.println("Loaded " + bot.knowledgeSize() + " knowledge entries.");

        if (args.length > 0 && args[0].equalsIgnoreCase("console")) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Console mode. Type 'bye' to quit.");
            while (true) {
                System.out.print("You: ");
                if (!sc.hasNextLine()) break;
                String line = sc.nextLine();
                System.out.println("Bot: " + bot.reply("console", line));
                if (line.trim().toLowerCase().matches("^(bye|goodbye|exit|quit)\\b.*")) break;
            }
        } else {
            int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
            new WebServer(bot, port).start();
        }
    }
}

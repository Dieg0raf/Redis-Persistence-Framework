package com.ecs160;

import com.ecs160.persistence.Session;
// import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Scanner;
import java.util.List;

public class ScannerManager {
    private Scanner scanner = null;
    private Session curSession = null;
    private PostPrinter postPrinter = null;

    public ScannerManager(Session curSession) {
        this.scanner = new Scanner(System.in);
        this.postPrinter = new PostPrinter();
        this.curSession = curSession;
    }

    public void handleUserInput() {
        int amountOfIdKeys = this.curSession.getAmountOfKeys();
        while (true) {
            int id = promptForPostId();

            if (isExitCommand(id)) {
                System.out.println("Exiting...");
                break;
            }

            if (isInvalidId(id, amountOfIdKeys)) {
                System.out.println("ID out of range. Please try again!");
                continue;
            }

            // create post and process it
            processPostById(id);
        }
        scanner.close();
    }

    private int promptForPostId() {
        System.out.println("\n---------------------------");
        System.out.print("Enter Post ID (or 0 to quit): ");
        return scanner.nextInt();
    }

    private boolean isExitCommand(int id) {
        return id == 0;
    }

    private boolean isInvalidId(int id, int maxId) {
        return id < 1 || id > maxId;
    }

    private void processPostById(int id) {
        Post p = new Post();
        p.setPostId(id);
        p = (Post) curSession.load(p);

        // Print main post content first
        System.out.println("\nMAIN POST CONTENT:");
        System.out.println("└─ " + p.getPostContent());

        // Ask user if they want to load replies
        System.out.print("\nPress Enter to load replies (or type 'q' to skip): ");
        scanner.nextLine(); // consume the previous newline
        String input = scanner.nextLine();

        if (!input.equalsIgnoreCase("q")) {
            // This will trigger lazy loading of replies
            List<Post> replies = p.getReplies();
            if (replies.isEmpty()) {
                System.out.println("\n(No replies yet.)");
            } else {
                System.out.println("\nREPLIES:");
                for (int i = 0; i < replies.size(); i++) {
                    Post reply = replies.get(i);
                    String prefix = (i == replies.size() - 1) ? "   └── " : "   ├── ";
                    System.out.println(prefix + reply.getPostContent());
                }
            }
        }
    }
}

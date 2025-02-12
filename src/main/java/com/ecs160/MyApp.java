package com.ecs160;


import com.ecs160.persistence.Session;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Scanner;

public class MyApp {
    private final JsonParser jsonParser;
    private final ConfigManager configManager;

    public MyApp(JsonParser jsonParser, ConfigManager configManager) {
        this.jsonParser = jsonParser;
        this.configManager = configManager;
    }

    public static void main(String[] args) throws FileNotFoundException, URISyntaxException, NoSuchFieldException {
        JsonParser jsonParser = new JsonParser();
        ConfigManager configManager = new ConfigManager();
        MyApp driver = new MyApp(jsonParser,configManager);

        // run the whole program flow
        driver.run(args);
    }

    public void run(String[] args) {
        Session curSesh = new Session();
        String filePath = getFilePath(args);
        List<Post> allPosts = parseJsonFile(filePath);

        curSesh.clearDB(); // before starting program, clear db to restart

        for (Post post: allPosts) {
            curSesh.add(post); // add to session list
        }

        curSesh.persistAll(); // persist to db

        Scanner scanner = new Scanner(System.in);
        int amountOfIdKeys = curSesh.getAmountOfKeys();
        while (true) {
            System.out.println("\n---------------------------");
            System.out.print("Enter Post ID (or 0 to quit): ");
            int id = scanner.nextInt();

            if (id == 0) {
                System.out.println("Exiting...");
                break;
            }

            if (id < 1 || id > amountOfIdKeys) {
                System.out.println("ID out of range. Please try again!");
                continue;
            }

            Post p = new Post();
            p.setPostId(id);
            p = (Post) curSesh.load(p);
            printQueryContent(p);
        }
        scanner.close();

    }

    private void printQueryContent(Post post) {
        System.out.println("\n===========================");

        if (post.getParentPostId() != -1) {
            System.out.println("NOTE: This is a reply post to parent post ID: " + post.getParentPostId());
            System.out.println("Reply Content: [" + post.getPostContent() + "]");
        } else {
            System.out.println("MAIN POST:");
            System.out.println("└─ " + post.getPostContent());

            if (!post.getReplies().isEmpty()) {
                System.out.println("\nREPLIES:");
                for (int i = 0; i < post.getReplies().size(); i++) {
                    Post replyPost = post.getReplies().get(i);
                    String replyContent = replyPost.getPostContent() != null ? replyPost.getPostContent() : "[Empty reply]";
                    String prefix = (i == post.getReplies().size() - 1) ? "   └── " : "   ├── ";
                    System.out.println(prefix + replyContent);
                }
            } else {
                System.out.println("\n(No replies yet.)");
            }
        }
        System.out.println("===========================");
    }

    private String getFilePath(String[] args) {
        return this.configManager.getFilePathFromArgs(args);
    }

    private List<Post> parseJsonFile(String filePath) {
        return this.jsonParser.parseJson(filePath);
    }


}

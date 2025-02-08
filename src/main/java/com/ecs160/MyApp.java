package com.ecs160;


import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.List;

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
        String filePath = getFilePath(args);
        List<Post> allPosts = parseJsonFile(filePath);
        for (Post post: allPosts) {
            if (post instanceof ThreadPost) {
                List<Post> replyPosts = ((ThreadPost) post).getReplies();
                System.out.println("Thread Post Id: " + post.getPostId());
                System.out.println("Thread Post reply count: " + replyPosts.size());
            } else {
                System.out.println("Standalone Post Id: " + post.getPostId());
            }
        }
        System.out.println("Amount of thread posts: " + allPosts.size());
    }

    private String getFilePath(String[] args) {
        return this.configManager.getFilePathFromArgs(args);
    }

    private List<Post> parseJsonFile(String filePath) {
        return this.jsonParser.parseJson(filePath);
    }


}

package com.ecs160;


import com.ecs160.persistence.Session;

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
        Session curSesh = new Session();
        String filePath = getFilePath(args);
        List<Post> allPosts = parseJsonFile(filePath);
        for (Post post: allPosts) {
//            System.out.println("Size of replies: " + post.getRepliesSize());
//            if (post.getRepliesSize() > 0) {
//                List<Post> replyPosts = post.getReplies();
//                System.out.println("Thread Post Id: " + post.getPostId());
//                for (Post replyPost: post.getReplies()) {
//                    System.out.println("-----> ReplyPost id: " + replyPost.getPostId());
//                }
//                System.out.println();
//            } else {
//                System.out.println("Standalone Post Id: " + post.getPostId());
//                System.out.println();
//            }
            System.out.println("Amount of replies: " + post.getRepliesSize());
            curSesh.savePost(post);

        }
        System.out.println("Amount of thread posts: " + allPosts.size());
        System.out.println("Before persisting all");
        curSesh.persistAll();
    }

    private String getFilePath(String[] args) {
        return this.configManager.getFilePathFromArgs(args);
    }

    private List<Post> parseJsonFile(String filePath) {
        return this.jsonParser.parseJson(filePath);
    }


}

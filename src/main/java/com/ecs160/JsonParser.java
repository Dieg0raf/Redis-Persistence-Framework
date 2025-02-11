package com.ecs160;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonParser {

    public List<Post> parseJson(String filePath) {
        List<Post> posts = new ArrayList<>();
        try {
            JsonElement element = getJsonElement(filePath); // Locate and load the JSON file
            if (element.isJsonObject()) {
                JsonObject jsonObject = element.getAsJsonObject();
                JsonArray feedArray = jsonObject.get("feed").getAsJsonArray();
                for (JsonElement feedElement : feedArray) { // Process each thread in the feed
                    if (feedElement.isJsonObject()) {
                        JsonObject feedObject = feedElement.getAsJsonObject();
                        if (feedObject.has("thread")) {
                            JsonObject threadObject = feedObject.getAsJsonObject("thread");
                            parseThread(threadObject, posts); // Parse thread with null parent (top-level posts)
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
        return posts;
    }

    private static JsonElement getJsonElement(String filePath) throws IOException, URISyntaxException {
        Gson parser = new Gson();
        JsonElement jsonElement = getJsonFromAbsolutePath(filePath, parser);
        return (jsonElement != null) ? jsonElement : getJsonFromResources(filePath, parser);
    }

    private static JsonElement getJsonFromAbsolutePath(String filePath, Gson parser) throws IOException {
        File file = new File(filePath);
        if (file.exists() && file.isAbsolute()) {
            try (FileReader reader = new FileReader(file)) {
                return parser.fromJson(reader, JsonElement.class);
            }
        }
        return null;
    }

    private static JsonElement getJsonFromResources(String filePath, Gson parser) {
        try (InputStream inputStream = MyApp.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException("File not found at absolute path or in resources: " + filePath);
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                return parser.fromJson(reader, JsonElement.class);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void parseThread(JsonObject threadObject, List<Post> posts) {
        Map<String, String> postData = extractPostData(threadObject);

        // Check if post is either Thread or Standalone Post (no replies)
        if(threadObject.has("replies") && !threadObject.getAsJsonArray("replies").isEmpty()) {
            processThreadPost(postData, posts, threadObject.getAsJsonArray("replies"));
            return;
        }

        processStandalonePost(postData, posts); // Create standalone post if no replies
    }

    private static Map<String, String> extractPostData(JsonObject postObject) {
        Map<String, String> postData = new HashMap<>();

        JsonObject postJsonObj = postObject.getAsJsonObject("post");
        JsonObject recordObj = postJsonObj.getAsJsonObject("record");

        postData.put("postId", postJsonObj.get("cid").getAsString());
        postData.put("uri", postJsonObj.get("uri").getAsString());
        postData.put("createdAt", recordObj.get("createdAt").getAsString());
        postData.put("text", recordObj.has("text") ? recordObj.get("text").getAsString() : "");

        return postData;
    }

    private static void processThreadPost(Map<String, String> postData, List<Post> posts, JsonArray replies) {
        Post threadPost = new Post(
                postData.get("postId"),
                "NULL",
                postData.get("createdAt"),
                postData.get("uri"),
                postData.get("text")
                );

        // Recursively parse replies
        parseReplies(replies, threadPost, posts, postData.get("postId"));
        posts.add(threadPost);
    }

    private static void processStandalonePost(Map<String, String> postData, List<Post> posts) {
        Post standalonePost = new Post(
                postData.get("postId"),
                "NULL",
                postData.get("createdAt"),
                postData.get("uri"),
                postData.get("text")
                );

        posts.add(standalonePost); // add Post into list of posts (not replies)
    }

    private static void processReplyPost(Map<String, String> postData, String parentId, List<Post> posts, Post parentThreadPost) {
        Post replyPost = new Post(
                    postData.get("postId"),
                    parentId,
                    postData.get("createdAt"),
                    postData.get("uri"),
                    postData.get("text")
            );

        parentThreadPost.addReply(replyPost); // add reply to Parent Post list of replies
    }

    private static void parseReplies(JsonArray replies, Post threadPost, List<Post> posts, String parentId) {
        for (JsonElement reply : replies) {
            if (reply.isJsonObject()) {
                JsonObject replyObj = reply.getAsJsonObject();
                if (replyObj.has("$type") && replyObj.get("$type").getAsString().equals("app.bsky.feed.defs#threadViewPost")) {

                    // creates ReplyPost obj and stores in ThreadPost list of replies of type Post
                    Map<String, String> postData = extractPostData(replyObj);
                    processReplyPost(postData, parentId, posts, threadPost);

                }
            }
        }
    }
}

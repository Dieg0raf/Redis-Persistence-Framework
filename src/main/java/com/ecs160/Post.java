package com.ecs160;


import java.util.ArrayList;
import java.util.List;

public class Post {
    private final String postId;
    private final String parentPostId;
    private final String createdAt;
    private final String uri;
    private final String postContent;
    private final List<Post> replies  = new ArrayList<>();

    public Post(String postId, String parentPostId, String createdAt, String uri, String postContent) {
        this.postId = postId;
        this.parentPostId = parentPostId;
        this.createdAt = createdAt;
        this.uri = uri;
        this.postContent = postContent;
    }

    public void addReply(Post post) {
        this.replies.add(post);
    }

    public List<Post> getReplies() {
        return this.replies;
    }

    public int getRepliesSize() {
        return this.replies.size();
    }

    public String getPostId() {
        return this.postId;
    }

    public String getParentPostId() {
        return this.parentPostId;
    }

    public String getCreatedTimeStamp() {
        return this.createdAt;
    }

    public String getUri() {
        return this.uri;
    }

    public String getPostContent() {
        return this.postContent;
    }

    public boolean hasContent() {
        return !this.postContent.isEmpty();
    }
}

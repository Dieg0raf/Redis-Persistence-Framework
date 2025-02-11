package com.ecs160;

import com.ecs160.persistence.Persistable;
import com.ecs160.persistence.PersistableField;
import com.ecs160.persistence.PersistableId;
import com.ecs160.persistence.PersistableListField;

import java.util.ArrayList;
import java.util.List;

@Persistable
public class Post {
    @PersistableId
    private String postId; // TODO: Convert to Integer

    @PersistableField
    private String parentPostId;

    @PersistableField
    private String createdAt;

    @PersistableField
    private String uri;

    @PersistableField
    private String postContent;

    @PersistableListField(className = "Post")
    private final List<Post> replies  = new ArrayList<>();

    public Post(String postId, String parentPostId, String createdAt, String uri, String postContent) {
        this.postId = postId;
        this.parentPostId = parentPostId;
        this.createdAt = createdAt;
        this.uri = uri;
        this.postContent = postContent;
    }

    public Post() {}

    // Setter methods
    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setParentPostId(String parentPostId) {
        this.parentPostId = parentPostId;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    // Getter methods
    public List<Post> getReplies() {
        return this.replies;
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

    public int getRepliesSize() {
        return this.replies.size();
    }

    // Useful methods
    public void addReply(Post post) {
        this.replies.add(post);
    }

    public boolean hasContent() {
        return !this.postContent.isEmpty();
    }
}

package com.ecs160;

import com.ecs160.persistence.Persistable;
import com.ecs160.persistence.PersistableField;
import com.ecs160.persistence.PersistableId;
import com.ecs160.persistence.PersistableListField;
import com.ecs160.persistence.LazyLoad;

import java.util.ArrayList;
import java.util.List;

@Persistable
public class Post {
    @PersistableId
    private Integer postId;

    @PersistableField
    private Integer parentPostId;

    @PersistableField
    private String createdAt;

    @PersistableField
    private String uri;

    @PersistableField
    private String postContent;

    @PersistableListField(className = "Post")
    @LazyLoad
    private final List<Post> replies = new ArrayList<>();

    public Post(Integer postId, Integer parentPostId, String createdAt, String uri, String postContent) {
        this.postId = postId;
        this.parentPostId = parentPostId;
        this.createdAt = createdAt;
        this.uri = uri;
        this.postContent = postContent;
    }

    public Post() {
    }

    // Setter methods
    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public void setParentPostId(Integer parentPostId) {
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

    public Integer getPostId() {
        return this.postId;
    }

    public Integer getParentPostId() {
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

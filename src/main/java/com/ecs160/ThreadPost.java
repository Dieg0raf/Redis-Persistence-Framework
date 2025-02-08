package com.ecs160;
import java.util.ArrayList;
import java.util.List;

// Thread Post (consists of replies)
public class ThreadPost extends Post {
    private List<Post> replies = new ArrayList<>();

    public ThreadPost(String postId, String parentPostId, String createAt, String uri, String postContent) {
        super(postId, parentPostId, createAt, uri, postContent);
    }

    public void addReply(Post post) {
        replies.add(post);
    }

    public List<Post> getReplies() {
        return this.replies;
    }
}
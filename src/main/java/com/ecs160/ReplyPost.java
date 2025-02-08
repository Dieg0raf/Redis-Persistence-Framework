package com.ecs160;

public class ReplyPost extends Post {
    public ReplyPost(String postId, String parentPostId, String createAt, String uri, String postContent) {
        super(postId, parentPostId, createAt, uri, postContent);
    }
}

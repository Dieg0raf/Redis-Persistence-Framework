package com.ecs160;

public class PostPrinter {
    public void printQueryContent(Post post) {
        System.out.println("\n===========================");

        // Print Main Post (if no parent id)
        if (post.getParentPostId() == -1) {
            printMainPost(post);
            return;
        }

        // Print Reply Post (if parent id)
        printReplyPost(post);

        System.out.println("===========================");
    }

    private void printReplyPost(Post post) {
        System.out.println("NOTE: This is a reply post to parent post ID: " + post.getParentPostId());
        System.out.println("Reply Content: [" + post.getPostContent() + "]");
    }

    private void printMainPost(Post post) {
        System.out.println("MAIN POST:");
        System.out.println("└─ " + post.getPostContent());
        if (!post.getReplies().isEmpty()) { // print Replies
            printMultipleReplyPosts(post);
            return;
        }
        System.out.println("\n(No replies yet.)");
    }

    private void printMultipleReplyPosts(Post post) {
        System.out.println("\nREPLIES:");
        for (int i = 0; i < post.getReplies().size(); i++) {
            Post replyPost = post.getReplies().get(i);
            String replyContent = replyPost.getPostContent() != null ? replyPost.getPostContent() : "[Empty reply]";
            String prefix = (i == post.getReplies().size() - 1) ? "   └── " : "   ├── ";
            System.out.println(prefix + replyContent);
        }
    }
}

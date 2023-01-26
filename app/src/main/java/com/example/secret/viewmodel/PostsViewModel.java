package com.example.secret.viewmodel;

import com.example.secret.interfaces.Listener;
import com.example.secret.model.Post;
import com.example.secret.model.PostsModel;

import java.util.List;
import java.util.Map;

public class PostsViewModel {
    private Post currentPost;

    public void getPost(String postId, Listener<Post> successListener, Listener<Void> failedListener) {
        if (currentPost.getId().equals(postId)){
            successListener.onComplete(currentPost);
            return;
        }

        Listener<Post> vmSuccessListener = post -> {
            currentPost = post;
            successListener.onComplete(post);
        };
        PostsModel.instance().getPost(postId, vmSuccessListener, failedListener);
    }

}

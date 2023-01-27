package com.example.secret.viewmodel;

import com.example.secret.interfaces.Listener;
import com.example.secret.model.Post;
import com.example.secret.model.PostsModel;

public class PostsViewModel {
    private Post currentPost;
    private static final PostsViewModel _instance = new PostsViewModel();

    private PostsViewModel(){}

    public static PostsViewModel instance(){
        return _instance;
    }

    public void getPost(String postId, Listener<Post> successListener, Listener<Void> failedListener) {
        if (currentPost != null && currentPost.getId().equals(postId)){
            successListener.onComplete(currentPost);
            return;
        }

        Listener<Post> vmSuccessListener = post -> {
            currentPost = post;
            successListener.onComplete(post);
        };
        PostsModel.instance().getPost(postId, vmSuccessListener, failedListener);
    }

    public Post getCurrentPost(){
        return currentPost;
    }

}

package com.example.secret.model;

import android.graphics.Bitmap;

import com.example.secret.interfaces.Listener;

import java.util.List;
import java.util.Map;

public class PostsModel {
    private static final PostsModel _instance = new PostsModel();
    private FirebaseModel firebaseModel = new FirebaseModel();

    private PostsModel() {

    }

    public static PostsModel instance() {
        return _instance;
    }

    public void uploadBackground(String name, Bitmap bitmap, Listener<String> listener) {
        firebaseModel.uploadImage(name, bitmap, listener);
    }

    public void uploadPost(Post post, Listener<Void> successListener, Listener<Void> failListener){
        firebaseModel.setPost(post, successListener, failListener);
    }

    public void getPost(String postId, Listener<Post> successListener, Listener<Void> failListener) {
        Listener<Map<String, Object>> modelSuccessListener = result -> {
            Post p = Post.fromJson(result);
            // TODO: set in localdb
            successListener.onComplete(p);
        };
        firebaseModel.getPost(postId, modelSuccessListener, failListener);
    }
}

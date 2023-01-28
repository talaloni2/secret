package com.example.secret.utls;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.example.secret.interfaces.Listener;
import com.example.secret.model.Post;
import com.example.secret.model.PostsModel;
import com.google.common.base.Strings;

import java.util.UUID;

import javax.annotation.Nullable;

public class PostPublisher {
    Listener<Void> onSuccess;
    Listener<String> onFail;

    public PostPublisher(Listener<Void> onSuccess, Listener<String> onFail) {
        this.onSuccess = onSuccess;
        this.onFail = onFail;
    }

    public void publish(Post p, @Nullable Bitmap postImage) {
        if (Strings.isNullOrEmpty(p.getContent()) || p.getContent().length() < 5){
            onFail.onComplete("Post content too short");
            return;
        }

        if (postImage != null) {
            PostsModel.instance().uploadBackground(
                    UUID.randomUUID().toString(),
                    postImage,
                    url -> {
                        if (url == null) {
                            onFail.onComplete("Could not upload url");
                            return;
                        }
                        createPostWithNullableAvatar(p, url);
                    }
            );
        }
        else {
            createPostWithNullableAvatar(p, null);
        }
    }

    private void createPostWithNullableAvatar(Post p, @Nullable String url) {
        if (!Strings.isNullOrEmpty(url)){
            p.setBackgroundUrl(url);
        }

        PostsModel.instance().uploadPost(
                p,
                onSuccess,
                unused -> onFail.onComplete("Cannot upload post. Try again later"));
    }
}

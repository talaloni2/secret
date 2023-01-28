package com.example.secret;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.secret.model.Comment;
import com.example.secret.model.CommentsModel;
import com.example.secret.model.Post;
import com.example.secret.model.PostsModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PostsListFragmentViewModel extends ViewModel {
    private LiveData<List<Post>> posts = PostsModel.instance().getAllPosts();

    LiveData<List<Post>> getPosts() {
        return posts;
    }

    Map<String, LiveData<List<Comment>>> getPostsLatestComments() {
        Map<String, LiveData<List<Comment>>> postsLatestComments = new HashMap<>();
        List<Post> allPosts = this.posts.getValue();
        if (allPosts == null) {
            return postsLatestComments;
        }
        for (Post post : allPosts) {
            postsLatestComments.put(post.id, CommentsModel.instance().getCommentsByPostIdLimited(post.id));
        }
        return postsLatestComments;
    }

    LiveData<List<Comment>> getPostLatestComments(String postId) {
        return CommentsModel.instance().getCommentsByPostIdLimited(postId);
    }
}

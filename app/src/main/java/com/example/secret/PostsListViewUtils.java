package com.example.secret;

import android.util.Log;
import android.view.View;

import androidx.navigation.Navigation;

import com.example.secret.model.CommentsModel;
import com.example.secret.model.Post;
import com.example.secret.model.PostsModel;

public class PostsListViewUtils {
    public static void reloadData() {
        CommentsModel.instance().refreshLatestComments();
        PostsModel.instance().refreshLatestPosts();
    }

    public static void onPostClicked(PostRecyclerAdapter adapter, View view, int pos) {
        Log.d("TAG", "Row was clicked " + pos);
        Post post = adapter.getPosts().get(pos);
        PostsListFragmentDirections.ActionPostsListFragmentToSinglePostFragment action =
                PostsListFragmentDirections.actionPostsListFragmentToSinglePostFragment(
                        post.id
                );
        Navigation.findNavController(view).navigate(action);
    }
}

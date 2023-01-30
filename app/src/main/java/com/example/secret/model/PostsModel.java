package com.example.secret.model;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.secret.interfaces.Listener;
import com.example.secret.viewmodel.UsersViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PostsModel {
    private static final PostsModel _instance = new PostsModel();

    private int postsLimit = 5;
    private final int postsLimitIncrement = 5;

    private Executor executor = Executors.newSingleThreadExecutor();
    private FirebaseModel firebaseModel = new FirebaseModel();
    private AppLocalDbRepository localDb = AppLocalDb.getAppDb();

    final public MutableLiveData<PostsModel.LoadingState> EventPostsListLoadingState = new MutableLiveData<PostsModel.LoadingState>(PostsModel.LoadingState.NOT_LOADING);
    private LiveData<List<Post>> postList;
    private Map<String, LiveData<List<Post>>> usersPostsList = new HashMap<>();
    private User currentUser = UsersViewModel.instance().getCurrentUser();

    private PostsModel() {

    }

    public enum LoadingState {
        LOADING,
        NOT_LOADING
    }

    public static PostsModel instance() {
        return _instance;
    }

    public void uploadBackground(String name, Bitmap bitmap, Listener<String> listener) {
        firebaseModel.uploadImage(name, bitmap, listener);
    }

    public void uploadPost(Post post, Listener<Void> successListener, Listener<Void> failListener) {
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

    public LiveData<List<Post>> getAllPosts() {
        if (postList == null) {
            postList = localDb.postDao().getAllLimited(
                    "-" + currentUser.maxDaysBackPosts + " days", this.postsLimit
            );
            refreshLatestPosts();
        }
        return postList;
    }

    public LiveData<List<Post>> getUserPosts(String userId) {
        if (!usersPostsList.containsKey(userId)) {
            usersPostsList.put(userId, localDb.postDao().getUserPostsLimited(
                    userId, "-" + currentUser.maxDaysBackPosts + " days", this.postsLimit
            ));
            refreshLatestPosts();
        }
        return usersPostsList.get(userId);
    }

    public void deletePost(String postId) {
        localDb.commentDao().deletePostComments(postId);
        localDb.postDao().deletePost(postId);
    }

    public LiveData<List<Post>> loadMorePosts() {
        this.postsLimit += this.postsLimitIncrement;
        postList = localDb.postDao().getAllLimited(
                "-" + currentUser.maxDaysBackPosts + " days", this.postsLimit
        );
        return postList;
    }

    public LiveData<List<Post>> loadMoreUserPosts(String userId) {
        this.postsLimit += this.postsLimitIncrement;
        usersPostsList.put(userId, localDb.postDao().getUserPostsLimited(
                userId, "-" + currentUser.maxDaysBackPosts + " days", this.postsLimit
        ));
        return usersPostsList.get(userId);
    }

    public void refreshLatestPosts() {
        EventPostsListLoadingState.setValue(PostsModel.LoadingState.LOADING);
        // get local last update
        Long localLastUpdate = Post.getLocalLastUpdate();
        // get all updated recorde from firebase since local last update
        firebaseModel.getAllPostsSince(localLastUpdate, list -> executor.execute(() -> {
            Log.d("TAG", " firebase return : " + list.size());
            Long time = localLastUpdate;
            for (Post st : list) {
                // insert new records into ROOM
                localDb.postDao().insertAll(st);
                if (time < st.getLastUpdated()) {
                    time = st.getLastUpdated();
                }
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // update local last update
            Post.setLocalLastUpdate(time);
            EventPostsListLoadingState.postValue(LoadingState.NOT_LOADING);
        }));
    }

    // TODO: this is a placeholder until user's posts list is created
    public void getRandomPost(String userId, Listener<Post> successListener, Listener<Void> failListener) {
        Listener<Map<String, Object>> modelSuccessListener = result -> {
            Post p = Post.fromJson(result);
            // TODO: set in localdb
            successListener.onComplete(p);
        };
        firebaseModel.getRandomPost(userId, modelSuccessListener, failListener);
    }
}

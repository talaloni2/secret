package com.example.secret.model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.secret.interfaces.Listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CommentsModel {
    private static final CommentsModel _instance = new CommentsModel();

    private Executor executor = Executors.newSingleThreadExecutor();
    private FirebaseModel firebaseModel = new FirebaseModel();
    private AppLocalDbRepository localDb = AppLocalDb.getAppDb();

    final public MutableLiveData<CommentsModel.LoadingState> eventCommentsListLoadingState = new MutableLiveData<>(CommentsModel.LoadingState.NOT_LOADING);
    private Map<String, LiveData<List<Comment>>> postsComments = new HashMap<>();

    private CommentsModel() {

    }

    public enum LoadingState {
        LOADING,
        NOT_LOADING
    }

    public static CommentsModel instance() {
        return _instance;
    }

    public void uploadComment(Comment comment, Listener<Void> successListener, Listener<Void> failListener) {
        firebaseModel.setComment(comment, successListener, failListener);
    }

    public LiveData<List<Comment>> getCommentsByPostId(String postId) {
        if (!this.postsComments.containsKey(postId)) {
            this.postsComments.put(postId, localDb.commentDao().getCommentsByPostId(postId));
            refreshLatestComments();
        }
        return this.postsComments.get(postId);
    }

    public void refreshLatestComments() {
        eventCommentsListLoadingState.setValue(CommentsModel.LoadingState.LOADING);
        // get local last update
        Long localLastUpdate = Comment.getLocalLastUpdate();
        // get all updated recorde from firebase since local last update
        firebaseModel.getAllCommentsSince(localLastUpdate, list -> {
            executor.execute(() -> {
                Log.d("refresh all comments", " firebase return : " + list.size());
                Long time = list.stream().map(Comment::getLastUpdated).max(Long::compareTo).orElse(0L);
                localDb.commentDao().insertAll(list.toArray(new Comment[0]));
                // update local last update
                Comment.setLocalLastUpdate(time);
                eventCommentsListLoadingState.postValue(CommentsModel.LoadingState.NOT_LOADING);
            });
        });
    }
}

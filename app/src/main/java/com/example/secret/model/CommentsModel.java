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
    private LiveData<List<Comment>> commentsList;
    private Map<String, LiveData<List<Comment>>> postsLatestComments = new HashMap<>();
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

    public void getComment(String commentId, Listener<Comment> successListener, Listener<Void> failListener) {
        Listener<Map<String, Object>> modelSuccessListener = result -> {
            Comment comment = Comment.fromJson(result);
            successListener.onComplete(comment);
        };
        firebaseModel.getComment(commentId, modelSuccessListener, failListener);
    }

    public LiveData<List<Comment>> getAllComments() {
        if (commentsList == null) {
            commentsList = localDb.commentDao().getAll();
            refreshLatestComments();
        }
        return commentsList;
    }

    public LiveData<List<Comment>> getCommentsByPostId(String postId) {
        if (!this.postsComments.containsKey(postId)) {
            this.postsComments.put(postId, localDb.commentDao().getCommentsByPostId(postId));
            refreshLatestComments();
        }
        return this.postsComments.get(postId);
    }

    public LiveData<List<Comment>> getCommentsByPostIdLimited(String postId) {
        if (!this.postsLatestComments.containsKey(postId)) {
            this.postsLatestComments.put(postId, localDb.commentDao().getCommentsByPostIdLimited(postId, 2));
            refreshLatestComments();
        }
        return this.postsLatestComments.get(postId);
    }

    public void refreshLatestComments() {
        eventCommentsListLoadingState.setValue(CommentsModel.LoadingState.LOADING);
        // get local last update
        Long localLastUpdate = Comment.getLocalLastUpdate();
        // get all updated recorde from firebase since local last update
        firebaseModel.getAllCommentsSince(localLastUpdate, list -> {
            executor.execute(() -> {
                Log.d("refresh all comments", " firebase return : " + list.size());
                Long time = localLastUpdate;
                for (Comment comment : list) {
                    // insert new records into ROOM
                    localDb.commentDao().insertAll(comment);
                    if (time < comment.getLastUpdated()) {
                        time = comment.getLastUpdated();
                    }
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // update local last update
                Comment.setLocalLastUpdate(time);
                eventCommentsListLoadingState.postValue(CommentsModel.LoadingState.NOT_LOADING);
            });
        });
    }
}

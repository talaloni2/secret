package com.example.secret.model;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.example.secret.MyApplication;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Entity(foreignKeys = {@ForeignKey(entity = Post.class,
        parentColumns = Post.ID,
        childColumns = Comment.POST_ID,
        onDelete = ForeignKey.CASCADE)
})
public class Comment {
    @PrimaryKey
    @NonNull
    public String id = "";
    public String content = "";
    public String userId = "";
    public String postId = "";
    public Long lastUpdated;

    public Comment() {
    }

    public Comment(@NonNull String id, String content, String userId, String postId) {
        this.id = id;
        this.content = content;
        this.userId = userId;
        this.postId = postId;
    }

    static final String ID = "id";
    static final String CONTENT = "content";
    static final String USER_ID = "userId";
    static final String POST_ID = "postId";
    static final String LAST_UPDATED = "lastUpdated";
    static final String COLLECTION = "Comments";
    static final String LOCAL_LAST_UPDATED = "Comments_local_last_update";

    public static Comment fromJson(Map<String, Object> json) {
        String id = (String) json.get(ID);
        String content = (String) json.get(CONTENT);
        String userId = (String) json.get(USER_ID);
        String postId = (String) json.get(POST_ID);
        Comment comment = new Comment(id, content, userId, postId);
        try {
            Timestamp time = (Timestamp) json.get(LAST_UPDATED);
            comment.setLastUpdated(time.getSeconds());
        } catch (Exception e) {

        }
        return comment;
    }

    public static Long getLocalLastUpdate() {
        SharedPreferences sharedPref = MyApplication.getMyContext().getSharedPreferences("TAG", Context.MODE_PRIVATE);
        return sharedPref.getLong(LOCAL_LAST_UPDATED, 0);
    }

    public static void setLocalLastUpdate(Long time) {
        SharedPreferences sharedPref = MyApplication.getMyContext().getSharedPreferences("TAG", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(LOCAL_LAST_UPDATED, time);
        editor.commit();
    }

    public Map<String, Object> toJson() {
        Map<String, Object> json = new HashMap<>();
        json.put(ID, getId());
        json.put(CONTENT, getContent());
        json.put(USER_ID, getUserId());
        json.put(POST_ID, getPostId());
        json.put(LAST_UPDATED, FieldValue.serverTimestamp());
        return json;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}

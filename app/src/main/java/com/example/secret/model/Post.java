package com.example.secret.model;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.secret.MyApplication;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Entity
public class Post {
    @PrimaryKey
    @NonNull
    public String id = "";
    public String content = "";
    public Boolean isAnonymous = true;
    public String backgroundUrl = "";
    public Long lastUpdated;

    public Post() {
    }

    public Post(@NonNull String id, String content, boolean isAnonymous, String backgroundUrl) {
        this.id = id;
        this.content = content;
        this.isAnonymous = isAnonymous;
        this.backgroundUrl = backgroundUrl;
    }

    static final String CONTENT = "content";
    static final String ID = "id";
    static final String BACKGROUND = "background";
    static final String ANONYMOUS = "anonymous";
    static final String COLLECTION = "Posts";
    static final String LAST_UPDATED = "lastUpdated";
    static final String LOCAL_LAST_UPDATED = "Posts_local_last_update";

    public static Post fromJson(Map<String, Object> json) {
        String id = (String) json.get(ID);
        String content = (String) json.get(CONTENT);
        String background = (String) json.get(BACKGROUND);
        boolean anonymous = Optional.ofNullable((Boolean) json.get(ANONYMOUS)).orElse(true);
        Post st = new Post(id, content, anonymous, background);
        try {
            Timestamp time = (Timestamp) json.get(LAST_UPDATED);
            st.setLastUpdated(time.getSeconds());
        } catch (Exception e) {

        }
        return st;
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
        json.put(ANONYMOUS, getAnonymous());
        json.put(BACKGROUND, getBackgroundUrl());
        json.put(LAST_UPDATED, FieldValue.serverTimestamp());
        return json;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(Boolean anonymous) {
        isAnonymous = anonymous;
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}

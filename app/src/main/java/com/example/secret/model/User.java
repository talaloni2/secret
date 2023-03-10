package com.example.secret.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
public class User {
    @PrimaryKey
    @NonNull
    public String id = "";
    public String nickname = "";
    public String email = "";
    public String avatarUrl = "";
    public Long lastUpdated;

    public String bio = "";

    public int maxDaysBackPosts = 10;

    public User() {
    }

    public User(@NonNull String id, String nickname, String avatarUrl, String email, String bio, int maxDaysBackPosts) {
        this.nickname = nickname;
        this.id = id;
        this.avatarUrl = avatarUrl;
        this.email = email;
        this.bio = bio;
        this.maxDaysBackPosts = maxDaysBackPosts;
    }

    static final String NICKNAME = "nickname";
    static final String ID = "id";
    static final String AVATAR = "avatar";
    static final String EMAIL = "email";
    static final String COLLECTION = "Users";
    static final String LAST_UPDATED = "lastUpdated";
    static final String LOCAL_LAST_UPDATED = "Users_local_last_update";

    static final String BIO = "bio";

    static final String MAX_DAYS_BACK_POSTS = "maxDaysBackPosts";

    public static User fromJson(Map<String, Object> json) {
        String id = (String) json.get(ID);
        String nickname = (String) json.get(NICKNAME);
        String avatar = (String) json.get(AVATAR);
        String email = (String) json.get(EMAIL);
        String bio = (String) json.get(BIO);
        int maxDaysBackPosts = Optional.ofNullable((Long) json.get(MAX_DAYS_BACK_POSTS)).orElse(10l).intValue();
        User st = new User(id, nickname, avatar, email, bio, maxDaysBackPosts);
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
        json.put(NICKNAME, getNickname());
        json.put(AVATAR, getAvatarUrl());
        json.put(EMAIL, getEmail());
        json.put(LAST_UPDATED, FieldValue.serverTimestamp());
        json.put(BIO, getBio());
        json.put(MAX_DAYS_BACK_POSTS, getMaxDaysBackPosts());
        return json;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setMaxDaysBackPosts(int maxDaysBackPosts) {
        this.maxDaysBackPosts = maxDaysBackPosts;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getBio() {
        return bio;
    }

    public int getMaxDaysBackPosts() {
        return maxDaysBackPosts;
    }
}

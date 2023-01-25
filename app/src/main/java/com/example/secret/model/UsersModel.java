package com.example.secret.model;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import com.example.secret.interfaces.Listener;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UsersModel {
    private static final UsersModel _instance = new UsersModel();

    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = HandlerCompat.createAsync(Looper.getMainLooper());
    private FirebaseModel firebaseModel = new FirebaseModel();
    AppLocalDbRepository localDb = AppLocalDb.getAppDb();

    public static UsersModel instance() {
        return _instance;
    }

    private UsersModel() {
    }

    public void uploadImage(String name, Bitmap bitmap, Listener<String> listener) {
        firebaseModel.uploadImage(name, bitmap, listener);
    }

    public boolean isUserConnected() {
        return firebaseModel.isUserConnected();
    }

    public void registerUser(User user, String password,
                             Listener<Void> successListener, Listener<Void> failedListener) {
        firebaseModel.register(user, password, successListener, failedListener);
    }

    public void signIn(String email, String password, Listener<Void> successListener, Listener<Void> failedListener) {
        firebaseModel.signIn(email, password, successListener, failedListener);
    }

    public void getCurrentUser(Listener<User> onUserReceived, Listener<Void> onUserNotReceived){
        firebaseModel.getCurrentUser(onUserReceived, onUserNotReceived);
    }

    public void updateUser(User user, Listener<Void> successListener, Listener<Void> failedListener) {
        firebaseModel.setUser(user, successListener, failedListener);
    }
}


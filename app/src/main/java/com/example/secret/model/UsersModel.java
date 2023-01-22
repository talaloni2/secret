package com.example.secret.model;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.os.HandlerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.secret.interfaces.Listener;

import java.util.List;
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
        return firebaseModel.getCurrentUser() != null;
    }

    public void registerUser(User user, String password,
                             Listener<Void> successListener, Listener<Void> failedListener) {
        firebaseModel.register(user, password, successListener, failedListener);
    }

}


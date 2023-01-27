package com.example.secret.model;

import android.graphics.Bitmap;

import com.example.secret.interfaces.Listener;

public class UsersModel {
    private static final UsersModel _instance = new UsersModel();
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

    public void signOut() {
        firebaseModel.signOut();
    }

    public void checkForNicknameExistence(String nickname, String userId, Listener<Boolean> onCheckSuccess, Listener<Exception> onCheckFailed) {
        firebaseModel.checkForNicknameExistence(nickname, userId, onCheckSuccess, onCheckFailed);

    }
}


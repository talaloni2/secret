package com.example.secret.model;

import android.graphics.Bitmap;

import com.example.secret.MyApplication;
import com.example.secret.interfaces.Listener;
import com.google.common.base.Strings;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsersModel {
    private static final UsersModel _instance = new UsersModel();
    private FirebaseModel firebaseModel = new FirebaseModel();
    AppLocalDbRepository localDb = AppLocalDb.getAppDb();
    ExecutorService executor = Executors.newSingleThreadExecutor();

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
        insertUserToLocalDb(user);
    }

    public void signIn(String email, String password, Listener<Void> successListener, Listener<Void> failedListener) {
        firebaseModel.signIn(email, password, successListener, failedListener);
    }

    public void getCurrentUser(boolean forceRefresh, Listener<User> onUserReceived, Listener<Void> onUserNotReceived){
        if(firebaseModel.isUserConnected()){
            executor.execute(() -> {
                String currentUserId = firebaseModel.getCurrentUserId();
                if (Strings.isNullOrEmpty(currentUserId)) {
                    onUserNotReceived.onComplete(null);
                    return;
                }
                User currentUser = forceRefresh ? null : localDb.userDao().getUserById(currentUserId);
                if (currentUser == null){
                    MyApplication.mainThreadHandler.post(()->
                            firebaseModel.getCurrentUser(user -> {
                                insertUserToLocalDb(user);
                                onUserReceived.onComplete(user);
                            }, onUserNotReceived)
                    );
                    return;
                }
                MyApplication.mainThreadHandler.post(()-> onUserReceived.onComplete(currentUser));
            });
        }
    }

    public void updateUser(User user, Listener<Void> successListener, Listener<Void> failedListener) {
        firebaseModel.setUser(user, successListener, failedListener);
        insertUserToLocalDb(user);
    }

    private void insertUserToLocalDb(User user) {
        executor.execute(() -> localDb.userDao().insertAll(user));
    }

    public void signOut() {
        firebaseModel.signOut();
    }

    public void checkForNicknameExistence(String nickname, String userId, Listener<Boolean> onCheckSuccess, Listener<Exception> onCheckFailed) {
        firebaseModel.checkForNicknameExistence(nickname, userId, onCheckSuccess, onCheckFailed);

    }
}


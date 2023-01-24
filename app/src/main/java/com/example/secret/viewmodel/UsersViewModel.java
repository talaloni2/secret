package com.example.secret.viewmodel;

import com.example.secret.interfaces.Listener;
import com.example.secret.model.FirebaseModel;
import com.example.secret.model.User;
import com.example.secret.model.UsersModel;

import java.util.List;

public class UsersViewModel {

    private User currentUser;

    private static final UsersViewModel _instance = new UsersViewModel();

    public static UsersViewModel instance() {
        return _instance;
    }

    private UsersViewModel() {
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setUser(Listener<Void> onSetUserSuccess, Listener<Void> onSetUserFailed){
        UsersModel.instance().getCurrentUser(user -> {
            currentUser = user;
            onSetUserSuccess.onComplete(null);
        }, onSetUserFailed);
    }

}

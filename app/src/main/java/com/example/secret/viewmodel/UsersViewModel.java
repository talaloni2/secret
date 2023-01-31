package com.example.secret.viewmodel;

import com.example.secret.interfaces.Listener;
import com.example.secret.model.User;
import com.example.secret.model.UsersModel;

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
        executeSetUser(false, onSetUserSuccess, onSetUserFailed);
    }

    public void reloadUser(Listener<Void> onSetUserSuccess, Listener<Void> onSetUserFailed){
        executeSetUser(true, onSetUserSuccess, onSetUserFailed);
    }

    private void executeSetUser(boolean forceRefresh, Listener<Void> onSetUserSuccess, Listener<Void> onSetUserFailed){
        UsersModel.instance().getCurrentUser(forceRefresh, user -> {
            currentUser = user;
            onSetUserSuccess.onComplete(null);
        }, onSetUserFailed);
    }

    public void signOut() {
        this.currentUser = null;
        UsersModel.instance().signOut();
    }

}

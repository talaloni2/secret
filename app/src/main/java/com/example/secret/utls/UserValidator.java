package com.example.secret.utls;

import android.util.Log;

import com.example.secret.interfaces.Listener;
import com.example.secret.model.UsersModel;

public class UserValidator {

    public static void validateNickname(String nickname, String userId, Listener<Void> valid, Listener<String> invalid) {
        if (nickname.length() <= 5) {
            invalid.onComplete("Nickname is too short");
        }
        UsersModel.instance().checkForNicknameExistence(nickname, userId, isExists->{
            if (isExists){
                invalid.onComplete("Nickname is already taken");
                return;
            }
            valid.onComplete(null);
        }, err->{
            Log.e("NicknameValidation", "Error while checking for nickname uniqueness", err);
            invalid.onComplete("An error occurred, try again later");
        });
    }
}

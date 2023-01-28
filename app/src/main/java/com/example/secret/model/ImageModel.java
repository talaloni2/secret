package com.example.secret.model;

import android.graphics.Bitmap;

import com.example.secret.interfaces.Listener;

public class ImageModel {

    private FirebaseModel firebaseModel = new FirebaseModel();
    AppLocalDbRepository localDb = AppLocalDb.getAppDb();

    private static final ImageModel _instance = new ImageModel();

    private ImageModel(){}

    public static ImageModel instance() {
        return _instance;
    }

    public void uploadImage(String name, Bitmap bitmap, Listener<String> listener) {
        firebaseModel.uploadImage(name, bitmap, listener);
    }

    public void downloadImage(String url, Listener<Bitmap> success, Listener<String> fail){
        Image localImage = localDb.imageDao().getUserByUrl(url);
        if (localImage!=null){

        }

    }

}

package com.example.secret.model;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.example.secret.R;
import com.example.secret.interfaces.Listener;
import com.example.secret.utls.BitmapCompressor;
import com.google.common.util.concurrent.Futures;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ImageModel {

    ExecutorService executor = Executors.newSingleThreadExecutor();
    private FirebaseModel firebaseModel = new FirebaseModel();
    AppLocalDbRepository localDb = AppLocalDb.getAppDb();

    private static final ImageModel _instance = new ImageModel();

    private ImageModel() {
    }

    public static ImageModel instance() {
        return _instance;
    }

    public void uploadImage(String name, Bitmap bitmap, Listener<String> listener) {
        firebaseModel.uploadImage(name, BitmapCompressor.compress(bitmap), listener);
    }

    public void getImage(String url, Listener<Bitmap> success, Listener<String> fail) {
        getImage(url, R.drawable.sharing_secret_image, success, fail);
    }

    public void getImage(String url, int defaultImageId, Listener<Bitmap> success, Listener<String> fail) {
        CompletableFuture.supplyAsync(() -> {
            String TAG = "DownloadImage";
            Image localImage = localDb.imageDao().getUserByUrl(url);
            if (localImage != null) {
                try {
                    return BitmapCompressor.decompress(localImage.content);
                } catch (Exception e) {
                    Log.e(TAG, "[LocalStore] Decompression Failure");
                    return null;
                }
            }
            try {
                Bitmap bitmap = Picasso.get().load(url).placeholder(defaultImageId).get();
                localDb.imageDao().insert(new Image(url, BitmapCompressor.compress(bitmap)));
                return bitmap;
            } catch (IOException e) {
                Log.e(TAG, "[Remote] bitmap load failed", e);
                return null;
            }
        }).thenAcceptAsync(bitmap -> {
            if (bitmap == null){
                fail.onComplete("Image download failed");
                return;
            }
            success.onComplete(bitmap);
        });
    }

}

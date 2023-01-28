package com.example.secret.utls;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultCallback;

import java.util.function.Consumer;

public class CameraActivityResultCallback implements ActivityResultCallback<Bitmap> {

    Consumer<Boolean> setImageSelected;

    ImageView targetView;

    Resources resources;
    public CameraActivityResultCallback(Consumer<Boolean> setImageSelected, ImageView targetView, Resources resources) {
        this.setImageSelected = setImageSelected;
        this.targetView = targetView;
        this.resources = resources;
    }

    @Override
    public void onActivityResult(Bitmap result) {
        if (result != null) {
            BitmapDrawable background = new BitmapDrawable(resources, result);
            targetView.setImageDrawable(background);
            setImageSelected.accept(true);
        }
    }
}

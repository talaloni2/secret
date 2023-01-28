package com.example.secret.utls;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.function.Consumer;

public class GalleryActivityResultCallback implements ActivityResultCallback<Uri> {

    Activity activity;
    ImageView targetView;
    Consumer<Boolean> setImageSelected;

    public GalleryActivityResultCallback(Activity activity, ImageView targetView, Consumer<Boolean> setImageSelected) {
        this.activity = activity;
        this.targetView = targetView;
        this.setImageSelected = setImageSelected;
    }

    @Override
    public void onActivityResult(Uri result) {
        if (result != null) {
            try {
                InputStream is = activity.getContentResolver().openInputStream(result);
                targetView.setImageDrawable(Drawable.createFromStream(is, result.toString()));
                setImageSelected.accept(true);
            } catch (FileNotFoundException e) {
                Toast.makeText(activity, "Could not select image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

package com.example.secret.utls;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class BitmapConverter {
    public static Bitmap fromDrawable(Drawable drawable){
        return ((BitmapDrawable) drawable).getBitmap();
    }
}

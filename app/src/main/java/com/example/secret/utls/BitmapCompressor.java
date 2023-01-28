package com.example.secret.utls;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class BitmapCompressor {
    public static byte[] compress(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap decompress(byte[] raw) {
        return BitmapFactory.decodeByteArray(raw, 0, raw.length);
    }
}

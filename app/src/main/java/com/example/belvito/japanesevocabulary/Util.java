package com.example.belvito.japanesevocabulary;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class Util {
    static byte[] convertBitmapToByteArray(Bitmap bitmap) {
        if(bitmap == null) {
            return null;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }
}

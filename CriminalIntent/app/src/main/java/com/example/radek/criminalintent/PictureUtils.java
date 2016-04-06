package com.example.radek.criminalintent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

/**
 * Created by radek on 23/01/16.
 */
public class PictureUtils {
    private PictureUtils() {}

    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int srcHeight = options.outHeight;
        int srcWidth = options.outWidth;
        int inSampleSize = 1;
        if (srcHeight < destHeight || srcWidth < destWidth) {
            if (srcWidth < srcHeight) {
                inSampleSize = Math.round(srcHeight / destHeight);
            } else {
                inSampleSize = Math.round(srcWidth / destWidth);
            }
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return getScaledBitmap(path, size.x, size.y);
    }
}

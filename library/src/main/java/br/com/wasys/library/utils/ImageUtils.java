package br.com.wasys.library.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.IOException;

/**
 * Created by pascke on 26/06/17.
 */

public class ImageUtils {

    public static Bitmap resize(Uri uri, int width) throws IOException {
        String path = uri.getPath();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        int height = (outHeight * width) / outWidth;
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int width, int height) {
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        int inSampleSize = 1;
        if (outWidth > width || outHeight > height) {
            int halfWidth = outWidth / 2;
            int halfHeight = outHeight / 2;
            while ((halfWidth / inSampleSize) > width && (halfHeight / inSampleSize) > height) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}

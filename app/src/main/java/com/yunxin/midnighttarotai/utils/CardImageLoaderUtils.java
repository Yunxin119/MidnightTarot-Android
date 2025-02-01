package com.yunxin.midnighttarotai.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class CardImageLoaderUtils {
    private static final String TAG = "CardImageLoaderUtils";
    /**
     * Get card bitmap from assets folder
     * @param context Application context
     * @param cardName Card name (without extension)
     * @param isReversed Whether the card should be reversed
     * @return Bitmap of the card, null if not found
     */
    public static Bitmap getCardBitmapFromAssets(Context context, String cardName, boolean isReversed) {
        try {
            AssetManager assetManager = context.getAssets();
            String filePath = "cards/" + cardName + ".jpg";

            InputStream inputStream = assetManager.open(filePath);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (isReversed && bitmap != null) {
                Matrix matrix = new Matrix();
                matrix.postRotate(180);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(),
                        matrix, true);
            }

            return bitmap;
        } catch (IOException e) {
            Log.e(TAG, "Error loading card image: " + cardName, e);
            return null;
        }
    }
}

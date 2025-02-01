package com.yunxin.midnighttarotai.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for handling card-related operations including image loading and theme detection.
 * Provides helper methods for card initialization and asset management.
 */
public class CardPickUtils {
    private static final String TAG = "CardPickUtils";

    /**
     * Determines if the device is currently in dark mode
     *
     * @param context Application context
     * @return true if device is in dark mode, false otherwise
     */
    public static boolean isDarkMode(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int nightMode = context.getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            return nightMode == Configuration.UI_MODE_NIGHT_YES;
        }
        return false;
    }

    /**
     * Loads a bitmap from assets
     *
     * @param context Application context
     * @param filePath Path to the asset file
     * @return Bitmap loaded from assets, or null if loading fails
     */
    public static Bitmap loadBitmapFromAssets(Context context, String filePath) {
        try {
            InputStream inputStream = context.getAssets().open(filePath);
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "Error loading bitmap from assets: " + filePath, e);
            return null;
        }
    }

    /**
     * Gets the appropriate card back image based on current theme
     *
     * @param context Application context
     * @return Bitmap of the card back image
     */
    public static Bitmap getCardBackImage(Context context) {
        String backImageFile = isDarkMode(context) ?
                "back/Back.jpg" : "back/Back_light.jpg";
        return loadBitmapFromAssets(context, backImageFile);
    }

    /**
     * Lists all card files in the assets directory
     *
     * @param context Application context
     * @return Array of card file names, or empty array if none found
     */
    public static String[] listCardFiles(Context context) {
        try {
            return context.getAssets().list("cards");
        } catch (IOException e) {
            Log.e(TAG, "Error listing card files", e);
            return new String[0];
        }
    }

    /**
     * Extracts card name from file name
     *
     * @param fileName Name of the card file
     * @return Card name without file extension
     */
    public static String extractCardName(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
}
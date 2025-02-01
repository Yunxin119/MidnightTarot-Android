package com.yunxin.midnighttarotai.utils;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.lang.ref.WeakReference;

/**
 * Singleton utility class for managing Bitmap references throughout the application.
 * Uses WeakReferences to allow garbage collection of unused Bitmaps.
 * Thread-safe implementation for concurrent access.
 */
public class BitmapHolder {
    private static volatile BitmapHolder sInstance;
    private final List<WeakReference<Bitmap>> mCardImages;

    /**
     * Private constructor to prevent instantiation
     */
    private BitmapHolder() {
        mCardImages = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Gets the singleton instance of BitmapHolder
     *
     * @return The singleton instance
     */
    @NonNull
    public static BitmapHolder getInstance() {
        if (sInstance == null) {
            synchronized (BitmapHolder.class) {
                if (sInstance == null) {
                    sInstance = new BitmapHolder();
                }
            }
        }
        return sInstance;
    }

    /**
     * Sets new card images, clearing any existing ones
     * Creates WeakReferences to allow garbage collection when images are no longer needed
     *
     * @param images List of Bitmaps to store
     */
    public void setCardImages(@NonNull List<Bitmap> images) {
        synchronized (mCardImages) {
            clear();
            for (Bitmap image : images) {
                if (image != null && !image.isRecycled()) {
                    mCardImages.add(new WeakReference<>(image));
                }
            }
        }
    }

    /**
     * Retrieves all currently valid card images
     * Automatically removes references to recycled or collected Bitmaps
     *
     * @return List of valid Bitmap images
     */
    @NonNull
    public List<Bitmap> getCardImages() {
        synchronized (mCardImages) {
            List<Bitmap> validImages = new ArrayList<>();
            List<WeakReference<Bitmap>> toRemove = new ArrayList<>();

            for (WeakReference<Bitmap> ref : mCardImages) {
                Bitmap bitmap = ref.get();
                if (bitmap != null && !bitmap.isRecycled()) {
                    validImages.add(bitmap);
                } else {
                    toRemove.add(ref);
                }
            }

            // Clean up invalid references
            mCardImages.removeAll(toRemove);

            return validImages;
        }
    }

    /**
     * Retrieves a specific card image by index
     *
     * @param index Index of the desired image
     * @return The Bitmap at the specified index, or null if invalid
     */
    @Nullable
    public Bitmap getCardImage(int index) {
        synchronized (mCardImages) {
            if (index >= 0 && index < mCardImages.size()) {
                WeakReference<Bitmap> ref = mCardImages.get(index);
                Bitmap bitmap = ref.get();
                if (bitmap != null && !bitmap.isRecycled()) {
                    return bitmap;
                }
                // Remove invalid reference
                mCardImages.remove(index);
            }
            return null;
        }
    }

    /**
     * Clears all stored image references
     * Does not recycle the Bitmaps as they may still be in use elsewhere
     */
    public void clear() {
        synchronized (mCardImages) {
            mCardImages.clear();
        }
    }

    /**
     * Cleans up resources and clears the singleton instance
     * Should be called when the application is being terminated
     */
    public static void destroy() {
        if (sInstance != null) {
            sInstance.clear();
            sInstance = null;
        }
    }
}
package com.yunxin.midnighttarotai.payment;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DailyReadingManager {
    private static final String TAG = "DailyReadingManager";

    private static final String PREFS_NAME = "DailyReadingTracker";
    private static final String KEY_LAST_READING_DATE = "last_reading_date";
    private static final String KEY_CREDITS_EARNED = "credits_earned";
    private static final String KEY_LAST_SHARE_DATE = "last_share_date";

    private Context context;
    private SharedPreferences sharedPreferences;
    private PaymentManager paymentManager;
    private DatabaseReference userRef;
    private FirebaseUser currentUser;

    /**
     * Constructs a new DailyReadingManager instance.
     *
     * @param context The application context
     */
    public DailyReadingManager(@NonNull Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.paymentManager = new PaymentManager(context);
        this.userRef = FirebaseDatabase.getInstance().getReference("users");
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    /**
     * Checks if a user can perform a daily reading.
     *
     * @param uid The user's ID
     * @return true if the user can perform a daily reading, false otherwise
     */
    public boolean canPerformDailyReading(@NonNull String uid) {
        if (paymentManager.hasLifetimeAccess(uid) || paymentManager.hasActiveSubscription(uid)) {
            return true;
        }

        SharedPreferences userPrefs = getUserPreferences(uid);
        return !userPrefs.getBoolean(
                getDailyReadingKey(getCurrentDate()),
                false
        );
    }

    /**
     * Records a daily reading.
     *
     * @param userId The user's ID
     * @return true if the reading was successfully recorded
     */
    public boolean recordDailyReading(@NonNull String userId) {
        SharedPreferences userPrefs = getUserPreferences(userId);
        SharedPreferences.Editor editor = userPrefs.edit();

        editor.putBoolean(getDailyReadingKey(getCurrentDate()), true);

        // 保存更改
        editor.apply();

        return true;
    }

    /**
     * Gets the SharedPreferences instance for a specific user.
     */
    private SharedPreferences getUserPreferences(String userId) {
        return context.getSharedPreferences(
                PREFS_NAME + "_" + userId,
                Context.MODE_PRIVATE
        );
    }

    /**
     * Generates a key for tracking daily readings.
     */
    private String getDailyReadingKey(String date) {
        return "has_done_daily_reading_" + date;
    }


    /**
     * Check if user has already shared today
     */
    public boolean canEarnShareCredit(@NonNull String userId) {
        if (paymentManager.hasLifetimeAccess(userId) ||
                paymentManager.hasActiveSubscription(userId)) {
            return false;
        }

        SharedPreferences userPrefs = getUserPreferences(userId);
        String today = getCurrentDate();
        String lastShareDate = userPrefs.getString(KEY_LAST_SHARE_DATE, "");

        return !today.equals(lastShareDate);
    }

    /**
     * get date
     * @return date
     */
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

}
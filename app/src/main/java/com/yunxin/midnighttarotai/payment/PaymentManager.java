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

import com.yunxin.midnighttarotai.payment.PaymentType;

/**
 * Manages local payment processing and credit system for the Tarot Reading application.
 */
public class PaymentManager {
    private static final String PREFS_NAME = "TarotAppPayments";
    private static final String TAG = PaymentManager.class.getSimpleName();
    private static final long SUBSCRIPTION_DURATION_MS = 30L * 24 * 60 * 60 * 1000; // 30 days
    private static final int CREDITS_PER_READING = 3;

    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final DatabaseReference userRef;

    public PaymentManager(@NonNull Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.userRef = FirebaseDatabase.getInstance().getReference("users");
    }

    /**
     * Initializes a new user with welcome credits.
     */
    public void initializeNewUser(@NonNull String userId) {
        final int WELCOME_CREDITS = 4 * CREDITS_PER_READING;

        if (sharedPreferences.contains(getPreferenceKey("credits", userId))) {
            return;
        }

        // local stoarge
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getPreferenceKey("credits", userId), WELCOME_CREDITS);
        editor.apply();

        // update firebase
        Map<String, Object> initialData = new HashMap<>();
        initialData.put("credits", WELCOME_CREDITS);
        initialData.put("welcomeCreditsReceived", true);

        userRef.child(userId).updateChildren(initialData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully initialized new user with welcome credits"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to initialize user credits", e));
    }


    /**
     * Processes a local payment for the specified payment option.
     */
    public boolean processPayment(@NonNull PaymentOption option) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No user logged in");
            return false;
        }

        try {
            boolean paymentSuccess = simulatePayment(option);
            if (paymentSuccess) {
                String userId = currentUser.getUid();
                updatePaymentRecord(userId, option);
                updateFirebaseUserPaymentStatus(userId, option);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Payment processing error", e);
        }
        return false;
    }

    private boolean simulatePayment(@NonNull PaymentOption option) {
        Log.d(TAG, "Simulating payment for: " + option.getTitle());
        return true;
    }

    /**
     * Updates local storage with payment information and credits.
     */
    private void updatePaymentRecord(@NonNull String userId, @NonNull PaymentOption option) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        switch (option.getType()) {
            case SUBSCRIPTION:
                editor.putBoolean(getPreferenceKey("is_subscribed", userId), true);
                editor.putLong(getPreferenceKey("subscription_end", userId),
                        System.currentTimeMillis() + SUBSCRIPTION_DURATION_MS);
                break;
            case ONE_TIME:
                editor.putBoolean(getPreferenceKey("has_lifetime_access", userId), true);
                break;
            case PACKAGE_1:
            case PACKAGE_5:
            case PACKAGE_15:
            case PACKAGE_30:
                int additionalCredits = getCreditsForPackage(option.getType());
                int currentCredits = getCredits(userId);
                editor.putInt(getPreferenceKey("credits", userId),
                        currentCredits + additionalCredits);
                break;
        }
        editor.apply();
    }

    /**
     * Updates Firebase with the user's payment status and credits.
     */
    private void updateFirebaseUserPaymentStatus(@NonNull String userId,
                                                 @NonNull PaymentOption option) {
        Map<String, Object> updates = new HashMap<>();

        switch (option.getType()) {
            case SUBSCRIPTION:
                long currentTime = System.currentTimeMillis();
                updates.put("subscriptionType", "MONTHLY");
                updates.put("subscriptionStartDate", currentTime);
                updates.put("subscriptionEndDate", currentTime + SUBSCRIPTION_DURATION_MS);
                break;
            case ONE_TIME:
                updates.put("hasLifetimeAccess", true);
                break;
            case PACKAGE_1:
            case PACKAGE_5:
            case PACKAGE_15:
            case PACKAGE_30:
                int additionalCredits = getCreditsForPackage(option.getType());
                updates.put("credits", getCredits(userId) + additionalCredits);
                break;
        }

        userRef.child(userId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Successfully updated user payment status"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update user payment status", e));
    }

    /**
     * Returns the number of credits included in a package type.
     * Each reading costs 3 credits, so multiply the number of readings by 3.
     */
    private int getCreditsForPackage(PaymentType type) {
        switch (type) {
            case PACKAGE_1: return 1 * CREDITS_PER_READING;
            case PACKAGE_5: return 5 * CREDITS_PER_READING;
            case PACKAGE_15: return 15 * CREDITS_PER_READING;
            case PACKAGE_30: return 30 * CREDITS_PER_READING;
            default: return 0;
        }
    }

    /**
     * Gets the current credit balance for a user.
     */
    public int getCredits(@NonNull String userId) {
        return sharedPreferences.getInt(getPreferenceKey("credits", userId), 0);
    }

    /**
     * Consumes credits for a reading.
     * @return true if enough credits were available and consumed
     */
    public boolean consumeCreditsForReading(@NonNull String userId) {
        if (hasLifetimeAccess(userId) || hasActiveSubscription(userId)) {
            return true;
        }

        int currentCredits = getCredits(userId);
        if (currentCredits < CREDITS_PER_READING) {
            return false;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getPreferenceKey("credits", userId), currentCredits - CREDITS_PER_READING);
        editor.apply();

        // Update Firebase
        userRef.child(userId).child("credits")
                .setValue(currentCredits - CREDITS_PER_READING);

        return true;
    }

    /**
     * Adds credits to a user's balance.
     */
    public void addCredits(@NonNull String userId, int amount) {
        int currentCredits = getCredits(userId);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getPreferenceKey("credits", userId), currentCredits + amount);
        editor.apply();

        // Update Firebase
        userRef.child(userId).child("credits")
                .setValue(currentCredits + amount);
    }

    private String getPreferenceKey(String key, String userId) {
        return key + "_" + userId;
    }

    public boolean hasLifetimeAccess(String userId) {
        return sharedPreferences.getBoolean(getPreferenceKey("has_lifetime_access", userId), false);
    }

    public boolean hasActiveSubscription(String userId) {
        boolean hasSubscription = sharedPreferences.getBoolean(
                getPreferenceKey("is_subscribed", userId), false);
        long subscriptionEnd = sharedPreferences.getLong(
                getPreferenceKey("subscription_end", userId), 0);
        return hasSubscription && subscriptionEnd > System.currentTimeMillis();
    }

    public String getExpirationDate(String userId) {
        boolean hasSubscription = sharedPreferences.getBoolean(
                getPreferenceKey("is_subscribed", userId), false);
        if (hasSubscription) {
            long subscriptionEnd = sharedPreferences.getLong(
                    getPreferenceKey("subscription_end", userId), 0);

            if (subscriptionEnd > System.currentTimeMillis()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return dateFormat.format(new Date(subscriptionEnd));
            }
        }

        return null;
    }

    public boolean hasValidAccess(String userId) {
        return hasLifetimeAccess(userId) ||
                hasActiveSubscription(userId) ||
                getCredits(userId) >= CREDITS_PER_READING;
    }
}
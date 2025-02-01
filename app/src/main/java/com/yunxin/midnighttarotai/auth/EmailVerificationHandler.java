package com.yunxin.midnighttarotai.auth;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Handles email verification functionality for user registration
 * This class manages sending verification emails and checking verification status
 * through Firebase Authentication
 */
public class EmailVerificationHandler {
    private static final String TAG = "EmailVerificationHandler";
    private final FirebaseAuth auth;
    private EmailVerificationListener listener;

    /**
     * Interface for email verification status callbacks
     */
    public interface EmailVerificationListener {
        /**
         * Called when verification email is successfully sent
         */
        void onVerificationEmailSent();

        /**
         * Called when email is successfully verified
         */
        void onVerificationSuccess();

        /**
         * Called when any verification process fails
         * @param e Exception containing error details
         */
        void onVerificationFailed(Exception e);
    }

    /**
     * Constructor initializes Firebase Authentication instance
     */
    public EmailVerificationHandler() {
        this.auth = FirebaseAuth.getInstance();
    }

    /**
     * Sets the listener for verification status callbacks
     * @param listener Implementation of EmailVerificationListener
     */
    public void setVerificationListener(EmailVerificationListener listener) {
        this.listener = listener;
    }

    /**
     * Sends verification email to the currently signed-in user
     * This method requires a user to be currently signed in to Firebase
     */
    public void sendVerificationEmail() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Verification email sent successfully");
                            if (listener != null) {
                                listener.onVerificationEmailSent();
                            }
                        } else {
                            Log.e(TAG, "Failed to send verification email", task.getException());
                            if (listener != null) {
                                listener.onVerificationFailed(task.getException());
                            }
                        }
                    });
        }
    }

    /**
     * Checks if the current user's email is verified
     * This method requires a user to be currently signed in to Firebase
     * The user object is reloaded to ensure verification status is current
     */
    public void checkEmailVerification() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (user.isEmailVerified()) {
                        if (listener != null) {
                            listener.onVerificationSuccess();
                        }
                    }
                } else {
                    if (listener != null) {
                        listener.onVerificationFailed(task.getException());
                    }
                }
            });
        }
    }
}
package com.yunxin.midnighttarotai.auth;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yunxin.midnighttarotai.home.MainActivity;
import com.yunxin.midnighttarotai.R;
import com.yunxin.midnighttarotai.payment.PaymentManager;

import java.util.HashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

/**
 * RegisterActivity handles new user registration and email verification
 * This activity manages user registration through Firebase Authentication and stores user data in Firestore
 */
public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    // UI Components
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnRegister;
    private Button btnLogin;
    private Button btnHome;
    private ProgressDialog loadingDialog;

    // Firebase Components
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Utility Components
    private EmailVerificationHandler verificationHandler;
    private PaymentManager paymentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Enable Firebase debug logging
//        FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);

        initializeComponents();
        setupVerificationHandler();
        setupClickListeners();
    }

    /**
     * Initializes Firebase components and UI elements
     */
    private void initializeComponents() {
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        etUsername = findViewById(R.id.et_register_username);
        etEmail = findViewById(R.id.et_register_email);
        etPassword = findViewById(R.id.et_register_password);
        btnRegister = findViewById(R.id.btn_register_submit);
        btnLogin = findViewById(R.id.btn_goto_login);
        btnHome = findViewById(R.id.btn_register_home);
    }

    /**
     * Sets up the email verification handler and its listeners
     */
    private void setupVerificationHandler() {
        verificationHandler = new EmailVerificationHandler();
        verificationHandler.setVerificationListener(new EmailVerificationHandler.EmailVerificationListener() {
            @Override
            public void onVerificationEmailSent() {
                Toast.makeText(RegisterActivity.this,
                        "Verification email sent. Please check your inbox.",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onVerificationSuccess() {
                Toast.makeText(RegisterActivity.this,
                        "Email verified successfully!",
                        Toast.LENGTH_SHORT).show();
                navigateToMainActivity(etEmail.getText().toString());
            }

            @Override
            public void onVerificationFailed(Exception e) {
                Toast.makeText(RegisterActivity.this,
                        "Verification failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sets up click listeners for all interactive UI components
     */
    private void setupClickListeners() {
        btnRegister.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInputs(username, email, password)) {
                registerUser(username, email, password);
            }
        });

        btnLogin.setOnClickListener(view ->
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));

        btnHome.setOnClickListener(view ->
                startActivity(new Intent(RegisterActivity.this, MainActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() != null) {
            verificationHandler.checkEmailVerification();
        }
    }

    /**
     * Validates all input fields
     * @param username User's chosen username
     * @param email User's email address
     * @param password User's chosen password
     * @return boolean indicating if all inputs are valid
     */
    private boolean validateInputs(String username, String email, String password) {
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Please enter name");
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter proper email");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Please enter proper password");
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return false;
        }

        return true;
    }

    private void registerUser(final String username, String email, String password) {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Network unavailable during registration attempt");
            showNetworkError(() -> registerUser(username, email, password));
            return;
        }

        Log.d(TAG, "Starting registration process for email: " + email);
        showLoadingDialog("Creating account...");

        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().getSignInMethods().isEmpty()) {
                            Log.d(TAG, "Email is not registered, proceeding to create user");
                            createFirebaseUser(username, email, password);
                        } else {
                            Log.d(TAG, "Email already in use: " + email);
                            hideLoadingDialog();
                            Toast.makeText(RegisterActivity.this,
                                    "Email already in use",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Failed to fetch sign-in methods for email: " + email, task.getException());
                        hideLoadingDialog();
                        handleRegistrationError(task.getException());
                    }
                });
    }


    /**
     * Creates a new Firebase user account
     */
    private void createFirebaseUser(String username, String email, String password) {
        Log.d(TAG, "Creating Firebase user with email: " + email);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase user created successfully");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Sending verification email to: " + email);
                            sendVerificationEmail(user, username, email);
                        }
                    } else {
                        Log.e(TAG, "Firebase user creation failed", task.getException());
                        hideLoadingDialog();
                        handleRegistrationError(task.getException());
                    }
                });
    }

    /**
     * Handles different types of registration errors
     */
    private void handleRegistrationError(Exception exception) {
        String errorMessage;

        if (exception instanceof FirebaseNetworkException) {
            errorMessage = "Network error occurred. Please check your internet connection and try again.";
            Log.e(TAG, "Network error during registration", exception);
        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
            errorMessage = "Please choose a stronger password.";
            Log.e(TAG, "Weak password error during registration", exception);
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Invalid email format. Please check your email address.";
            Log.e(TAG, "Invalid credentials error during registration", exception);
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            errorMessage = "This email is already registered. Please use a different email or try logging in.";
            Log.e(TAG, "User collision error during registration", exception);
        } else {
            errorMessage = "Registration failed: " + exception.getMessage();
            Log.e(TAG, "Unknown error during registration", exception);
        }

        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Sends verification email to the user
     */
    private void sendVerificationEmail(FirebaseUser user, String username, String email) {
        Log.d(TAG, "Attempting to send verification email to: " + email);
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Verification email sent successfully");
                        saveUserDataToFirestore(user, username, email);
                    } else {
                        Log.e(TAG, "Failed to send verification email", task.getException());
                        Toast.makeText(RegisterActivity.this,
                                "Failed to send verification email",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Saves user data to Firestore database
     */
    private void saveUserDataToFirestore(FirebaseUser user, String username, String email) {
        Log.d(TAG, "Saving user data to Firestore for UID: " + user.getUid());
        showLoadingDialog("Finalizing registration...");
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("emailVerified", false);
        userData.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("MidnightTarot")
                .document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data saved to Firestore successfully");
                    hideLoadingDialog();
                    initializeUserPayment(user.getUid());
                    navigateToLogin();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save user data to Firestore", e);
                    hideLoadingDialog();
                    Toast.makeText(RegisterActivity.this,
                            "Error saving user data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Initializes payment settings for new user
     */
    private void initializeUserPayment(String userId) {
        paymentManager = new PaymentManager(this);
        paymentManager.initializeNewUser(userId);
    }

    /**
     * Navigates to login screen after successful registration
     */
    private void navigateToLogin() {
        Toast.makeText(RegisterActivity.this,
                "Registration successful! Please check your email.",
                Toast.LENGTH_LONG).show();
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }

    /**
     * Updates user's email verification status and navigates to main activity
     */
    private void navigateToMainActivity(String email) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("MidnightTarot")
                    .document(user.getUid())
                    .update("emailVerified", true)
                    .addOnSuccessListener(aVoid -> {
                        mAuth.signOut();
                        Toast.makeText(RegisterActivity.this,
                                "Email verified! Please sign in.",
                                Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error updating email verification status", e);
                    });
        }
    }

    /**
     * Checks if network connection is available
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        return false;
    }

    /**
     * Shows network error dialog with retry option
     */
    private void showNetworkError(Runnable retryAction) {
        new AlertDialog.Builder(this)
                .setTitle("Network Error")
                .setMessage("Unable to connect to the server. Please check your internet connection and try again.")
                .setPositiveButton("Retry", (dialog, which) -> retryAction.run())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Shows loading dialog during operations
     */
    private void showLoadingDialog(String message) {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(this);
            loadingDialog.setCancelable(false);
        }
        loadingDialog.setMessage(message);
        loadingDialog.show();
    }

    /**
     * Hides loading dialog
     */
    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

}
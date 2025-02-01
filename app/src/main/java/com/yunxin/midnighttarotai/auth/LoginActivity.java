package com.yunxin.midnighttarotai.auth;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yunxin.midnighttarotai.home.MainActivity;
import com.yunxin.midnighttarotai.R;
import com.yunxin.midnighttarotai.profile.UserProfileActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import androidx.appcompat.app.AlertDialog;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.FirebaseTooManyRequestsException;

/**
 * LoginActivity handles user authentication and login functionality
 * This activity manages user login through Firebase Authentication and stores user data in Firestore
 *
 * @author Yunxin
 * @version 2.0
 */
public class LoginActivity extends AppCompatActivity {

    // UI Components
    private EditText etEmail;
    private EditText etPassword;
    private Button btnRegister;
    private Button btnLogin;
    private Button btnHome;
    private Button btnForgotPassword;
    private ProgressDialog loadingDialog;

    // Firebase Components
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    /**
     * Initializes the activity, sets up UI components and Firebase instances
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeFirebase();
        initializeViews();
        handleIncomingIntent();
        setupClickListeners();
    }

    /**
     * Initializes Firebase Authentication and Firestore instances
     */
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Initializes and binds all UI components
     */
    private void initializeViews() {
        btnRegister = findViewById(R.id.btn_goto_register);
        btnLogin = findViewById(R.id.btn_login_submit);
        btnHome = findViewById(R.id.btn_login_home);
        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        btnForgotPassword = findViewById(R.id.btn_forgot_password);
    }

    /**
     * Handles any data passed to this activity through intent
     */
    private void handleIncomingIntent() {
        Intent intent = getIntent();
        String email = intent.getStringExtra("number1"); // Consider renaming these keys
        String password = intent.getStringExtra("number2");

        if (email != null) {
            etEmail.setText(email);
        }
        if (password != null) {
            etPassword.setText(password);
        }
    }

    /**
     * Sets up click listeners for all interactive UI components
     */
    private void setupClickListeners() {
        btnRegister.setOnClickListener(view ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        btnLogin.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            validateLogin(email, password);
        });

        btnHome.setOnClickListener(view ->
                startActivity(new Intent(LoginActivity.this, MainActivity.class)));

        btnForgotPassword.setOnClickListener(view ->
                startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class)));
    }

    /**
     * Validates user input and performs login operation
     * @param email User's email address
     * @param password User's password
     */
    private void validateLogin(String email, String password) {
        if (!validateInputs(email, password)) {
            return;
        }

        performFirebaseLogin(email, password);
    }

    /**
     * Validates that email and password fields are not empty
     * @param email User's email address
     * @param password User's password
     * @return boolean indicating if inputs are valid
     */
    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            etEmail.setError("Email cannot be empty");
            return false;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password cannot be empty");
            return false;
        }
        return true;
    }

    /**
     * Attempts to login user with Firebase Authentication
     * On success, retrieves additional user data from Firestore
     * @param email User's email address
     * @param password User's password
     */
    private void performFirebaseLogin(String email, String password) {
        // First check network connectivity
        if (!isNetworkAvailable()) {
            showNetworkError();
            return;
        }

        // Show loading indicator
        showLoadingDialog();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Hide loading indicator
                    hideLoadingDialog();

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            fetchUserDataFromFirestore(user);
                        }
                    } else {
                        handleLoginError(task.getException());
                    }
                });
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
    private void showNetworkError() {
        new AlertDialog.Builder(this)
                .setTitle("Network Error")
                .setMessage("Unable to connect to the server. Please check your internet connection and try again.")
                .setPositiveButton("Retry", (dialog, which) -> {
                    String email = etEmail.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();
                    performFirebaseLogin(email, password);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Shows loading dialog during authentication
     */
    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(this);
            loadingDialog.setMessage("Signing in...");
            loadingDialog.setCancelable(false);
        }
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

    /**
     * Handles different types of login errors
     */
    private void handleLoginError(Exception exception) {
        String errorMessage;

        if (exception instanceof FirebaseNetworkException) {
            errorMessage = "Network error occurred. Please check your internet connection and try again.";
        } else if (exception instanceof FirebaseAuthInvalidUserException) {
            errorMessage = "Account not found. Please check your email or sign up.";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Invalid password. Please try again.";
        } else if (exception instanceof FirebaseTooManyRequestsException) {
            errorMessage = "Too many login attempts. Please try again later.";
        } else {
            errorMessage = "Authentication failed: " + exception.getMessage();
        }

        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        Log.w(TAG, "signInWithEmail:failure", exception);
    }

    /**
     * Retrieves user data from Firestore and saves to SharedPreferences
     * @param user FirebaseUser object of the authenticated user
     */
    private void fetchUserDataFromFirestore(FirebaseUser user) {
        db.collection("MidnightTarot")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        saveUserDataAndNavigate(document.getString("username"),
                                document.getString("email"),
                                user.getUid());
                    } else {
                        // Handle case where user document doesn't exist
                        Log.w(TAG, "No user document found in Firestore");
                        Toast.makeText(LoginActivity.this,
                                "User profile not found",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error getting user data", e);
                    Toast.makeText(LoginActivity.this,
                            "Error retrieving user data",
                            Toast.LENGTH_SHORT).show();
                });
    }
    /**
     * Saves user data to SharedPreferences and navigates to UserProfile
     * @param username User's username
     * @param email User's email
     * @param userId User's unique identifier
     */
    private void saveUserDataAndNavigate(String username, String email, String userId) {
        // Save to SharedPreferences
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putString("userId", userId);
        editor.apply();

        // Show success message
        Toast.makeText(LoginActivity.this, "Login Successful!",
                Toast.LENGTH_SHORT).show();

        // Navigate to UserProfile
        Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("email", email);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }
}
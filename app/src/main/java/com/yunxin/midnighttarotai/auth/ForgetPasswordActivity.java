package com.yunxin.midnighttarotai.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.yunxin.midnighttarotai.R;

/**
 * ForgetPasswordActivity handles password reset functionality
 * This activity allows users to request a password reset email through Firebase Authentication
 */
public class ForgetPasswordActivity extends AppCompatActivity {

    // UI Components
    private EditText etEmail;
    private Button btnReset;
    private Button btnBack;

    // Firebase Components
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forget_password);

        initializeComponents();
        setupClickListeners();
        setupWindowInsets();
    }

    /**
     * Initializes Firebase components and UI elements
     */
    private void initializeComponents() {
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        etEmail = findViewById(R.id.et_forget_password_email);
        btnReset = findViewById(R.id.btn_forget_password_reset);
        btnBack = findViewById(R.id.btn_forget_password_back);
    }

    /**
     * Sets up click listeners for all interactive UI components
     */
    private void setupClickListeners() {
        btnReset.setOnClickListener(view -> handleResetPassword());
        btnBack.setOnClickListener(view -> finish());
    }

    /**
     * Configures window insets for edge-to-edge display
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.ll_forget_password_container),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                }
        );
    }

    /**
     * Handles the password reset process
     * Validates email input and sends reset email through Firebase
     */
    private void handleResetPassword() {
        String email = etEmail.getText().toString().trim();

        if (!validateEmail(email)) {
            return;
        }

        sendPasswordResetEmail(email);
    }

    /**
     * Validates the email input
     * @param email User's email address
     * @return boolean indicating if email is valid
     */
    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter a valid email");
            return false;
        }
        return true;
    }

    /**
     * Sends password reset email through Firebase
     * @param email User's email address
     */
    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showSuccessAndFinish();
                    } else {
                        showError(task.getException());
                    }
                });
    }

    /**
     * Shows success message and closes activity
     */
    private void showSuccessAndFinish() {
        Toast.makeText(ForgetPasswordActivity.this,
                "Password reset email sent. Please check your inbox.",
                Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * Shows error message when reset email fails
     * @param exception Exception containing error details
     */
    private void showError(Exception exception) {
        Toast.makeText(ForgetPasswordActivity.this,
                "Failed to send reset email: " + exception.getMessage(),
                Toast.LENGTH_LONG).show();
    }
}
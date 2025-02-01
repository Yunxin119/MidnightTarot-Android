package com.yunxin.midnighttarotai.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yunxin.midnighttarotai.R;

/**
 * ChangePasswordActivity handles user password changes
 * This activity allows users to update their password after re-authentication
 *
 * @author YunXin
 * @version 2.0
 */
public class ChangePasswordActivity extends AppCompatActivity {
    private static final String TAG = "ChangePasswordActivity";

    // UI Components
    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnChangePassword;
    private Button btnBack;
    private ProgressDialog progressDialog;

    // Firebase Components
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        if (!initializeFirebase()) {
            return;
        }

        initializeViews();
        setupClickListeners();
    }

    /**
     * Initializes Firebase components and checks user authentication
     * @return boolean indicating if initialization was successful
     */
    private boolean initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return false;
        }
        return true;
    }

    /**
     * Initializes all UI components
     */
    private void initializeViews() {
        etCurrentPassword = findViewById(R.id.current_password);
        etNewPassword = findViewById(R.id.new_password);
        etConfirmPassword = findViewById(R.id.confirm_password);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnBack = findViewById(R.id.btn_back);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating password...");
        progressDialog.setCancelable(false);
    }

    /**
     * Sets up click listeners for all interactive UI components
     */
    private void setupClickListeners() {
        btnChangePassword.setOnClickListener(view -> handlePasswordChange());
        btnBack.setOnClickListener(view -> finish());
    }

    /**
     * Handles the password change process
     */
    private void handlePasswordChange() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (validateInputs(currentPassword, newPassword, confirmPassword)) {
            showProgressDialog();
            updatePassword(currentPassword, newPassword);
        }
    }

    /**
     * Validates all input fields
     * @param currentPassword User's current password
     * @param newPassword User's new password
     * @param confirmPassword Confirmation of new password
     * @return boolean indicating if all inputs are valid
     */
    private boolean validateInputs(String currentPassword, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError("Please enter current password");
            return false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Please enter new password");
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm new password");
            return false;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return false;
        }

        if (currentPassword.equals(newPassword)) {
            etNewPassword.setError("New password must be different from current password");
            return false;
        }

        return true;
    }

    /**
     * Updates user's password after re-authentication
     * @param currentPassword User's current password
     * @param newPassword User's new password
     */
    private void updatePassword(String currentPassword, String newPassword) {
        AuthCredential credential = EmailAuthProvider.getCredential(
                currentUser.getEmail(),
                currentPassword
        );

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        performPasswordUpdate(newPassword);
                    } else {
                        hideProgressDialog();
                        Log.w(TAG, "Re-authentication failed", task.getException());
                        Toast.makeText(this,
                                "Current password is incorrect",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Performs the actual password update
     * @param newPassword User's new password
     */
    private void performPasswordUpdate(String newPassword) {
        currentUser.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    hideProgressDialog();
                    if (task.isSuccessful()) {
                        handleSuccessfulUpdate();
                    } else {
                        Log.w(TAG, "Error updating password", task.getException());
                        Toast.makeText(this,
                                "Failed to update password: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Handles successful password update
     */
    private void handleSuccessfulUpdate() {
        Toast.makeText(this,
                "Password updated successfully, please login again :)",
                Toast.LENGTH_SHORT).show();
        mAuth.signOut();
        navigateToLogin();
    }

    /**
     * Navigates to login screen
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Shows progress dialog
     */
    private void showProgressDialog() {
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    /**
     * Hides progress dialog
     */
    private void hideProgressDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
package com.yunxin.midnighttarotai.profile;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.yunxin.midnighttarotai.auth.ChangePasswordActivity;
import com.yunxin.midnighttarotai.auth.LoginActivity;
import com.yunxin.midnighttarotai.home.MainActivity;
import com.yunxin.midnighttarotai.R;
//import com.yunxin.midnighttarotai.reading.SavedReadingsActivity;

/**
 * UserProfile activity handles user profile management and display
 * Allows users to view and edit their profile information, manage credits,
 * and access saved readings
 */
public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfile";
    private static final String PREFS_NAME = "UserPrefs";
    private static final int PERMISSION_REQUEST_CODE = 1;

    // UI Components
    private ImageView profileImage;
    private TextView userName;
    private TextView userEmail;
    private Button homeButton;
    private TextView creditsValue;
    private TextView creditsText;
    private MaterialCardView creditsCard;
    private MaterialCardView savedReadingsCard;

    // Data
    private SharedPreferences preferences;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);

        try {
            initializeViews();
            loadUserData();
            setupClickListeners();
            checkPermissions();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing UserProfile", e);
            showErrorAndFinish();
        }
    }

    /**
     * Initializes all UI components
     */
    private void initializeViews() {
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        homeButton = findViewById(R.id.home_button);
        profileImage = findViewById(R.id.profile_image);
        userName = findViewById(R.id.user_name);
        userEmail = findViewById(R.id.user_email);
        creditsValue = findViewById(R.id.credits_value);
        creditsText = findViewById(R.id.credits_text);
        creditsCard = findViewById(R.id.credits_card);
        savedReadingsCard = findViewById(R.id.saved_readings_card);
        profileImage.bringToFront();
    }

    /**
     * Loads user data from Intent or SharedPreferences
     */
    private void loadUserData() {
        Intent intent = getIntent();
        String name = intent.getStringExtra("username");
        String email = intent.getStringExtra("email");
        userId = intent.getStringExtra("userId");

        if (name == null || email == null || userId == null) {
            loadFromPreferences();
        } else {
            saveToPreferences(name, email, userId);
        }

        updateUI(name, email);
        loadProfileImage();
        setupCardsContent();
    }

    /**
     * Loads user data from SharedPreferences
     */
    private void loadFromPreferences() {
        userName.setText(preferences.getString("username", "Default User"));
        userEmail.setText(preferences.getString("email", "default@email.com"));
        userId = preferences.getString("userId", "");
    }

    /**
     * Saves user data to SharedPreferences
     */
    private void saveToPreferences(String name, String email, String userId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username", name);
        editor.putString("email", email);
        editor.putString("userId", userId);
        editor.apply();
    }

    /**
     * Updates UI with user data
     */
    private void updateUI(String name, String email) {
        userName.setText(name);
        userEmail.setText(email);
    }

    /**
     * Loads and displays profile image
     */
    private void loadProfileImage() {
        String savedImageUri = preferences.getString("profileImageUri", null);
        if (savedImageUri != null) {
            try {
                Uri imageUri = Uri.parse(savedImageUri);
                profileImage.setImageURI(imageUri);
            } catch (Exception e) {
                Log.e(TAG, "Failed to load profile image", e);
                setDefaultProfileImage();
            }
        } else {
            setDefaultProfileImage();
        }
    }

    /**
     * Sets default profile image
     */
    private void setDefaultProfileImage() {
        profileImage.setImageResource(R.drawable.profileimg_default_value);
    }

    /**
     * Sets up credits and saved readings cards
     */
    private void setupCardsContent() {
        updateCreditsDisplay("0", "Get more readings");
        setupSavedReadingsCard();
    }

    /**
     * Updates credits display
     */
    private void updateCreditsDisplay(String value, String description) {
        creditsValue.setText(value);
        creditsText.setText(description);
    }

    /**
     * Sets up saved readings card click listener
     */
    private void setupSavedReadingsCard() {
//        savedReadingsCard.setOnClickListener(v -> {
//            Intent savedReadings = new Intent(UserProfileActivity.this, SavedReadingsActivity.class);
//            startActivity(savedReadings);
//        });
    }

    /**
     * Sets up click listeners for all interactive UI components
     */
    private void setupClickListeners() {
        Button changePasswordButton = findViewById(R.id.change_password_button);
        Button logoutButton = findViewById(R.id.logout_button);

        homeButton.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));

        changePasswordButton.setOnClickListener(v -> {
            if (userId != null) {
                Intent intent = new Intent(this, ChangePasswordActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(v -> handleLogout());

        profileImage.setOnClickListener(v -> {
            if (checkPermissions()) {
                openImagePicker();
            } else {
                requestPermissions();
            }
        });
    }

    /**
     * Handles user logout
     */
    private void handleLogout() {
        clearUserData();
        navigateToLogin();
    }

    /**
     * Clears user data from SharedPreferences
     */
    private void clearUserData() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.remove("username");
        editor.remove("email");
        editor.remove("userId");
        editor.apply();
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
     * Checks for required permissions
     */
    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * Requests required permissions
     */
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        }
    }

    /**
     * Opens image picker for profile photo
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            profileImage.setImageURI(imageUri);
                            saveProfileImageUri(imageUri);
                        } catch (Exception e) {
                            Log.e(TAG, "Error setting profile image", e);
                            Toast.makeText(this, "Failed to set profile image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    /**
     * Saves profile image URI to SharedPreferences
     */
    private void saveProfileImageUri(Uri imageUri) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("profileImageUri", imageUri.toString());
        editor.apply();
    }

    /**
     * Shows error message and finishes activity
     */
    private void showErrorAndFinish() {
        Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
        finish();
    }
}
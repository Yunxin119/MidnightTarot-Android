package com.yunxin.midnighttarotai.settings;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yunxin.midnighttarotai.R;

/**
 * SettingsActivity allows users to configure app preferences such as sound, dark mode, and tutorial visibility.
 */
public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    private SettingsManager settingsManager;
    private FirebaseUser currentUser;

    // UI Components
    private ConstraintLayout tutorialSettings;
    private SwitchMaterial soundSwitch;
    private SwitchMaterial darkModeSwitch;
    private SwitchMaterial tutorialSwitch;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        try {
            initializeViews(); // Initialize UI components
            settingsManager = new SettingsManager(this); // Initialize settings manager
            currentUser = FirebaseAuth.getInstance().getCurrentUser(); // Get current user

            setupInitialState(); // Set initial UI state
            setupListeners(); // Set up event listeners
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e); // Log errors during initialization
        }
    }

    /**
     * Initializes all UI components from the layout.
     */
    private void initializeViews() {
        backButton = findViewById(R.id.home_button);
        soundSwitch = findViewById(R.id.soundSwitch);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        tutorialSwitch = findViewById(R.id.tutorialSwitch);
        tutorialSettings = findViewById(R.id.tutorial_settings);

        // Set up back button to close the activity
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Sets the initial state of the UI components based on saved settings.
     */
    private void setupInitialState() {
        // Set sound switch state
        if (soundSwitch != null) {
            soundSwitch.setChecked(settingsManager.isSoundEnabled());
        }

        // Set dark mode switch state based on system/app theme
        if (darkModeSwitch != null) {
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            boolean isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES;
            darkModeSwitch.setChecked(isDarkMode);
            settingsManager.setDarkMode(isDarkMode); // Sync with settings
        }

        // Set tutorial switch state
        if (tutorialSwitch != null) {
            tutorialSwitch.setChecked(settingsManager.isShowTutorial());
        }

        // Show tutorial settings only for authenticated users
        if (tutorialSettings != null) {
            tutorialSettings.setVisibility(currentUser != null ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Sets up listeners for UI components to handle user interactions.
     */
    private void setupListeners() {
        // Sound switch listener
        if (soundSwitch != null) {
            soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                settingsManager.setSoundEnabled(isChecked); // Save sound preference
            });
        }

        // Dark mode switch listener
        if (darkModeSwitch != null) {
            darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                settingsManager.setDarkMode(isChecked); // Save dark mode preference
                updateTheme(isChecked); // Apply theme changes
            });
        }

        // Tutorial switch listener
        if (tutorialSwitch != null) {
            tutorialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                settingsManager.setShowTutorial(isChecked); // Save tutorial preference
            });
        }
    }

    /**
     * Updates the app theme based on the dark mode preference.
     *
     * @param isDarkMode True if dark mode is enabled, false otherwise.
     */
    private void updateTheme(boolean isDarkMode) {
        try {
            int newNightMode = isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            AppCompatDelegate.setDefaultNightMode(newNightMode); // Apply theme
            recreate(); // Recreate activity to reflect theme changes
        } catch (Exception e) {
            Log.e(TAG, "Error updating theme", e); // Log theme update errors
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Update dark mode switch when system theme changes
        if (darkModeSwitch != null) {
            int currentNightMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
            boolean isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES;

            // Sync switch state with system theme
            if (darkModeSwitch.isChecked() != isDarkMode) {
                darkModeSwitch.setChecked(isDarkMode);
                settingsManager.setDarkMode(isDarkMode);
            }
        }
    }
}
package com.yunxin.midnighttarotai.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.yunxin.midnighttarotai.R;
import com.yunxin.midnighttarotai.home.DailyTarotFragment;
import com.yunxin.midnighttarotai.home.TarotInquiryFragment;
//import com.yunxin.midnighttarotai.learnspreads.SpreadGridActivity;
//import com.yunxin.midnighttarotai.settings.SettingsActivity;
import com.yunxin.midnighttarotai.auth.LoginActivity;
import com.yunxin.midnighttarotai.profile.UserProfileActivity;
import com.yunxin.midnighttarotai.settings.SettingsActivity;

import java.util.ArrayList;

/**
 * MainActivity serves as the primary entry point for the Midnight Tarot AI application.
 * It manages the navigation between different fragments and handles user authentication state.
 *
 * Key features:
 * - Fragment management for daily tarot and tarot inquiry screens
 * - Navigation to user profile, settings, and spread learning sections
 * - Window insets handling for proper UI display
 */
public class MainActivity extends AppCompatActivity {
    // Fragment Management Components
    private Fragment mDailyTarotFragment;
    private Fragment mTarotInquiryFragment;
    private FragmentManager mFragmentManager;
    private int mCurrentFragmentIndex = 0;
    private ArrayList<Fragment> mFragments = new ArrayList<>();

    // UI Components
    private Button mBtnProfile;
    private Button mBtnNavigateLeft;
    private Button mBtnNavigateRight;
    private Button mBtnSettings;
    private Button mBtnSpreads;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupFragments();
        setupNavigation();
        setupWindowInsets();
    }

    /**
     * Initializes all UI components by finding their respective views in the layout.
     */
    private void initializeViews() {
        mBtnProfile = findViewById(R.id.btn_profile);
        mBtnNavigateLeft = findViewById(R.id.btn_navigate_left);
        mBtnNavigateRight = findViewById(R.id.btn_navigate_right);
        mBtnSettings = findViewById(R.id.btn_settings);
        mBtnSpreads = findViewById(R.id.btn_spreads);
    }

    /**
     * Sets up the main fragments and initializes fragment management.
     * Creates instances of DailyTarotFragment and TarotInquiryFragment.
     */
    private void setupFragments() {
        mDailyTarotFragment = new DailyTarotFragment();
        mTarotInquiryFragment = new TarotInquiryFragment();
        mFragmentManager = getSupportFragmentManager();
        mFragments.add(mDailyTarotFragment);
        mFragments.add(mTarotInquiryFragment);

        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, mDailyTarotFragment)
                .commit();
    }

    /**
     * Configures navigation functionality including:
     * - Profile/Login button behavior based on authentication state
     * - Settings navigation
     * - Fragment navigation
     * - Spread learning section access
     */
    private void setupNavigation() {
        // Check authentication status
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);

        // Configure profile navigation
        mBtnProfile.setOnClickListener(v -> {
            if (isLoggedIn) {
                Intent profileIntent = new Intent(MainActivity.this, UserProfileActivity.class);
                profileIntent.putExtra("username", preferences.getString("username", ""));
                profileIntent.putExtra("email", preferences.getString("email", ""));
                profileIntent.putExtra("userId", preferences.getString("userId", ""));
                startActivity(profileIntent);
            } else {
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        // Configure settings navigation
        mBtnSettings.setOnClickListener(v -> {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        });

        // Configure fragment navigation
        mBtnNavigateLeft.setOnClickListener(v -> showPreviousFragment());
        mBtnNavigateRight.setOnClickListener(v -> showNextFragment());

        // Configure spreads learning navigation
//        mBtnSpreads.setOnClickListener(v -> {
//            Intent spreadIntent = new Intent(MainActivity.this, SpreadGridActivity.class);
//            startActivity(spreadIntent);
//        });
    }

    /**
     * Configures window insets to ensure proper UI display accounting for system bars.
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Navigates to the previous fragment with a right-to-left animation.
     */
    private void showPreviousFragment() {
        mCurrentFragmentIndex = (mCurrentFragmentIndex - 1 + 2) % 2;
        mFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
                .replace(R.id.fragment_container, mFragments.get(mCurrentFragmentIndex))
                .commit();
    }

    /**
     * Navigates to the next fragment with a left-to-right animation.
     */
    private void showNextFragment() {
        mCurrentFragmentIndex = (mCurrentFragmentIndex + 1) % 2;
        mFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
                .replace(R.id.fragment_container, mFragments.get(mCurrentFragmentIndex))
                .commit();
    }
}
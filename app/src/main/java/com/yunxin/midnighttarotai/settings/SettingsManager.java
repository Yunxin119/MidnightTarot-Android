package com.yunxin.midnighttarotai.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
    private final SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "app_settings";

    public SettingsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isSoundEnabled() {
        return sharedPreferences.getBoolean("sound_enabled", true);
    }

    public void setSoundEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean("sound_enabled", enabled).apply();
    }

    public boolean isDarkMode() {
        return sharedPreferences.getBoolean("dark_mode", false);
    }

    public void setDarkMode(boolean enabled) {
        sharedPreferences.edit().putBoolean("dark_mode", enabled).apply();
    }

    public boolean isShowTutorial() {
        return sharedPreferences.getBoolean("show_tutorial", true);
    }

    public void setShowTutorial(boolean enabled) {
        sharedPreferences.edit().putBoolean("show_tutorial", enabled).apply();
    }
}
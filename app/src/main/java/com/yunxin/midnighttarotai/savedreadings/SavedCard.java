package com.yunxin.midnighttarotai.savedreadings;

public class SavedCard {
    private String name;
    private boolean isReversed;
    private int position;

    public SavedCard() {
        // Empty constructor for Firebase
    }

    public SavedCard(String name, boolean isReversed, int position) {
        this.name = name;
        this.isReversed = isReversed;
        this.position = position;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isReversed() { return isReversed; }
    public void setReversed(boolean reversed) { isReversed = reversed; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
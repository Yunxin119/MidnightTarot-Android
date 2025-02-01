package com.yunxin.midnighttarotai.cardpicking;

/**
 * Constants for different types of spreads.
 * Centralizes spread type definitions to avoid string literals.
 */
public final class SpreadType {
    public static final String ONE_CARD = "OneCard";
    public static final String HEXAGRAM = "Hexagram";
    public static final String TWO_OPTIONS = "TwoOptions";
    public static final String HORSESHOE = "Horseshoe";
    public static final String CROSS = "Cross";
    public static final String TIMELINE = "Timeline";
    public static final String COMPANION = "Companion";

    private SpreadType() {
        // Prevent instantiation
    }
}
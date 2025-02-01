package com.yunxin.midnighttarotai.utils;

import android.util.Log;

import com.yunxin.midnighttarotai.savedreadings.SavedCard;

import java.util.ArrayList;
import java.util.List;

public class SaveReadingUtils {
    /**
     * Parse card info from string format: "position: cardName-reversed" or "cut card: cardName-reversed"
     * @param cardInfo Card info string
     * @return SavedCard object
     */
    public static SavedCard parseCardInfo(String cardInfo) {
        try {
            // Handle cut card case
            if (cardInfo.toLowerCase().startsWith("cut card:")) {
                String cardDetails = cardInfo.substring(9).trim();
                boolean isReversed = cardDetails.toLowerCase().endsWith("-reversed");
                String cardName = isReversed ?
                        cardDetails.substring(0, cardDetails.length() - 9).trim() :
                        cardDetails.trim();

                Log.d("Reading Utils", "Parsed Cut Card: " + cardName + " (Reversed: " + isReversed + ")");

                return new SavedCard(cardName, isReversed, 0);
            }

            // Handle normal cards
            String[] parts = cardInfo.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid card info format: " + cardInfo);
            }

            int position = Integer.parseInt(parts[0].trim());
            String cardDetails = parts[1].trim();
            boolean isReversed = cardDetails.endsWith("-reversed");
            String cardName = isReversed ?
                    cardDetails.substring(0, cardDetails.length() - 9).trim() :
                    cardDetails.trim();

            return new SavedCard(cardName, isReversed, position);
        } catch (Exception e) {
            Log.e("Reading Utils ERROR", "Error parsing card info: " + cardInfo, e);
            throw new IllegalArgumentException("Failed to parse card info", e);
        }
    }

    /**
     * Convert list of card info strings to SavedCard objects
     * @param cardInfoList List of card info strings
     * @return List of SavedCard objects
     */
    public static List<SavedCard> parseCardInfoList(List<String> cardInfoList) {
        List<SavedCard> savedCards = new ArrayList<>();
        for (String cardInfo : cardInfoList) {
            savedCards.add(parseCardInfo(cardInfo));
        }
        return savedCards;
    }
}

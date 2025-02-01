package com.yunxin.midnighttarotai.cardpicking;

import android.content.Context;
import android.graphics.Bitmap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.yunxin.midnighttarotai.utils.CardPickUtils;

/**
 * Manages the initialization and handling of tarot cards.
 * Responsible for loading card images and maintaining the deck state.
 */
public class CardPick {
    private final List<Card> mCardList = new ArrayList<>();
    private final Context mContext;

    /**
     * Constructs a new CardPick instance and initializes the deck
     *
     * @param context Application context for resource access
     */
    public CardPick(Context context) {
        this.mContext = context;
        initializeCards();
        shuffleDeck();
    }

    /**
     * Initializes the deck by loading card images and creating card objects
     */
    private void initializeCards() {
        Bitmap backImage = CardPickUtils.getCardBackImage(mContext);
        String[] cardFiles = CardPickUtils.listCardFiles(mContext);

        if (cardFiles != null) {
            for (String fileName : cardFiles) {
                String filePath = "cards/" + fileName;
                Bitmap frontImage = CardPickUtils.loadBitmapFromAssets(mContext, filePath);

                if (frontImage != null) {
                    String cardName = CardPickUtils.extractCardName(fileName);
                    Card card = new Card(frontImage, backImage, cardName,
                            "Description for " + cardName);
                    mCardList.add(card);
                }
            }
        }
    }

    /**
     * Shuffles the deck randomly
     */
    private void shuffleDeck() {
        Collections.shuffle(mCardList);
    }

    /**
     * Returns the current deck of cards
     *
     * @return List of Card objects representing the current deck
     */
    public List<Card> getCardList() {
        return new ArrayList<>(mCardList);
    }
}
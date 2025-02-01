package com.yunxin.midnighttarotai.cardpicking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.yunxin.midnighttarotai.R;
import com.yunxin.midnighttarotai.result.ResultActivity;
import com.yunxin.midnighttarotai.settings.SettingsManager;
import com.yunxin.midnighttarotai.tutorial.TutorialOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Activity for handling tarot card selection and spread layout.
 * Manages the card selection process, layout presentation, and transitions to the result screen.
 */
public class CardPickActivity extends AppCompatActivity implements CardPickView.OnCardSelectedListener {
    private static final String TAG = "CardPickActivity";

    // Layout Components
    private List<ImageView> mCardSlots;
    private Button mBtnProceed;
    private CardPickView mCardPickView;

    // State Management
    private String mSpreadType;
    private String mQuestion;
    private int mPickCardLimit;
    private int mSelectedSlotIndex;
    private List<String> mSelectedCards;
    private HashMap<ImageView, Card> mCardMap;

    // Settings
    private SettingsManager mSettingsManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("CardPickView", "LoadingCardPickActivity");

        initializeComponents();
        setupCardPickView();
        setupProceedButton();
    }

    /**
     * Initializes all activity components and data structures
     */
    private void initializeComponents() {
        mSettingsManager = new SettingsManager(this);
        mCardSlots = new ArrayList<>();
        mSelectedCards = new ArrayList<>();
        mCardMap = new HashMap<>();
        mSelectedSlotIndex = 0;

        if (!initializeData()) {
            finish();
            return;
        }

        loadLayout(mSpreadType);
        initializeCardSlots();

        if (mSettingsManager.isShowTutorial()) {
            showTutorial();
        }
    }

    /**
     * Initializes activity data from intent extras
     */
    private boolean initializeData() {
        try {
            Intent intent = getIntent();
            if (intent == null) {
                showError(getString(R.string.error_intent_data));
                return false;
            }

            mSpreadType = getSpreadType(intent);
            mQuestion = getQuestion(intent);
            mPickCardLimit = intent.getIntExtra("pick", 3);

            logInitialization();
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error in initializeData: " + e.getMessage());
            showError(getString(R.string.error_initialization));
            return false;
        }
    }

    /**
     * Retrieves and validates spread type from intent
     */
    @NonNull
    private String getSpreadType(Intent intent) {
        String spreadType = intent.getStringExtra("spreadType");
        if (spreadType == null || spreadType.trim().isEmpty()) {
            showError(getString(R.string.error_invalid_spread));
            return "Timeline";
        }
        return spreadType;
    }

    /**
     * Retrieves and validates question from intent
     */
    @NonNull
    private String getQuestion(Intent intent) {
        String question = intent.getStringExtra("question");
        if (question == null || question.trim().isEmpty()) {
            showError(getString(R.string.error_no_question));
            return getString(R.string.default_question);
        }
        return question;
    }

    /**
     * Logs initialization parameters for debugging
     */
    private void logInitialization() {
        Log.d(TAG, String.format(
                "Initialized with: spreadType=%s, pickCard=%d",
                mSpreadType, mPickCardLimit));
    }

    /**
     * Loads the appropriate layout based on spread type
     */
    private void loadLayout(String layoutType) {
        int layoutRes = getLayoutResource(layoutType);
        setContentView(layoutRes);
    }

    /**
     * Gets the layout resource ID for the given spread type
     */
    private int getLayoutResource(String layoutType) {
        switch (layoutType) {
            case "One Card":
                return R.layout.layout_one_card;
            case "Hexagram":
                return R.layout.layout_hexagram;
            case "Two Options":
                return R.layout.layout_two_options;
            case "Horseshoe":
                return R.layout.layout_horseshoe;
            case "Love Cross":
                return R.layout.layout_love_cross;
            case "Timeline":
                return R.layout.layout_timeline;
//            case "Companion":
//                return R.layout.layout_companion;
            default:
                throw new IllegalArgumentException("Unknown layout type: " + layoutType);
        }
    }

    /**
     * Initializes card slots from layout
     */
    private void initializeCardSlots() {
        int i = 1;
        while (true) {
            String slotId = "card" + i;
            int resId = getResources().getIdentifier(slotId, "id", getPackageName());

            if (resId == 0) break;

            ImageView slot = findViewById(resId);
            if (slot != null) {
                mCardSlots.add(slot);
            }
            i++;
        }
    }

    /**
     * Sets up the CardPickView with necessary configurations
     */
    private void setupCardPickView() {
        mCardPickView = findViewById(R.id.cardPickView);
        mCardPickView.setOnCardSelectedListener(this);
        mCardPickView.initialize(mPickCardLimit);
    }

    /**
     * Sets up the proceed button with animation and click handling
     */
    private void setupProceedButton() {
        mBtnProceed = findViewById(R.id.myButton);
        if (mBtnProceed == null) {
            Log.e(TAG, "Failed to find proceed button");
            return;
        }

        Log.d(TAG, "Successfully initialized proceed button");
        mBtnProceed.setVisibility(View.GONE);
        mBtnProceed.setOnClickListener(v -> navigateToResult());
    }
    @Override
    public void onCardSelected(Card selectedCard) {
        try {
            if (mSelectedSlotIndex < mCardSlots.size()) {
                handleCardSelection(selectedCard);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error selecting card: " + e.getMessage());
            showError(getString(R.string.error_card_selection));
        }
    }

    /**
     * Handles the selection of a card and its animation
     */
    private void handleCardSelection(Card selectedCard) {
        ImageView currentSlot = mCardSlots.get(mSelectedSlotIndex);
        currentSlot.setImageBitmap(selectedCard.getImage());
        mCardMap.put(currentSlot, selectedCard);

        Log.d(TAG, "Handling card selection: " + (mSelectedSlotIndex + 1) +
                " of " + mPickCardLimit);

        selectedCard.flipWithAnimation(currentSlot, () -> {
            addSelectedCard(selectedCard);
            mSelectedSlotIndex++;
            Log.d(TAG, "Card animation completed, new index: " + mSelectedSlotIndex);

            if (isSelectionComplete()) {
                Log.d(TAG, "Selection complete, showing proceed button");
                showProceedButton();
            }
        });
    }

    /**
     * Adds a selected card to the list with proper formatting
     */
    private void addSelectedCard(Card card) {
        String cardInfo = String.format("%d: %s%s",
                mSelectedSlotIndex + 1,
                card.getName(),
                card.getIsReversed() ? "-reversed" : "");
        mSelectedCards.add(cardInfo);
    }

    /**
     * Checks if all required cards have been selected
     */
    private boolean isSelectionComplete() {
        boolean complete = mSelectedSlotIndex == mPickCardLimit;
        Log.d(TAG, "Selection complete check: " + complete +
                " (selected: " + mSelectedSlotIndex +
                ", limit: " + mPickCardLimit + ")");
        return complete;
    }

    /**
     * Shows the proceed button with animation
     */
    private void showProceedButton() {
        if (mBtnProceed == null) {
            Log.e(TAG, "Cannot show null proceed button");
            return;
        }

        mBtnProceed.setAlpha(1f);
        mBtnProceed.setScaleX(1f);
        mBtnProceed.setScaleY(0f);
        mBtnProceed.animate()
                .scaleY(1f)
                .setDuration(300)
                .withStartAction(() -> {
                    mBtnProceed.setVisibility(View.VISIBLE);
                })
                .start();
        mBtnProceed.bringToFront();
    }
    /**
     * Navigates to the result screen with selected cards
     */
    private void navigateToResult() {
        ArrayList<Bitmap> cardImages = collectCardImages();
        Intent intent = createResultIntent();
        startActivity(intent);
        finish();
    }

    /**
     * Collects bitmap images from selected cards
     */
    private ArrayList<Bitmap> collectCardImages() {
        ArrayList<Bitmap> cardImages = new ArrayList<>();
        for (ImageView slot : mCardSlots) {
            Card card = mCardMap.get(slot);
            if (card != null) {
                Bitmap image = card.getImage();
                if (image != null && !image.isRecycled()) {
                    cardImages.add(image);
                }
            }
        }
        return cardImages;
    }

    /**
     * Creates the intent for the result activity
     */
    private Intent createResultIntent() {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putExtra("spreadType", mSpreadType);
        intent.putExtra("question", mQuestion);
        intent.putStringArrayListExtra("cardPicked", (ArrayList<String>) mSelectedCards);


        return intent;
    }

    /**
     * Shows a tutorial overlay for new users
     */
    private void showTutorial() {
        TutorialOverlay tutorial = new TutorialOverlay(this);
        ((ViewGroup) getWindow().getDecorView()).addView(tutorial);
    }

    /**
     * Shows an error message to the user
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupResources();
    }

    /**
     * Cleans up resources when activity is destroyed
     */
    private void cleanupResources() {
        for (Card card : mCardMap.values()) {
            Bitmap bitmap = card.getImage();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        mCardMap.clear();
    }
}
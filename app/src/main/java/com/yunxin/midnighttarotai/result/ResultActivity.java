package com.yunxin.midnighttarotai.result;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yunxin.midnighttarotai.BuildConfig;
import com.yunxin.midnighttarotai.home.MainActivity;
import com.yunxin.midnighttarotai.R;
import com.yunxin.midnighttarotai.payment.DailyReadingManager;
import com.yunxin.midnighttarotai.payment.PaymentManager;
import com.yunxin.midnighttarotai.utils.CardImageLoaderUtils;
//import com.yunxin.midnighttarotai.reading.FirebaseReadingManager;
//import com.yunxin.midnighttarotai.reading.Reading;
//import com.yunxin.midnighttarotai.reading.ReadingUtils;
//import com.yunxin.midnighttarotai.reading.SavedCard;
import com.yunxin.midnighttarotai.settings.SettingsManager;
import com.yunxin.midnighttarotai.utils.FadeEdgeImageView;
import com.yunxin.midnighttarotai.utils.OpenAIHelper;
import com.yunxin.midnighttarotai.utils.ResultAnimationUtils;
//import com.yunxin.midnighttarotai.utils.ShareImageGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for displaying tarot reading results.
 * Manages the display of card interpretations, animations, and user interactions.
 */
public class ResultActivity extends AppCompatActivity {
    private static final String TAG = "ResultActivity"; // Tag for logging
    private static final long REQUEST_TIMEOUT = 20000; // 20 seconds timeout for API requests

    // UI Components - Content Display
    private TextView txtLoadingMessage; // Displays loading messages
    private TextView txtReadingContent; // Displays the tarot reading content
    private FadeEdgeImageView imgSelectedCard; // Displays the selected tarot card image
    private ImageView imgDecorative; // Decorative image for animations
    private View containerContent; // Container for the main content
    private View containerScroll; // Container for scrollable content

    // UI Components - Controls
    private MaterialButton btnNext; // Button to navigate to the next interpretation
    private MaterialButton btnPrevious; // Button to navigate to the previous interpretation
    private MaterialButton btnShare; // Button to share the reading
    private MaterialButton btnSave; // Button to save the reading
    private View containerNavigation; // Container for navigation buttons

    // UI Components - Error Handling
    private View containerError; // Container for error messages
    private TextView txtErrorMessage; // Displays error messages
    private MaterialButton btnRetry; // Button to retry failed operations

    // State Management
    private boolean isReadingSaved = false; // Tracks if the reading is saved
    private String currentReadingId; // ID of the current reading
    private ArrayList<String> selectedCards; // List of selected tarot cards
    private String spreadType; // Type of tarot spread used
    private String userQuestion; // User's question for the reading
    private List<Bitmap> cardImages; // Bitmaps of the selected tarot cards

    // Services and Managers
    private ResultViewModel viewModel; // ViewModel for managing UI state
    private MediaPlayer musicPlayer; // MediaPlayer for background music
    private SettingsManager settingsManager; // Manages app settings
    private DailyReadingManager dailyReadingManager; // Manages daily reading limits
    private PaymentManager paymentManager; // Manages in-app purchases and credits
//    private FirebaseReadingManager readingManager; // Manages reading data in Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        initializeManagers(); // Initialize service managers
        initializeViews(); // Initialize UI components
        loadIntentData(); // Load data from the intent
        setupViewObservers(); // Set up LiveData observers
        setupClickListeners(); // Set up button click listeners
        setupInitialState(); // Set initial UI state
        generateReading(); // Generate the tarot reading
    }

    /**
     * Initializes service managers and ViewModel.
     */
    private void initializeManagers() {
        dailyReadingManager = new DailyReadingManager(this);
        paymentManager = new PaymentManager(this);
        settingsManager = new SettingsManager(this);
//        readingManager = new FirebaseReadingManager();
        viewModel = new ViewModelProvider(this).get(ResultViewModel.class);
    }

    /**
     * Initializes UI components from the layout.
     */
    private void initializeViews() {
        // Content Views
        txtLoadingMessage = findViewById(R.id.txtLoadingMessage);
        txtReadingContent = findViewById(R.id.txtReadingContent);
        imgSelectedCard = findViewById(R.id.imgSelectedCard);
        imgDecorative = findViewById(R.id.imgDecorativeTurning);
        containerContent = findViewById(R.id.contentContainer);
        containerScroll = findViewById(R.id.scrollContent);

        // Control Views
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnShare = findViewById(R.id.btnShare);
        btnSave = findViewById(R.id.btnSaveReading);
        containerNavigation = findViewById(R.id.containerNavigation);

        // Error Views
        containerError = findViewById(R.id.containerError);
        txtErrorMessage = findViewById(R.id.txtErrorMessage);
        btnRetry = findViewById(R.id.btnRetry);

        // Setup text scrolling
        txtReadingContent.setMovementMethod(new ScrollingMovementMethod());
    }

    /**
     * Sets the initial state of the UI, including animations and visibility.
     */
    private void setupInitialState() {
        // Set initial visibility and properties
        txtReadingContent.setAlpha(0f);
        btnShare.setAlpha(0f);
        containerNavigation.setAlpha(0f);
        btnShare.setTranslationY(-100f);
        containerNavigation.setTranslationY(100f);

        // Start animations
        new Handler().postDelayed(() -> ResultAnimationUtils
                .startEntranceAnimations(btnShare, containerNavigation), 200);
        ResultAnimationUtils.startSpinningAnimation(imgDecorative);

        // Setup music if enabled
        if (settingsManager.isSoundEnabled()) {
            setupBackgroundMusic();
        }
    }

    /**
     * Sets up click listeners for UI buttons.
     */
    private void setupClickListeners() {
        btnNext.setOnClickListener(v -> handleNextClick());
        btnPrevious.setOnClickListener(v -> handlePreviousClick());
        btnShare.setOnClickListener(v -> handleShareClick());
        btnRetry.setOnClickListener(v -> handleRetryClick());
        btnSave.setOnClickListener(v -> handleSaveClick());
    }

    /**
     * Sets up LiveData observers for UI state changes.
     */
    private void setupViewObservers() {
        // Loading state observer
        viewModel.isLoading().observe(this, this::handleLoadingState);

        // Content observers
        viewModel.getCurrentInterpretation().observe(this,
                interpretation -> ResultAnimationUtils.animateInterpretationText(
                        txtReadingContent, interpretation));

        viewModel.isCardInterpretation().observe(this, this::handleCardVisibility);
        viewModel.isLastInterpretation().observe(this, this::updateNavigationState);
        viewModel.isFirstInterpretation().observe(this, isFirst -> {
            btnPrevious.setVisibility(isFirst ? View.GONE : View.VISIBLE);
            btnPrevious.setEnabled(!isFirst);
        });

        // Error observer
        viewModel.getError().observe(this, error -> {
            if (error != null) showError(error);
            else hideError();
        });
    }

    /**
     * Handles the loading state by showing or hiding the loading message.
     *
     * @param isLoading Whether the app is currently loading.
     */
    private void handleLoadingState(boolean isLoading) {
        if (isLoading) {
            txtLoadingMessage.setVisibility(View.VISIBLE);
            txtLoadingMessage.setAlpha(1f);
            ResultAnimationUtils.createLoadingAnimation(txtLoadingMessage,
                    () -> {
                        if (viewModel.isLoading().getValue()) {
                            ResultAnimationUtils.createLoadingAnimation(
                                    txtLoadingMessage, null).start();
                        }
                    }).start();
        } else {
            ResultAnimationUtils.fadeOutView(txtLoadingMessage);
        }
    }

    /**
     * Handles the visibility of the selected tarot card image.
     *
     * @param showCard Whether to show the card image.
     */
    private void handleCardVisibility(boolean showCard) {
        if (showCard) {
            int cardIndex = viewModel.getCurrentIndex() - 1;
            if (cardImages != null && cardIndex >= 0 && cardIndex < cardImages.size()) {
                Bitmap cardBitmap = cardImages.get(cardIndex);
                if (cardBitmap != null && !cardBitmap.isRecycled()) {
                    imgSelectedCard.setImageBitmap(cardBitmap);
                    ResultAnimationUtils.fadeInView(imgSelectedCard, 0.35f);
                } else {
                    Log.e(TAG, "Card bitmap is null or recycled at index: " + cardIndex);
                }
            }
        } else {
            ResultAnimationUtils.fadeOutView(imgSelectedCard);
        }
    }

    /**
     * Updates the navigation button state based on whether the current interpretation is the last one.
     *
     * @param isLast Whether the current interpretation is the last one.
     */
    private void updateNavigationState(boolean isLast) {
        btnNext.setIcon(getDrawable(isLast ?
                R.drawable.ic_home_foreground :
                R.drawable.ic_right_foreground));
    }

    /**
     * Loads data from the intent, including the spread type, user question, and selected cards.
     */
    private void loadIntentData() {
        Intent intent = getIntent();
        spreadType = intent.getStringExtra("spreadType");
        userQuestion = intent.getStringExtra("question");
        selectedCards = intent.getStringArrayListExtra("cardPicked");

        if (selectedCards != null) {
            cardImages = new ArrayList<>();
            Log.d(TAG, "Loading card images");
            for (String card : selectedCards) {
                String cardInfo = card.split(":")[1].trim();
                String[] parts = cardInfo.split("-");
                String cardName = parts[0].trim();
                boolean isReversed = parts.length > 1;

                Bitmap cardBitmap = CardImageLoaderUtils.getCardBitmapFromAssets(this, cardName, isReversed);
                if (cardBitmap != null) {
                    cardImages.add(cardBitmap);
                    Log.d(TAG, cardImages.toString());
                } else {
                    Log.e(TAG, "Failed to load card image: " + cardName);
                }
            }
        }

        validateIntentData();
    }

    /**
     * Validates the data loaded from the intent.
     */
    private void validateIntentData() {
        if (spreadType == null || userQuestion == null ||
                selectedCards == null || cardImages == null) {
            Log.e(TAG, String.format("Missing data - spreadType: %s, question: %s, " +
                            "cards: %s, images: %s", spreadType, userQuestion,
                    selectedCards, cardImages != null));
        }
    }

    /**
     * Generates the tarot reading by sending a prompt to the OpenAI API.
     */
    private void generateReading() {
        viewModel.setLoading(true);

        String prompt = new TarotPromptBuilder(userQuestion, spreadType, selectedCards)
                .buildPrompt();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        OpenAIHelper openAIHelper = new OpenAIHelper(this, BuildConfig.OPENAI_API_KEY);

        openAIHelper.generateResponse(prompt, new OpenAIHelper.AIResponseListener() {
            @Override
            public void onSuccess(String response) {
                handleReadingResponse(response, userId);
            }

            @Override
            public void onFailure(String errorMessage) {
                handleReadingError(errorMessage);
            }
        });

        setupRequestTimeout();
    }

    /**
     * Handles the response from the OpenAI API.
     *
     * @param response The response from the API.
     * @param userId   The ID of the current user.
     */
    private void handleReadingResponse(String response, String userId) {
        viewModel.setLoading(false);

        if (!response.contains("##")) {
            viewModel.setError("Invalid response format");
            return;
        }

        boolean hasValidAccess = paymentManager.hasLifetimeAccess(userId) ||
                paymentManager.hasActiveSubscription(userId);

        if (!hasValidAccess) {
            handleFreeUserAccess(userId);
        }

        viewModel.setTarotResponse(response);
        updateSaveButton();
    }

    /**
     * Handles access for free users, including daily reading limits and credit consumption.
     *
     * @param userId The ID of the current user.
     */
    private void handleFreeUserAccess(String userId) {
        if (dailyReadingManager.canPerformDailyReading(userId) &&
                selectedCards.size() == 1) {
            dailyReadingManager.recordDailyReading(userId);
            paymentManager.addCredits(userId, 1);
            showToast("Reading credit + 1");
        } else {
            paymentManager.consumeCreditsForReading(userId);
        }
    }

    /**
     * Handles errors during the reading generation process.
     *
     * @param errorMessage The error message to display.
     */
    private void handleReadingError(String errorMessage) {
        viewModel.setLoading(false);
        viewModel.setError(errorMessage);
    }

    /**
     * Sets up a timeout for the reading generation request.
     */
    private void setupRequestTimeout() {
        new Handler().postDelayed(() -> {
            if (viewModel.isLoading().getValue()) {
                viewModel.setLoading(false);
                viewModel.setError("Request timed out. Please check your connection.");
            }
        }, REQUEST_TIMEOUT);
    }

    /**
     * Handles the "Next" button click, either navigating to the next interpretation or returning to the main activity.
     */
    private void handleNextClick() {
        if (viewModel.isLastInterpretation().getValue()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            viewModel.showNextInterpretation();
        }
    }

    /**
     * Handles the "Previous" button click, navigating to the previous interpretation.
     */
    private void handlePreviousClick() {
        viewModel.showPreviousInterpretation();
    }

    /**
     * Handles the "Retry" button click, retrying the reading generation process.
     */
    private void handleRetryClick() {
        viewModel.clearError();
        generateReading();
    }

    /**
     * Handles the "Share" button click, generating and sharing an image of the reading.
     */
    private void handleShareClick() {
//        if (!validateSharePrerequisites()) return;
//
//        try {
//            ShareImageGenerator generator = new ShareImageGenerator(
//                    this, spreadType, userQuestion, cardImages);
//
//            Bitmap shareBitmap = generator.createShareImage(containerContent);
//            if (shareBitmap != null) {
//                showShareDialog(shareBitmap);
//            } else {
//                showToast("Failed to create share image");
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Error creating share image", e);
//            showToast("Failed to create share image: " + e.getMessage());
//        }
    }

    /**
     * Validates prerequisites for sharing the reading.
     *
     * @return Whether the prerequisites are met.
     */
    private boolean validateSharePrerequisites() {
//        if (cardImages == null || cardImages.isEmpty()) {
//            Log.e(TAG, "No card images available");
//            showToast("No card images available");
//            return false;
//        }
//
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser == null) {
//            showToast("Please sign in to share");
//            return false;
//        }
//
//        return true;
        return true;
    }

    /**
     * Displays a dialog for sharing the reading image.
     *
     * @param shareBitmap The bitmap of the reading image to share.
     */
    private void showShareDialog(Bitmap shareBitmap) {
//        SharePreviewDialog dialog = SharePreviewDialog.newInstance(shareBitmap);
//        dialog.setDailyReadingManager(dailyReadingManager);
//
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (dailyReadingManager.canEarnShareCredit(currentUser.getUid())) {
//            showToast("Share to earn 1 credit!");
//        }
//
//        dialog.show(getSupportFragmentManager(), "share_preview");
    }



    /**
     * Handles the "Save" button click, saving or deleting the reading.
     */
    private void handleSaveClick() {
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser == null) {
//            showToast("Please sign in to save readings");
//            return;
//        }
//
//        if (!isReadingSaved) {
//            saveReading(currentUser);
//        } else {
//            deleteReading(currentUser);
//        }
    }

    /**
     * Saves the current reading to Firebase.
     *
     * @param user The current user.
     */
    private void saveReading(FirebaseUser user) {
//        Reading reading = new Reading();
//        reading.setUserId(user.getUid());
//        reading.setSpreadType(spreadType);
//        reading.setQuestion(userQuestion);
//        reading.setInterpretation(viewModel.getCompleteInterpretation());
//
//        List<SavedCard> savedCards = new ArrayList<>();
//        for (String cardInfo : selectedCards) {
//            SavedCard card = ReadingUtils.parseCardInfo(cardInfo);
//            savedCards.add(card);
//        }
//        reading.setCards(savedCards);
//
//        currentReadingId = reading.getId();
//
//        readingManager.saveReading(reading)
//                .addOnSuccessListener(aVoid -> {
//                    isReadingSaved = true;
//                    updateSaveButton();
//                    showToast("Reading saved");
//                })
//                .addOnFailureListener(e -> showToast("Failed to save reading"));
    }

    /**
     * Deletes the current reading from Firebase.
     *
     * @param user The current user.
     */
    private void deleteReading(FirebaseUser user) {
//        readingManager.deleteReading(user.getUid(), currentReadingId)
//                .addOnSuccessListener(aVoid -> {
//                    isReadingSaved = false;
//                    currentReadingId = null;
//                    updateSaveButton();
//                    showToast("Reading removed");
//                })
//                .addOnFailureListener(e -> showToast("Failed to remove reading"));
    }

    /**
     * Updates the save button icon based on whether the reading is saved.
     */
    private void updateSaveButton() {
        btnSave.setVisibility(View.VISIBLE);
        btnSave.setIcon(getDrawable(isReadingSaved ?
                R.drawable.ic_saved_foreground :
                R.drawable.ic_save_foreground));
    }

    /**
     * Displays an error message.
     *
     * @param message The error message to display.
     */
    private void showError(String message) {
        ResultAnimationUtils.fadeOutView(txtLoadingMessage);
        ResultAnimationUtils.fadeOutView(txtReadingContent);

        containerError.setVisibility(View.VISIBLE);
        containerError.setAlpha(0f);
        containerError.setTranslationY(50f);

        txtErrorMessage.setText(message);

        containerError.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(ResultAnimationUtils.FADE_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        setButtonsEnabled(false);
    }

    private void hideError() {
        containerError.setVisibility(View.GONE);
        txtReadingContent.setVisibility(View.VISIBLE);
        setButtonsEnabled(true);
    }

    private void setButtonsEnabled(boolean enabled) {
        btnNext.setEnabled(enabled);
        btnShare.setEnabled(enabled);
    }

    private void setupBackgroundMusic() {
        try {
            if (musicPlayer != null) {
                ResultAnimationUtils.stopAndReleaseMusic(musicPlayer);
            }

            musicPlayer = MediaPlayer.create(this, R.raw.reading_music);
            if (musicPlayer != null) {
                musicPlayer.setLooping(true);
                musicPlayer.setVolume(0.7f, 0.7f);
                musicPlayer.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up background music", e);
        }
    }

    private void showToast(String toastString) {
        Toast.makeText(this, toastString, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        View[] views = {txtReadingContent, btnShare, containerNavigation, containerContent};

        for (View view : views) {
            if (view == null) {
                Log.e(TAG, "One or more views in the array are null");
                return;
            }
        }

        AnimatorSet animator = ResultAnimationUtils.createExitAnimation(views);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        animator.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ResultAnimationUtils.stopAndReleaseMusic(musicPlayer);
    }

    public String buildShareText() {
        return String.format(
                "My Tarot Result\n\n" +
                        "Question: %s\n" +
                        "Get your own reading with Midnight Tarot AI App!",
                userQuestion);
    }
}


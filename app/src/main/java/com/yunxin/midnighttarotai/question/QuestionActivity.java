package com.yunxin.midnighttarotai.question;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yunxin.midnighttarotai.BuildConfig;
import com.yunxin.midnighttarotai.home.MainActivity;
import com.yunxin.midnighttarotai.R;
import com.yunxin.midnighttarotai.cardpicking.CardPickActivity;
import com.yunxin.midnighttarotai.utils.OpenAIHelper;

/**
 * Activity for handling user question input and spread type selection.
 * This activity allows users to input their questions and receive AI-suggested spread types.
 */
public class QuestionActivity extends AppCompatActivity {
    private static final String TAG = "QuestionActivity";

    /** UI Components */
    private EditText questionContent;
    private Button back;
    private Button ask;
    private FrameLayout buttonContainer;
    private Button popoutInstruction;
    private FrameLayout questionInstruction;
    private ImageView closeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_question);

        initializeViews();
        setupClickListeners();
        setupWindowInsets();
    }

    /**
     * Initializes all view components from layout.
     * Sets initial visibility states for UI elements.
     */
    private void initializeViews() {
        questionContent = findViewById(R.id.questioncontent);
        back = findViewById(R.id.back);
        ask = findViewById(R.id.ask);

        popoutInstruction = findViewById(R.id.popoutInstruction);
        questionInstruction = findViewById(R.id.questionInstruction);
        questionInstruction.setVisibility(View.GONE);
        closeIcon = findViewById(R.id.closeIcon);

        buttonContainer = findViewById(R.id.buttonContainer);
        buttonContainer.setVisibility(View.GONE);
    }

    /**
     * Sets up click listeners for all interactive UI elements.
     * Initializes OpenAI helper for spread suggestions.
     */
    private void setupClickListeners() {
        OpenAIHelper openAIHelper = new OpenAIHelper(this, BuildConfig.OPENAI_API_KEY);

        ask.setOnClickListener(view -> handleAskButton(openAIHelper));
        back.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        popoutInstruction.setOnClickListener(v -> showInstructionPopup());
        closeIcon.setOnClickListener(v -> questionInstruction.setVisibility(View.GONE));
    }

    /**
     * Handles the ask button click event.
     * Validates user input and requests spread suggestion from OpenAI.
     *
     * @param openAIHelper Helper instance for OpenAI API interactions
     */
    private void handleAskButton(OpenAIHelper openAIHelper) {
        String question = questionContent.getText().toString().trim();
        if (question.isEmpty()) {
            showError("Please enter a question");
            return;
        }

        String prompt = PromptManager.getSpreadSelectionPrompt(question);
        openAIHelper.generateResponse(prompt, new OpenAIHelper.AIResponseListener() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> createProceedButton(response));
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() -> showError("Error: " + errorMessage));
            }
        });
    }

    /**
     * Displays the instruction popup with animation.
     */
    private void showInstructionPopup() {
        questionInstruction.setVisibility(View.VISIBLE);
        questionInstruction.setScaleX(0f);
        questionInstruction.setScaleY(0f);
        questionInstruction.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start();
    }

    /**
     * Creates and displays the proceed button based on AI response.
     *
     * @param response The spread type response from OpenAI
     */
    private void createProceedButton(String response) {
        try {
            String spreadType = formatSpreadType(response);
            if (spreadType == null) {
                return;
            }

            View popoutButtonLayout = findViewById(R.id.popoutButtonLayout);
            if (popoutButtonLayout == null) {
                showError("Layout error: popoutButtonLayout not found");
                return;
            }

            setupButtonLayout(popoutButtonLayout, spreadType);
            animateButtonAppearance(popoutButtonLayout);

        } catch (Exception e) {
            Log.e(TAG, "Error in createProceedButton: " + e.getMessage());
            showError("Error creating button: " + e.getMessage());
        }
    }

    /**
     * Formats the spread type response from OpenAI.
     *
     * @param response Raw response string from OpenAI
     * @return Formatted spread type string or null if invalid
     */
    private String formatSpreadType(String response) {
        String spreadType = response.trim()
                .replaceAll("\n", "")
                .replaceAll("\\s+", "")
                .toLowerCase();

        if (spreadType.isEmpty()) {
            showError("Invalid spread type received");
            return null;
        }

        switch (spreadType) {
            case "timeline": return "Timeline";
            case "hexagram": return "Hexagram";
            case "two_options": return "Two Options";
            case "cross": return "Cross";
            case "pastlove": return "PastLove";
            case "onecard": return "One Card";
            case "horseshoe": return "Horseshoe";
//            case "companion": return "Companion";
            default:
                showError("Unknown spread type: " + spreadType);
                return null;
        }
    }

    /**
     * Sets up the button layout with proper styling and click listener.
     *
     * @param popoutButtonLayout The layout containing the button
     * @param spreadType The formatted spread type string
     */
    private void setupButtonLayout(View popoutButtonLayout, String spreadType) {
        Button layoutButton = popoutButtonLayout.findViewById(R.id.layout);
        TextView layoutText = popoutButtonLayout.findViewById(R.id.layouttext);

        if (layoutButton == null || layoutText == null) {
            showError("Layout error: Button or text view is null");
            return;
        }

        buttonContainer.setVisibility(View.VISIBLE);
        popoutButtonLayout.setVisibility(View.VISIBLE);
        layoutText.setText("- " + spreadType + " -");

        Typeface customTypeface = ResourcesCompat.getFont(this, R.font.arizonia);
        layoutText.setTypeface(customTypeface);
        layoutText.setTextColor(getResources().getColor(R.color.text_primary));
        layoutText.setTextSize(30);

        layoutButton.setOnClickListener(view -> animateButtonClick(view, spreadType));
    }

    /**
     * Animates the button click with scale effect.
     *
     * @param view The button view being clicked
     * @param spreadType The selected spread type
     */
    private void animateButtonClick(View view, String spreadType) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .withEndAction(() -> startCardPickActivity(spreadType))
                            .start();
                })
                .start();
    }

    /**
     * Animates the button appearance with scale effect.
     *
     * @param popoutButtonLayout The layout to animate
     */
    private void animateButtonAppearance(View popoutButtonLayout) {
        popoutButtonLayout.setAlpha(1f);
        popoutButtonLayout.setScaleX(0f);
        popoutButtonLayout.setScaleY(0f);
        popoutButtonLayout.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .withStartAction(() -> popoutButtonLayout.setVisibility(View.VISIBLE))
                .start();
    }

    /**
     * Starts the CardPickActivity with selected spread type and question.
     *
     * @param spreadType The selected spread type
     */
    private void startCardPickActivity(String spreadType) {
        try {
            String question = questionContent.getText().toString().trim();
            if (question.isEmpty()) {
                showError("Please enter a question");
                return;
            }

            Intent intent = new Intent(this, CardPickActivity.class);
            intent.putExtra("spreadType", spreadType);
            intent.putExtra("question", question);
            intent.putExtra("pick", getPickCount(spreadType));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        } catch (Exception e) {
            Log.e(TAG, "Error starting CardPickActivity: " + e.getMessage());
            showError("Failed to start card selection: " + e.getMessage());
        }
    }

    /**
     * Gets the number of cards to pick based on spread type.
     *
     * @param spreadType The selected spread type
     * @return Number of cards to pick
     */
    private int getPickCount(String spreadType) {
        switch (spreadType.toLowerCase()) {
            case "cross":
            case "two options":
            case "horseshoe":
                return 5;
            case "hexagram":
                return 6;
            case "pastlove":
                return 4;
            case "timeline":
                return 3;
            case "companion":
                return 7;
            case "one card":
                return 1;
            default:
                return 3;
        }
    }

    /**
     * Sets up window insets for proper system UI handling.
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Shows an error message to the user.
     *
     * @param message The error message to display
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
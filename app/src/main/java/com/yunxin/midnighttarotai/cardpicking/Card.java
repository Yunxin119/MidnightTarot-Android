package com.yunxin.midnighttarotai.cardpicking;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import java.util.Random;

/**
 * Represents a Tarot card with front and back images, and handles card animations.
 * This class manages the state and behavior of individual tarot cards, including:
 * - Card images (front and back)
 * - Card orientation (normal or reversed)
 * - Flip animations with proper state management
 * - Card information (name and description)
 */
public class Card {
    private Bitmap frontImage;
    private Bitmap backImage;
    private final String name;
    private final String description;

    // State flags
    private boolean isFaceUp = false;
    private boolean isReversed = false;
    private boolean isAnimating = false;
    private boolean isFlipped = false;
    private ImageView currentImageView = null;

    /**
     * Constructs a new Card instance with the specified attributes.
     *
     * @param frontImage   The front face bitmap of the card
     * @param backImage    The back face bitmap of the card
     * @param name        The name of the card
     * @param description The description or meaning of the card
     */
    public Card(Bitmap frontImage, Bitmap backImage, String name, String description) {
        this.frontImage = frontImage;
        this.backImage = backImage;
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the current visible face of the card.
     *
     * @return Bitmap of either the front or back image depending on card state
     */
    public Bitmap getImage() {
        return isFaceUp ? frontImage : backImage;
    }

    /**
     * Executes a card flip animation with proper state management and placement handling.
     * This method ensures that:
     * - Only one animation can occur at a time
     * - The card is properly placed before animation starts
     * - The card's state is correctly maintained throughout the animation
     *
     * @param cardView    The ImageView where the card will be displayed
     * @param onComplete  Callback to be executed when the animation completes
     */
    public void flipWithAnimation(ImageView cardView, Runnable onComplete) {
        // Prevent multiple animations and check if card is already in final state
        if (isAnimating || isFlipped) {
            return;
        }

        // Set initial state and store reference to current ImageView
        isAnimating = true;
        currentImageView = cardView;

        // Set initial card image before animation
        cardView.setImageBitmap(backImage);

        // Configure animation parameters
        float start = 0f;
        float end = 180f;
        cardView.setCameraDistance(8000);

        // Create flip animation phases
        ObjectAnimator firstHalf = ObjectAnimator.ofFloat(cardView, "rotationY", start, end/2)
                .setDuration(200);
        ObjectAnimator secondHalf = ObjectAnimator.ofFloat(cardView, "rotationY", -end/2, 0f)
                .setDuration(200);

        // Set interpolators for smooth animation
        firstHalf.setInterpolator(new AccelerateInterpolator());
        secondHalf.setInterpolator(new DecelerateInterpolator());

        // Handle mid-flip card face change
        firstHalf.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (currentImageView == cardView) {  // Verify this is still the active animation
                    determineCardOrientation();
                    isFaceUp = true;
                    cardView.setImageBitmap(frontImage);
                }
            }
        });

        // Handle animation completion
        secondHalf.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (currentImageView == cardView) {  // Verify this is still the active animation
                    isAnimating = false;
                    isFlipped = true;
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                resetCardState();
            }
        });

        // Combine animation phases
        AnimatorSet flipAnimation = new AnimatorSet();
        flipAnimation.playSequentially(firstHalf, secondHalf);

        // Handle animation cancellation
        flipAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                resetCardState();
            }
        });

        flipAnimation.start();
    }

    /**
     * Determines if the card should be reversed and rotates the image if necessary.
     */
    private void determineCardOrientation() {
        Random random = new Random();
        isReversed = random.nextBoolean();
        if (isReversed) {
            frontImage = getRotatedBitmap(frontImage);
        } else {
            frontImage = Bitmap.createBitmap(frontImage);
        }
    }

    /**
     * Resets the card to its initial state.
     */
    private void resetCardState() {
        isAnimating = false;
        isFlipped = false;
        isFaceUp = false;
        currentImageView = null;
    }

    /**
     * Rotates a bitmap 180 degrees.
     *
     * @param bitmap The bitmap to rotate
     * @return A new rotated bitmap
     */
    private Bitmap getRotatedBitmap(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(180);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    // Getters
    public boolean isFlipped() {
        return isFlipped;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean getIsReversed() {
        return isReversed;
    }

    public Bitmap getImageResourceId() {
        return frontImage;
    }
}
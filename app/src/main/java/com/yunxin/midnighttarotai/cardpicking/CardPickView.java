package com.yunxin.midnighttarotai.cardpicking;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;

import com.yunxin.midnighttarotai.utils.CardAnimationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Custom view for displaying and handling tarot card selection.
 * Provides an interactive fan layout of cards with animations and gestures.
 */
public class CardPickView extends View {
    // Constants
    private static final float ROTATION_SENSITIVITY = 0.2f;
    private static final float DEFAULT_FAN_RADIUS = 1400f;
    private static final float DEFAULT_MAX_ROTATION = 3.0f;
    private static final float DEFAULT_MAX_ELEVATION = 20.0f;
    private static final float DEFAULT_HOVER_SCALE = 1.1f;

    // Card Management
    private List<Card> mCardList;
    private List<Card> mSelectedCards = new ArrayList<>();
    private int mPickCardLimit;

    // Layout Parameters
    private float mCurrentRotation = 0f;
    private float mFanRadius = DEFAULT_FAN_RADIUS;
    private float mFanAngle = 100f;
    private float mCenterX, mCenterY;
    private float mMinRotation = -130f;
    private float mMaxRotation = 130f;
    private float mLastTouchX;

    // Visual Effects
    private final Paint mPaint;
    private final List<RectF> mCardBounds = new ArrayList<>();
    private float[] mCardRotations;
    private float[] mCardElevations;
    private int mHoveredCardIndex = -1;

    // State Management
    private boolean mIsInitialAnimationComplete = false;
    private boolean mIsExiting = false;
    private float mExitProgress = 0f;

    // Animation
    private ValueAnimator mSpreadAnimator;
    private ValueAnimator mExitAnimator;
    private final Random mRandom = new Random();
    private final GestureDetector mGestureDetector;

    /**
     * Interface for card selection callbacks
     */
    public interface OnCardSelectedListener {
        void onCardSelected(Card card);
    }

    private OnCardSelectedListener mOnCardSelectedListener;

    public CardPickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = initializePaint();
        mGestureDetector = new GestureDetector(context, new CardGestureListener());
        postDelayed(this::startFanOutAnimation, 300);
    }

    /**
     * Initializes the paint object with proper settings
     */
    private Paint initializePaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShadowLayer(12, 0, 6, 0x60000000);
        return paint;
    }

    /**
     * Initializes card effects and loads the deck
     */
    public void initialize(int pickCardLimit) {
        mPickCardLimit = pickCardLimit;
        CardPick cardPick = new CardPick(getContext());
        mCardList = cardPick.getCardList();
        initializeCardEffects();
    }

    /**
     * Initializes card visual effects
     */
    private void initializeCardEffects() {
        mCardRotations = new float[mCardList.size()];
        mCardElevations = new float[mCardList.size()];

        for (int i = 0; i < mCardList.size(); i++) {
            mCardRotations[i] = (mRandom.nextFloat() - 0.5f) * DEFAULT_MAX_ROTATION;
            mCardElevations[i] = mRandom.nextFloat() * (DEFAULT_MAX_ELEVATION / 2);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2f;
        mCenterY = h + mFanRadius * 0.7f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCardList == null || mCardList.isEmpty()) return;

        drawCards(canvas);
    }

    /**
     * Draws all cards in the fan layout
     */
    private void drawCards(Canvas canvas) {
        mCardBounds.clear();
        float angleStep = mFanAngle / (mCardList.size() - 1);
        float startAngle = -mFanAngle / 2f + mCurrentRotation;

        for (int i = 0; i < mCardList.size(); i++) {
            drawCard(canvas, i, startAngle + (i * angleStep));
        }
    }

    /**
     * Draws a single card with proper rotation and position
     */
    private void drawCard(Canvas canvas, int index, float angle) {
        Card card = mCardList.get(index);
        Bitmap cardBitmap = card.getImage();

        float radians = (float) Math.toRadians(angle);
        float cardCenterX = mCenterX + (float) (mFanRadius * Math.sin(radians));
        float cardCenterY = mCenterY - (float) (mFanRadius * Math.cos(radians));

        if (mIsExiting) {
            cardCenterY += getHeight() * mExitProgress;
        }

        canvas.save();
        setupCardTransform(canvas, cardCenterX, cardCenterY, angle, index, cardBitmap);
        canvas.drawBitmap(cardBitmap, 0, 0, mPaint);
        canvas.restore();
    }

    /**
     * Sets up the transform matrix for card drawing
     */
    private void setupCardTransform(Canvas canvas, float centerX, float centerY,
                                    float angle, int index, Bitmap bitmap) {
        canvas.translate(centerX, centerY);
        canvas.rotate(angle + mCardRotations[index]);

        float baseScale = 0.88f;
        canvas.scale(baseScale, baseScale);

        float cardWidth = bitmap.getWidth();
        float cardHeight = bitmap.getHeight();
        canvas.translate(-cardWidth / 2f, -cardHeight / 2f);

        mPaint.setShadowLayer(12, 0, mCardElevations[index] * 0.5f, 0x60000000);

        RectF cardRect = new RectF(0, 0, cardWidth, cardHeight);
        canvas.getMatrix().mapRect(cardRect);
        mCardBounds.add(cardRect);
    }

    /**
     * Gesture listener for handling card interactions
     */
    private class CardGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!mIsInitialAnimationComplete) return false;

            float rotationDelta = (e2.getX() - mLastTouchX) * ROTATION_SENSITIVITY;
            float newRotation = mCurrentRotation + rotationDelta;

            if (newRotation >= mMinRotation && newRotation <= mMaxRotation) {
                mCurrentRotation = newRotation;
                invalidate();
            }

            mLastTouchX = e2.getX();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mIsInitialAnimationComplete) {
                handleCardSelection(e.getX(), e.getY());
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            updateHoveredCard(e.getX(), e.getY());
            return true;
        }
    }

    /**
     * Starts the fan-out animation for the cards
     */
    private void startFanOutAnimation() {
        mSpreadAnimator = ValueAnimator.ofFloat(0f, 1f);
        mSpreadAnimator.setDuration(1500);
        mSpreadAnimator.setInterpolator(new DecelerateInterpolator(1.5f));

        mSpreadAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            mFanAngle = 260f * progress;
            invalidate();
        });

        mSpreadAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                mIsInitialAnimationComplete = true;
                animateCardSettle();
            }
        });

        mSpreadAnimator.start();
    }

    /**
     * Animates cards settling into their final positions
     */
    private void animateCardSettle() {
        for (int i = 0; i < mCardList.size(); i++) {
            float targetRotation = (mRandom.nextFloat() - 0.5f) * DEFAULT_MAX_ROTATION;
            ValueAnimator rotationAnimator = ValueAnimator.ofFloat(mCardRotations[i], targetRotation);
            rotationAnimator.setDuration(300 + mRandom.nextInt(200));
            rotationAnimator.setInterpolator(new DecelerateInterpolator());

            final int index = i;
            rotationAnimator.addUpdateListener(animation -> {
                mCardRotations[index] = (float) animation.getAnimatedValue();
                invalidate();
            });
            rotationAnimator.start();
        }
    }

    /**
     * Updates the currently hovered card and triggers hover animations
     */
    private void updateHoveredCard(float x, float y) {
        int oldHoveredIndex = mHoveredCardIndex;
        mHoveredCardIndex = -1;

        for (int i = mCardBounds.size() - 1; i >= 0; i--) {
            if (mCardBounds.get(i).contains(x, y)) {
                mHoveredCardIndex = i;
                break;
            }
        }

        if (oldHoveredIndex != mHoveredCardIndex) {
            animateCardHover(oldHoveredIndex, mHoveredCardIndex);
        }
    }

    /**
     * Handles the hover animation for cards
     */
    private void animateCardHover(int oldIndex, int newIndex) {
        if (oldIndex >= 0 && oldIndex < mCardList.size()) {
            mCardElevations[oldIndex] = mRandom.nextFloat() * (DEFAULT_MAX_ELEVATION / 2);
        }

        if (newIndex >= 0 && newIndex < mCardList.size()) {
            mCardElevations[newIndex] = DEFAULT_MAX_ELEVATION;
        }
        invalidate();
    }

    /**
     * Handles card selection logic
     */
    private void handleCardSelection(float touchX, float touchY) {
        if (isCardSelectionComplete()) return;

        for (int i = mCardBounds.size() - 1; i >= 0; i--) {
            RectF cardRect = mCardBounds.get(i);
            if (cardRect.contains(touchX, touchY)) {
                selectCard(i);
                break;
            }
        }
    }

    /**
     * Processes the selection of a specific card
     */
    private void selectCard(int index) {
        Card selectedCard = mCardList.get(index);
        mCardList.remove(index);
        mSelectedCards.add(selectedCard);

        if (mOnCardSelectedListener != null) {
            mOnCardSelectedListener.onCardSelected(selectedCard);
        }

        if (isCardSelectionComplete()) {
            startExitAnimation();
        }

        invalidate();
    }

    /**
     * Starts the exit animation when card selection is complete
     */
    private void startExitAnimation() {
        mIsExiting = true;
        mExitAnimator = ValueAnimator.ofFloat(0f, 1f);
        mExitAnimator.setDuration(1000);
        mExitAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        mExitAnimator.addUpdateListener(animation -> {
            mExitProgress = (float) animation.getAnimatedValue();
            invalidate();
        });

        mExitAnimator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mLastTouchX = event.getX();
        }
        return mGestureDetector.onTouchEvent(event);
    }

    /**
     * Checks if the required number of cards have been selected
     */
    public boolean isCardSelectionComplete() {
        return mSelectedCards.size() >= mPickCardLimit;
    }

    /**
     * Sets the card selection listener
     */
    public void setOnCardSelectedListener(OnCardSelectedListener listener) {
        mOnCardSelectedListener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cleanupAnimations();
    }

    /**
     * Cleans up any running animations
     */
    private void cleanupAnimations() {
        if (mSpreadAnimator != null) {
            mSpreadAnimator.cancel();
            mSpreadAnimator = null;
        }
        if (mExitAnimator != null) {
            mExitAnimator.cancel();
            mExitAnimator = null;
        }
    }
}

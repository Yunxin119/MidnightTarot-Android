package com.yunxin.midnighttarotai.tutorial;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunxin.midnighttarotai.R;
import com.yunxin.midnighttarotai.settings.SettingsManager;
import com.yunxin.midnighttarotai.utils.TutorialAnimationUtils;

/**
 * Custom view that displays an interactive tutorial overlay for tarot card reading.
 * Manages step-by-step tutorial animations and user guidance.
 */
public class TutorialOverlay extends FrameLayout {
    private static final float MUSIC_VOLUME = 0.7f;

    // UI Components
    private ImageView mHandView;
    private TextView mMessageView;
    private Button mGotItButton;
    private ImageView mCardView;
    private View mGlowEffect;

    // State Management
    private int mCurrentStep = 0;
    private final SettingsManager mSettingsManager;

    // Animation Controllers
    private android.animation.ValueAnimator mCurrentAnimation;
    private MediaPlayer mMediaPlayer;

    public TutorialOverlay(Context context) {
        super(context);
        mSettingsManager = new SettingsManager(context);
        initializeView(context);
    }

    public TutorialOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSettingsManager = new SettingsManager(context);
        initializeView(context);
    }

    private void initializeView(Context context) {
        Log.d("Tutorial", "Tutorial Overlay Loading");
        inflate(context, R.layout.activity_tutorial_overlay, this);
        findViews();
        setupInitialState();
        initializeMusic(context);
        setupClickListeners();
        showCurrentStep();
    }

    private void findViews() {
        mHandView = findViewById(R.id.handView);
        mMessageView = findViewById(R.id.messageView);
        mGotItButton = findViewById(R.id.gotItButton);
        mCardView = findViewById(R.id.cardView);
        mGlowEffect = findViewById(R.id.glowEffect);
    }

    private void setupInitialState() {
        mMessageView.setAlpha(0f);
        mGlowEffect.setAlpha(0f);
    }

    private void setupClickListeners() {
        mGotItButton.setOnClickListener(v -> dismissTutorial());
        setOnClickListener(v -> {
            if (mCurrentStep < TutorialStep.values().length - 1) {
                mCurrentStep++;
                showCurrentStep();
            }
        });
    }

    private void initializeMusic(Context context) {
        if (mSettingsManager.isSoundEnabled()) {
            try {
                mMediaPlayer = MediaPlayer.create(context, R.raw.meditation_music);
                if (mMediaPlayer != null) {
                    mMediaPlayer.setLooping(true);
                    mMediaPlayer.setVolume(MUSIC_VOLUME, MUSIC_VOLUME);
                    mMediaPlayer.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showCurrentStep() {
        cancelCurrentAnimation();
        resetViews();

        TutorialStep step = TutorialStep.values()[mCurrentStep];
        setupStepContent(step);
        startStepAnimation(step);
    }

    private void setupStepContent(TutorialStep step) {
        mMessageView.setText(step.getMessageResId());
        setupViewsForStep(step);
        mCurrentAnimation = TutorialAnimationUtils.createMessageAnimation(mMessageView);
        mCurrentAnimation.start();
    }

    private void setupViewsForStep(TutorialStep step) {
        mHandView.setVisibility(step.showHand() ? VISIBLE : GONE);
        mCardView.setVisibility(step.showCard() ? VISIBLE : GONE);
        mGotItButton.setVisibility(step.showGotIt() ? VISIBLE : GONE);
        mGlowEffect.setVisibility(step.showGlow() ? VISIBLE : GONE);
    }

    private void startStepAnimation(TutorialStep step) {
        switch (step) {
            case MEDITATION:
                mCurrentAnimation = TutorialAnimationUtils.createBreathingAnimation(mMessageView);
                TutorialAnimationUtils.createGlowAnimation(mGlowEffect).start();
                break;
            case SWIPE:
                mCurrentAnimation = TutorialAnimationUtils.createSwipeAnimation(mHandView);
                break;
            case SELECT:
                mCurrentAnimation = TutorialAnimationUtils.createSelectionAnimation(mHandView);
                break;
        }
        if (mCurrentAnimation != null) {
            mCurrentAnimation.start();
        }
    }

    private void cancelCurrentAnimation() {
        if (mCurrentAnimation != null) {
            mCurrentAnimation.cancel();
            mCurrentAnimation = null;
        }
    }

    private void resetViews() {
        mMessageView.setAlpha(0f);
        mMessageView.setScaleX(1f);
        mMessageView.setScaleY(1f);
        mGlowEffect.setAlpha(0f);
    }

    private void dismissTutorial() {
        fadeOutMusic();
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
    }

    private void fadeOutMusic() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            TutorialAnimationUtils.createMusicFadeOutAnimation(
                    MUSIC_VOLUME,
                    volume -> {
                        if (mMediaPlayer != null) {
                            mMediaPlayer.setVolume(volume, volume);
                        }
                    },
                    this::stopAndReleaseMusic
            ).start();
        } else {
            stopAndReleaseMusic();
        }
    }

    private void stopAndReleaseMusic() {
        if (mMediaPlayer != null) {
            try {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mMediaPlayer = null;
            }
        }
    }

    public void updateMusicState() {
        if (mSettingsManager.isSoundEnabled()) {
            if (mMediaPlayer == null) {
                initializeMusic(getContext());
            } else if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
            }
        } else {
            stopAndReleaseMusic();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAndReleaseMusic();
        cancelCurrentAnimation();
    }

    /**
     * Enum defining the tutorial steps and their properties
     */
    private enum TutorialStep {
        MEDITATION(R.string.tutorial_meditation, false, false, true, false),
        SWIPE(R.string.tutorial_swipe, true, false, false, false),
        SELECT(R.string.tutorial_select, true, false, false, true);

        private final int messageResId;
        private final boolean showHand;
        private final boolean showCard;
        private final boolean showGlow;
        private final boolean showGotIt;

        TutorialStep(int messageResId, boolean showHand, boolean showCard,
                     boolean showGlow, boolean showGotIt) {
            this.messageResId = messageResId;
            this.showHand = showHand;
            this.showCard = showCard;
            this.showGlow = showGlow;
            this.showGotIt = showGotIt;
        }

        public int getMessageResId() { return messageResId; }
        public boolean showHand() { return showHand; }
        public boolean showCard() { return showCard; }
        public boolean showGlow() { return showGlow; }
        public boolean showGotIt() { return showGotIt; }
    }
}
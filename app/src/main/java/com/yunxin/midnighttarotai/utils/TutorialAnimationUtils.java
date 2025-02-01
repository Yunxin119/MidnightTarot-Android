package com.yunxin.midnighttarotai.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

/**
 * Utility class for managing tutorial animations.
 * Provides reusable animations for tutorial overlay components.
 */
public class TutorialAnimationUtils {
    private static final float BREATHING_MIN_SCALE = 0.85f;
    private static final float BREATHING_MAX_SCALE = 1f;
    private static final int BREATHING_DURATION = 3500;
    private static final int FADE_DURATION = 2000;
    private static final int SWIPE_DURATION = 2000;
    private static final float SWIPE_DISTANCE = 300f;
    private static final float HOVER_DISTANCE = -30f;

    /**
     * Creates a breathing animation for meditation step
     */
    public static ValueAnimator createBreathingAnimation(TextView messageView) {
        ValueAnimator animator = ValueAnimator.ofFloat(BREATHING_MIN_SCALE, BREATHING_MAX_SCALE);
        animator.setDuration(BREATHING_DURATION);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            messageView.setScaleX(scale);
            messageView.setScaleY(scale);
        });

        return animator;
    }

    /**
     * Creates a glow effect animation
     */
    public static ValueAnimator createGlowAnimation(View glowView) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 0.8f);
        animator.setDuration(FADE_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation ->
                glowView.setAlpha((Float) animation.getAnimatedValue())
        );

        return animator;
    }

    /**
     * Creates a swipe gesture animation
     */
    public static ValueAnimator createSwipeAnimation(View handView) {
        handView.setAlpha(1f);
        handView.setTranslationX(0f);

        ValueAnimator animator = ValueAnimator.ofFloat(0f, SWIPE_DISTANCE);
        animator.setDuration(SWIPE_DURATION);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation ->
                handView.setTranslationX((Float) animation.getAnimatedValue())
        );

        return animator;
    }

    /**
     * Creates a selection hover animation
     */
    public static ValueAnimator createSelectionAnimation(View handView) {
        handView.setTranslationX(0f);
        handView.setTranslationY(0f);

        ValueAnimator animator = ValueAnimator.ofFloat(0f, HOVER_DISTANCE);
        animator.setDuration(SWIPE_DURATION);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation ->
                handView.setTranslationY((Float) animation.getAnimatedValue())
        );

        return animator;
    }

    /**
     * Creates a message fade-in animation
     */
    public static ValueAnimator createMessageAnimation(TextView messageView) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(FADE_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            messageView.setAlpha(alpha);
            messageView.setTranslationY(30 * (1 - alpha));
        });

        return animator;
    }

    /**
     * Creates a music fade-out animation
     */
    public static ValueAnimator createMusicFadeOutAnimation(
            float startVolume,
            MusicVolumeListener volumeListener,
            Runnable onComplete
    ) {
        ValueAnimator animator = ValueAnimator.ofFloat(startVolume, 0f);
        animator.setDuration(1000);

        animator.addUpdateListener(animation -> {
            float volume = (float) animation.getAnimatedValue();
            volumeListener.onVolumeChanged(volume);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onComplete.run();
            }
        });

        return animator;
    }

    /**
     * Interface for music volume changes
     */
    public interface MusicVolumeListener {
        void onVolumeChanged(float volume);
    }
}
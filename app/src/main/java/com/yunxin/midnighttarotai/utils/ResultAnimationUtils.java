package com.yunxin.midnighttarotai.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling animations in the Tarot reading app.
 */
public class ResultAnimationUtils {
    public static final long FADE_DURATION = 800;
    public static final long SLIDE_DURATION = 600;
    public static final long SCALE_DURATION = 300;

    /**
     * Creates entrance animations for navigation buttons and sharing options.
     */
    public static void startEntranceAnimations(View shareButton, View navigationLayout) {
        shareButton.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(SLIDE_DURATION)
                .setInterpolator(new OvershootInterpolator())
                .start();

        navigationLayout.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(SLIDE_DURATION)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    /**
     * Creates a loading animation for the waiting text.
     */
    public static AnimatorSet createLoadingAnimation(TextView pleaseWaitText, Runnable onAnimationEnd) {
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(
                pleaseWaitText, "alpha", 0f, 1f);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(
                pleaseWaitText, "alpha", 1f, 0f);

        fadeIn.setDuration(1500);
        fadeOut.setDuration(1500);

        AnimatorSet set = new AnimatorSet();
        set.play(fadeIn).before(fadeOut);
        set.setStartDelay(500);

        if (onAnimationEnd != null) {
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onAnimationEnd.run();
                }
            });
        }

        return set;
    }

    /**
     * Creates a magical spinning animation effect.
     */
    public static void startSpinningAnimation(ImageView turningMagical) {
        // Rotation animation
        ValueAnimator spinningAnimator = ValueAnimator.ofFloat(0f, 360f);
        spinningAnimator.setDuration(6000);
        spinningAnimator.setRepeatCount(ValueAnimator.INFINITE);
        spinningAnimator.setInterpolator(new LinearInterpolator());
        spinningAnimator.addUpdateListener(animation -> {
            float rotation = (float) animation.getAnimatedValue();
            turningMagical.setRotation(rotation);
        });

        // Glow animation
        ValueAnimator glowingAnimator = ValueAnimator.ofFloat(0f, 0.3f);
        glowingAnimator.setDuration(3000);
        glowingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowingAnimator.setRepeatMode(ValueAnimator.REVERSE);
        glowingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        glowingAnimator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            turningMagical.setAlpha(alpha);
        });

        spinningAnimator.start();
        glowingAnimator.start();
    }

    /**
     * Creates fade animation for interpretation text.
     */
    public static void animateInterpretationText(TextView answerText, String newText) {
        if (answerText.getAlpha() > 0) {
            answerText.animate()
                    .alpha(0f)
                    .setDuration(FADE_DURATION / 2)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> {
                        answerText.setText(newText);
                        answerText.animate()
                                .alpha(1f)
                                .setDuration(FADE_DURATION / 2)
                                .setInterpolator(new DecelerateInterpolator())
                                .start();
                    }).start();
        } else {
            answerText.setText(newText);
            answerText.animate()
                    .alpha(1f)
                    .setDuration(FADE_DURATION)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    /**
     * Creates exit animations for multiple views.
     */
    public static AnimatorSet createExitAnimation(View[] views) {
        if (views == null || views.length == 0) {
            throw new IllegalArgumentException("Views array cannot be null or empty");
        }

        AnimatorSet exitAnimation = new AnimatorSet();
        List<Animator> animations = new ArrayList<>();

        for (View view : views) {
            if (view != null) {
                ObjectAnimator fade = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
                fade.setDuration(FADE_DURATION);
                animations.add(fade);
            }
        }

        exitAnimation.playTogether(animations);
        return exitAnimation;
    }

    /**
     * Applies scale animation for button touch feedback.
     */
    public static void applyButtonTouchAnimation(View button) {
        if (button == null) return;

        button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() ->
                        button.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start()
                )
                .start();
    }

    /**
     * Fades in a view with custom alpha value.
     */
    public static void fadeInView(View view, float targetAlpha) {
        if (view == null) return;

        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.animate()
                .alpha(targetAlpha)
                .setDuration(FADE_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Fades out a view and hides it.
     */
    public static void fadeOutView(View view) {
        if (view == null) return;

        view.animate()
                .alpha(0f)
                .setDuration(FADE_DURATION)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    /**
     * Slides in a view from the bottom.
     */
    public static void slideInFromBottom(View view) {
        if (view == null) return;

        view.setVisibility(View.VISIBLE);
        view.setTranslationY(view.getHeight());
        view.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(SLIDE_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Slides out a view to the bottom.
     */
    public static void slideOutToBottom(View view) {
        if (view == null) return;

        view.animate()
                .translationY(view.getHeight())
                .alpha(0f)
                .setDuration(SLIDE_DURATION)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }

    /**
     * Creates a pulse animation for views.
     */
    public static void pulseAnimation(View view) {
        if (view == null) return;

        AnimatorSet pulseAnim = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);

        scaleX.setDuration(SCALE_DURATION);
        scaleY.setDuration(SCALE_DURATION);

        pulseAnim.playTogether(scaleX, scaleY);
        pulseAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnim.start();
    }

    /**
     * Creates a bounce animation for views.
     */
    public static void bounceAnimation(View view) {
        if (view == null) return;

        view.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(SCALE_DURATION / 2)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1.1f)
                                .scaleY(1.1f)
                                .setInterpolator(new OvershootInterpolator())
                                .setDuration(SCALE_DURATION)
                                .withEndAction(() ->
                                        view.animate()
                                                .scaleX(1f)
                                                .scaleY(1f)
                                                .setDuration(SCALE_DURATION / 2)
                                                .start()
                                )
                                .start()
                )
                .start();
    }

    /**
     * Crossfades between two views.
     */
    public static void crossFade(View oldView, View newView) {
        if (oldView == null || newView == null) return;

        newView.setAlpha(0f);
        newView.setVisibility(View.VISIBLE);

        newView.animate()
                .alpha(1f)
                .setDuration(FADE_DURATION)
                .setInterpolator(new DecelerateInterpolator());

        oldView.animate()
                .alpha(0f)
                .setDuration(FADE_DURATION)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> oldView.setVisibility(View.GONE))
                .start();
    }

    public static void stopAndReleaseMusic(MediaPlayer musicPlayer) {
        if (musicPlayer != null) {
            try {
                if (musicPlayer.isPlaying()) {
                    musicPlayer.stop();
                }
                musicPlayer.release();
            } catch (Exception e) {
                Log.e("Result Animation Error", "Error releasing media player", e);
            } finally {
                musicPlayer = null;
            }
        }
    }
}
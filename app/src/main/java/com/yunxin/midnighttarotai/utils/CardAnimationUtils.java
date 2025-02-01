package com.yunxin.midnighttarotai.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

/**
 * Utility class for handling card animations.
 * Provides reusable animation methods for card interactions.
 */
public class CardAnimationUtils {

    /**
     * Creates a card hover animation
     *
     * @param view Target view to animate
     * @param scale Target scale for the hover effect
     * @return AnimatorSet containing the hover animation
     */
    public static AnimatorSet createHoverAnimation(ImageView view, float scale) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", scale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", scale);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

        return animatorSet;
    }

    /**
     * Creates a card flip animation
     *
     * @param view Target view to animate
     * @param duration Duration of each half of the flip animation
     * @return AnimatorSet containing the flip animation
     */
    public static AnimatorSet createFlipAnimation(ImageView view, long duration) {
        view.setCameraDistance(8000);

        ObjectAnimator firstHalf = ObjectAnimator.ofFloat(view, "rotationY", 0f, 90f)
                .setDuration(duration);
        ObjectAnimator secondHalf = ObjectAnimator.ofFloat(view, "rotationY", -90f, 0f)
                .setDuration(duration);

        firstHalf.setInterpolator(new AccelerateInterpolator());
        secondHalf.setInterpolator(new DecelerateInterpolator());

        AnimatorSet flipAnimation = new AnimatorSet();
        flipAnimation.playSequentially(firstHalf, secondHalf);

        return flipAnimation;
    }

    /**
     * Creates a fade animation
     *
     * @param view Target view to animate
     * @param startAlpha Starting alpha value
     * @param endAlpha Ending alpha value
     * @param duration Animation duration
     * @return ValueAnimator containing the fade animation
     */
    public static ValueAnimator createFadeAnimation(ImageView view, float startAlpha,
                                                    float endAlpha, long duration) {
        ValueAnimator fadeAnimator = ValueAnimator.ofFloat(startAlpha, endAlpha);
        fadeAnimator.setDuration(duration);
        fadeAnimator.addUpdateListener(animation ->
                view.setAlpha((float) animation.getAnimatedValue()));
        return fadeAnimator;
    }

    /**
     * Creates an elevation animation
     *
     * @param startElevation Starting elevation value
     * @param endElevation Ending elevation value
     * @param duration Animation duration
     * @return ValueAnimator containing the elevation animation
     */
    public static ValueAnimator createElevationAnimation(float startElevation,
                                                         float endElevation, long duration) {
        ValueAnimator elevationAnimator = ValueAnimator.ofFloat(startElevation, endElevation);
        elevationAnimator.setDuration(duration);
        return elevationAnimator;
    }
}
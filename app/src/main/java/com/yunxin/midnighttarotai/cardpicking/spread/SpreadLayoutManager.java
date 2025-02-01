package com.yunxin.midnighttarotai.cardpicking.spread;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.yunxin.midnighttarotai.R;

public class SpreadLayoutManager {
    private final Context context;
    private final ConstraintLayout spreadContainer;

    public SpreadLayoutManager(Context context, ConstraintLayout spreadContainer) {
        this.context = context;
        this.spreadContainer = spreadContainer;
    }

    public void setupSpreadLayout(@NonNull String spreadType) {
        clearExistingLayout();
        switch (spreadType) {
            case "OneCard":
                setupOneCardSpread();
                break;
            case "Timeline":
                setupTimelineSpread();
                break;
            case "Cross":
                setupCrossSpread();
                break;
            // Add other spread types
            default:
                throw new IllegalArgumentException("Unknown spread type: " + spreadType);
        }
    }

    private void clearExistingLayout() {
        spreadContainer.removeAllViews();
    }

    private void setupOneCardSpread() {
        ImageView slot = createCardSlot(1);
        spreadContainer.addView(slot);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(spreadContainer);

        constraintSet.connect(slot.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.connect(slot.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(slot.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraintSet.connect(slot.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        constraintSet.setDimensionRatio(slot.getId(), "1:1.5");
        constraintSet.constrainPercentWidth(slot.getId(), 0.4f);

        constraintSet.applyTo(spreadContainer);
    }

    private ImageView createCardSlot(int index) {
        ImageView slot = new ImageView(context);
        slot.setId(View.generateViewId());
        slot.setTag("slot_card_" + index);
        slot.setBackgroundResource(R.drawable.card_border);
        slot.setScaleType(ImageView.ScaleType.FIT_CENTER);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        slot.setLayoutParams(params);

        return slot;
    }

    private void setupTimelineSpread() {
        // Implementation for timeline spread
    }

    private void setupCrossSpread() {
        // Implementation for cross spread
    }
}
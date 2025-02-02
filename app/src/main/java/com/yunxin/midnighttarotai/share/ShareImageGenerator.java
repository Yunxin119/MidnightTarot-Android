package com.yunxin.midnighttarotai.share;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.yunxin.midnighttarotai.R;
import com.yunxin.midnighttarotai.utils.SpreadUtils;

import java.util.ArrayList;

public class ShareImageGenerator {
    private final Context context;
    private final String spreadType;
    private final String question;
    private ArrayList<Bitmap> cardImages;

    public ShareImageGenerator(Context context, String spreadType, String question, ArrayList<Bitmap> cardImages) {
        this.context = context;
        this.spreadType = spreadType;
        this.question = question;
        this.cardImages = cardImages;
    }

    public Bitmap createShareImage(View spreadContainer) {
        try {
            Log.d("ShareImageGenerator", "Creating share image");
            Log.d("ShareImageGenerator", "Card images count: " +
                    (cardImages != null ? cardImages.size() : 0));

            View shareLayout = LayoutInflater.from(context).inflate(R.layout.layout_share, null);

            ConstraintLayout container = shareLayout.findViewById(R.id.spreadContainer);
            int layoutResId = SpreadUtils.getLayoutResourceId(spreadType);
            if (container != null) {
                container.removeAllViews();
                View inflatedView = LayoutInflater.from(context).inflate(layoutResId, container, false);
                inflatedView.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                container.addView(inflatedView);
            }

            int width = 410;
            int height = 820;

            float density = context.getResources().getDisplayMetrics().density;
            int widthPx = (int) (width * density);
            int heightPx = (int) (height * density);

            shareLayout.measure(
                    View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY)
            );
            shareLayout.layout(0, 0, widthPx, heightPx);

            TextView questionText = shareLayout.findViewById(R.id.question);
            if (questionText != null) {
                questionText.setText(question);
            }

            TextView bottomText = shareLayout.findViewById(R.id.bottomText);
            if (bottomText != null) {
                bottomText.setText(spreadType + " - MidnightTarotAI");
            }

            if (cardImages != null && !cardImages.isEmpty()) {
                for (int i = 0; i < cardImages.size(); i++) {
                    int cardId = context.getResources().getIdentifier(
                            "card" + (i + 1),
                            "id",
                            context.getPackageName());
                    ImageView targetSlot = shareLayout.findViewById(cardId);
                    if (targetSlot != null) {
                        targetSlot.setImageBitmap(cardImages.get(i));
                        Log.d("ShareImageGenerator", "Set image for card " + (i + 1));
                    }
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            shareLayout.draw(canvas);

            return bitmap;
        } catch (Exception e) {
            Log.e("ShareImageGenerator", "Error creating share image", e);
            e.printStackTrace();
            return null;
        }
    }

}

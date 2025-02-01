package com.yunxin.midnighttarotai.savedreadings;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yunxin.midnighttarotai.R;
import com.yunxin.midnighttarotai.cardpicking.SpreadType;
import com.yunxin.midnighttarotai.utils.CardImageLoaderUtils;
import com.yunxin.midnighttarotai.utils.SpreadUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Fragment for displaying the visual layout of a tarot card reading.
 * Supports different spread types and handles card positioning and display.
 */
public class ReadingDetailLayoutFragment extends Fragment {
    private static final String TAG = "ReadingLayout";
    private static final String ARG_CARDS = "cards";
    private static final String ARG_SPREAD_TYPE = "spreadType";

    private List<SavedCard> cardList;
    private String spreadType;

    /**
     * Creates a new instance of ReadingDetailLayoutFragment
     * @param cards List of saved cards in the reading
     * @param spreadType Type of spread layout to display
     * @return A new instance of the fragment
     */
    public static ReadingDetailLayoutFragment newInstance(List<SavedCard> cards, String spreadType) {
        ReadingDetailLayoutFragment fragment = new ReadingDetailLayoutFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CARDS, (Serializable) cards);
        args.putString(ARG_SPREAD_TYPE, spreadType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extractArguments();
    }

    private void extractArguments() {
        Bundle args = getArguments();
        if (args != null) {
            cardList = (List<SavedCard>) args.getSerializable(ARG_CARDS);
            spreadType = args.getString(ARG_SPREAD_TYPE, SpreadType.ONE_CARD);
        } else {
            Log.w(TAG, "No arguments provided to fragment");
            cardList = Collections.emptyList();
            spreadType = SpreadType.ONE_CARD;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reading_detail_layout, container, false);

        int spreadLayoutResId = SpreadUtils.getLayoutResourceId(spreadType);
        View spreadLayout = inflater.inflate(spreadLayoutResId, (ViewGroup) rootView, false);
        ((ViewGroup) rootView).addView(spreadLayout);

        displayCards(spreadLayout);
        return rootView;
    }

    private void displayCards(View spreadLayout) {
        if (cardList == null || cardList.isEmpty()) {
            Log.e(TAG, "No cards found in reading");
            return;
        }

        List<SavedCard> sortedCards = getSortedCards();
        displayRegularCards(spreadLayout, sortedCards);
    }

    private List<SavedCard> getSortedCards() {
        List<SavedCard> sortedList = new ArrayList<>(cardList);
        Collections.sort(sortedList, (a, b) -> Integer.compare(a.getPosition(), b.getPosition()));
        return sortedList;
    }

    private void displayRegularCards(View spreadLayout, List<SavedCard> sortedCards) {
        for (SavedCard card : sortedCards) {
            String cardViewId = String.format("card%d", card.getPosition());
            int resourceId = getResources().getIdentifier(
                    cardViewId, "id", requireContext().getPackageName()
            );

            Optional.ofNullable(spreadLayout.findViewById(resourceId))
                    .ifPresent(view -> displayCardInView((ImageView) view, card));
        }
    }

    private void displayCardInView(ImageView cardView, SavedCard card) {
        CardImageLoaderUtils.getCardBitmapFromAssets(
                requireContext(),
                card.getName(),
                card.isReversed(),
                new CardImageLoaderUtils.CardImageCallback() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        cardView.setImageBitmap(bitmap);
                        cardView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Failed to load bitmap for card: " + card.getName(), e);
                    }
                }
        );
    }
}
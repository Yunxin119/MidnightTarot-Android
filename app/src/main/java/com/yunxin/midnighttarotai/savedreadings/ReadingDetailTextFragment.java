package com.yunxin.midnighttarotai.savedreadings;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yunxin.midnighttarotai.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReadingDetailTextFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReadingDetailTextFragment extends Fragment {
    private static final String TAG = "ReadingDetailTextFragment";
    private static final String ARG_INTERPRETATION = "interpretation";

    private TextView interpretationText;
    private String interpretation;

    public static ReadingDetailTextFragment newInstance(String interpretation) {
        ReadingDetailTextFragment fragment = new ReadingDetailTextFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INTERPRETATION, interpretation);
        fragment.setArguments(args);
        return fragment;
    }

    public ReadingDetailTextFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            interpretation = getArguments().getString(ARG_INTERPRETATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reading_detail_text, container, false);
        interpretationText = view.findViewById(R.id.text_interpretation);
        displayText();
        return view;
    }

    private void displayText() {
        if (interpretation == null) {
            Log.e(TAG, "Interpretation text is null!");
            return;
        }

        if (interpretationText == null) {
            Log.e(TAG, "interpretationText TextView is null!");
            return;
        }

        try {
            String formattedText = interpretation.replace("\n", "<br>");
            CharSequence styledText = Html.fromHtml(formattedText, Html.FROM_HTML_MODE_COMPACT);
            interpretationText.setText(styledText);
        } catch (Exception e) {
            Log.e(TAG, "Failed to format interpretation text: " + e.getMessage());
            interpretationText.setText(interpretation);
        }
    }
}
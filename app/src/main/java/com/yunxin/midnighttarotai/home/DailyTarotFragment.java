package com.yunxin.midnighttarotai.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yunxin.midnighttarotai.R;
import com.yunxin.midnighttarotai.auth.LoginActivity;
import com.yunxin.midnighttarotai.cardpicking.CardPickActivity;

public class DailyTarotFragment extends Fragment {
    private FirebaseUser currentUser;
    protected Button dailyReadingButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_daily_tarot, container, false);
        dailyReadingButton = view.findViewById(R.id.daily_reading);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences preferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        dailyReadingButton.setOnClickListener(v -> {
            if (currentUser == null) {
                Intent login = new Intent(getActivity(), LoginActivity.class);
                startActivity(login);
                return;
            }
            proceedWithReading();
        });
        return view;
    }

    private void proceedWithReading() {
        Intent cardpick = new Intent(getActivity(), CardPickActivity.class);
        cardpick.putExtra("spreadType", "One Card");
        cardpick.putExtra("question", "How's my day going?");
        cardpick.putExtra("pick", 1);
        startActivity(cardpick);
    }
}
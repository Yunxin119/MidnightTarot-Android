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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yunxin.midnighttarotai.R;
import com.yunxin.midnighttarotai.auth.LoginActivity;
import com.yunxin.midnighttarotai.cardpicking.CardPickActivity;

import com.yunxin.midnighttarotai.payment.PaymentBottomSheetDialog;
import com.yunxin.midnighttarotai.payment.PaymentManager;
import com.yunxin.midnighttarotai.payment.DailyReadingManager;

public class DailyTarotFragment extends Fragment {
    private FirebaseUser currentUser;
    protected Button dailyReadingButton;
    private DailyReadingManager dailyReadingManager;
    private PaymentManager paymentManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_daily_tarot, container, false);
        dailyReadingButton = view.findViewById(R.id.daily_reading);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        dailyReadingManager = new DailyReadingManager(requireContext());
        paymentManager = new PaymentManager(requireContext());
        SharedPreferences preferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        dailyReadingButton.setOnClickListener(v -> {
            if (currentUser == null) {
                Intent login = new Intent(getActivity(), LoginActivity.class);
                startActivity(login);
                return;
            }

            if (hasDailyReadingAccess()) {
                proceedWithReading();
            } else {
                showPaymentDialog();
            }
        });
        return view;
    }

    private boolean hasDailyReadingAccess() {
        return dailyReadingManager.canPerformDailyReading(currentUser.getUid()) || paymentManager.getCredits(currentUser.getUid()) >= 3;
    }

    private void showPaymentDialog() {
        PaymentBottomSheetDialog paymentDialog = new PaymentBottomSheetDialog();
        paymentDialog.show(getChildFragmentManager(), "payment_dialog");

        Toast.makeText(requireContext(), "You need a valid subscription or reading credits to continue.", Toast.LENGTH_LONG).show();
    }
    
    private void proceedWithReading() {
        Intent cardpick = new Intent(getActivity(), CardPickActivity.class);
        cardpick.putExtra("spreadType", "One Card");
        cardpick.putExtra("question", "How's my day going?");
        cardpick.putExtra("pick", 1);
        startActivity(cardpick);
    }
}
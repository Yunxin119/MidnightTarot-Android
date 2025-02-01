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
import com.yunxin.midnighttarotai.question.QuestionActivity;

public class TarotInquiryFragment extends Fragment {
    private FirebaseUser currentUser;
    protected Button tarotInquiryButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tarot_inquiry, container, false);
        tarotInquiryButton = view.findViewById(R.id.tarot_inquiry);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences preferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        tarotInquiryButton.setOnClickListener(v -> {
            if (currentUser == null) {
                Intent login = new Intent(getActivity(), LoginActivity.class);
                startActivity(login);
                return;
            }

            proceedWithQuestionReading();
        });
        return view;
    }

    private void proceedWithQuestionReading() {
        Intent intent = new Intent(getActivity(), QuestionActivity.class);
        startActivity(intent);
    }
}
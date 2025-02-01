package com.yunxin.midnighttarotai.payment;

import static com.yunxin.midnighttarotai.payment.PaymentType.ONE_TIME;
import static com.yunxin.midnighttarotai.payment.PaymentType.PACKAGE_1;
import static com.yunxin.midnighttarotai.payment.PaymentType.PACKAGE_15;
import static com.yunxin.midnighttarotai.payment.PaymentType.PACKAGE_30;
import static com.yunxin.midnighttarotai.payment.PaymentType.PACKAGE_5;
import static com.yunxin.midnighttarotai.payment.PaymentType.SUBSCRIPTION;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yunxin.midnighttarotai.R;

import java.util.ArrayList;
import java.util.List;

public class PaymentBottomSheetDialog extends BottomSheetDialogFragment {

    public interface PaymentDialogListener {
        void onPaymentDialogDismissed();
        void onPaymentSuccess();
    }

    private PaymentDialogListener listener;
    private PaymentManager paymentManager;
    private FirebaseUser currentUser;

    public void setPaymentDialogListener(PaymentDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null) {
            listener.onPaymentDialogDismissed();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_payment, container, false);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        paymentManager = new PaymentManager(requireContext());

        // Initialize views
        RecyclerView recyclerView = view.findViewById(R.id.payment_options_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));


        // Set up payment options
        List<PaymentOption> options = new ArrayList<>();
        options.add(new PaymentOption("Monthly Subscription", "Unlimited readings for $3.99/month", 3.99f, SUBSCRIPTION));
        options.add(new PaymentOption("Lifetime Access", "Unlimited readings forever for $12.99", 12.99f, ONE_TIME));

        if (!paymentManager.hasLifetimeAccess(currentUser.getUid()) && !paymentManager.hasActiveSubscription(currentUser.getUid())) {
            options.add(new PaymentOption("1 Reading", "1 reading for $0.99", 0.99f, PACKAGE_1));
            options.add(new PaymentOption("5 Readings", "5 readings for $2.99", 2.99f, PACKAGE_5));
            options.add(new PaymentOption("15 Readings", "15 readings for $6.99", 6.99f, PACKAGE_15));
            options.add(new PaymentOption("30 Readings", "30 readings for $8.99", 8.99f, PACKAGE_30));
        }

        PaymentOptionsAdapter adapter = new PaymentOptionsAdapter(options, this::handlePayment);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void handlePayment(PaymentOption option) {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Process local payment
        boolean paymentSuccess = paymentManager.processPayment(option);

        if (paymentSuccess) {
            if (listener != null) {
                listener.onPaymentSuccess();
            }
            Toast.makeText(getContext(), "Purchase successful!", Toast.LENGTH_SHORT).show();
            dismiss();
        } else {
            Toast.makeText(getContext(), "Purchase failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
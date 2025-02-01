package com.yunxin.midnighttarotai.payment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yunxin.midnighttarotai.R;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class PaymentOptionsAdapter extends RecyclerView.Adapter<PaymentOptionsAdapter.ViewHolder> {
    private List<PaymentOption> options;
    private Consumer<PaymentOption> onPaymentSelected;

    public PaymentOptionsAdapter(List<PaymentOption> options, Consumer<PaymentOption> onPaymentSelected) {
        this.options = options;
        this.onPaymentSelected = onPaymentSelected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentOption option = options.get(position);
        holder.titleTextView.setText(option.getTitle());
        holder.descriptionTextView.setText(option.getDescription());
        holder.priceButton.setText(String.format(Locale.US, "Buy for $%.2f", option.getPrice()));

        holder.priceButton.setOnClickListener(v -> {
            if (onPaymentSelected != null) {
                onPaymentSelected.accept(option);
            }
        });
    }

    @Override
    public int getItemCount() {
        return options != null ? options.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        Button priceButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_payment_title);
            descriptionTextView = itemView.findViewById(R.id.text_payment_description);
            priceButton = itemView.findViewById(R.id.button_payment_buy);
        }
    }
}
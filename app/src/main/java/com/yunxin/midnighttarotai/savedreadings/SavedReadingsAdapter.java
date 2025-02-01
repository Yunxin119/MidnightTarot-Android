package com.yunxin.midnighttarotai.savedreadings;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yunxin.midnighttarotai.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying saved tarot readings in a RecyclerView.
 * Handles both viewing and editing modes, including deletion functionality.
 */
public class SavedReadingsAdapter extends RecyclerView.Adapter<SavedReadingsAdapter.ReadingViewHolder> {

    private static final String DATE_FORMAT_PATTERN = "MMM dd, yyyy HH:mm";

    // Core data
    private final List<Reading> readingsList = new ArrayList<>();
    private final OnReadingClickListener readingClickListener;
    private final SimpleDateFormat readingDateFormatter;
    private final FirebaseReadingManager readingDataManager;

    // State
    private boolean isEditModeEnabled = false;
    private Context context;

    /**
     * Interface for handling reading item click events
     */
    public interface OnReadingClickListener {
        void onReadingClick(Reading reading);
    }

    public SavedReadingsAdapter(OnReadingClickListener listener) {
        this.readingClickListener = listener;
        this.readingDateFormatter = new SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault());
        this.readingDataManager = new FirebaseReadingManager();
    }

    @NonNull
    @Override
    public ReadingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_saved_reading, parent, false);
        return new ReadingViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReadingViewHolder holder, int position) {
        Reading reading = readingsList.get(position);
        holder.bindReadingData(reading);
    }

    @Override
    public int getItemCount() {
        return readingsList.size();
    }

    /**
     * Updates the list of readings and refreshes the view
     * @param readings New list of readings to display
     */
    public void setReadings(List<Reading> readings) {
        this.readingsList.clear();
        this.readingsList.addAll(readings);
        notifyDataSetChanged();
    }

    /**
     * Toggles edit mode for the adapter
     * @param editMode True to enable edit mode, false to disable
     */
    public void setEditMode(boolean editMode) {
        this.isEditModeEnabled = editMode;
        notifyDataSetChanged();
    }

    /**
     * Deletes a reading from Firebase and updates the UI accordingly
     */
    private void deleteReading(Reading reading, int position) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        readingDataManager.deleteUserSingleReading(currentUser.getUid(), reading.getId())
                .addOnSuccessListener(aVoid -> handleDeleteSuccess(position))
                .addOnFailureListener(e -> handleDeleteFailure());
    }

    private void handleDeleteSuccess(int position) {
        readingsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, readingsList.size());

        updateUIAfterDeletion();
    }

    private void handleDeleteFailure() {
        Toast.makeText(context, R.string.error_delete_reading, Toast.LENGTH_SHORT).show();
    }

    private void updateUIAfterDeletion() {
        if (readingsList.isEmpty() && context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.findViewById(R.id.recyclerView).setVisibility(View.GONE);
            activity.findViewById(R.id.emptyView).setVisibility(View.VISIBLE);
            activity.findViewById(R.id.edit_button).setVisibility(View.GONE);
        }
    }

    /**
     * ViewHolder class for individual reading items
     */
    class ReadingViewHolder extends RecyclerView.ViewHolder {
        private final TextView spreadTypeTextView;
        private final TextView questionTextView;
        private final TextView dateTextView;
        private final Button deleteActionButton;
        private final MaterialCardView readingCardView;

        public ReadingViewHolder(@NonNull View itemView) {
            super(itemView);
            spreadTypeTextView = itemView.findViewById(R.id.text_spread_type);
            questionTextView = itemView.findViewById(R.id.text_question);
            dateTextView = itemView.findViewById(R.id.text_date);
            deleteActionButton = itemView.findViewById(R.id.button_delete);
            readingCardView = (MaterialCardView) itemView;

            setupClickListeners();
        }

        private void setupClickListeners() {
            readingCardView.setOnClickListener(v -> handleReadingClick());
            deleteActionButton.setOnClickListener(v -> handleDeleteClick());
        }

        private void handleReadingClick() {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && readingClickListener != null) {
                readingClickListener.onReadingClick(readingsList.get(position));
            }
        }

        private void handleDeleteClick() {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                showDeleteConfirmationDialog(readingsList.get(position), position);
            }
        }

        private void showDeleteConfirmationDialog(Reading reading, int position) {
            new MaterialAlertDialogBuilder(itemView.getContext())
                    .setTitle(R.string.title_delete_reading)
                    .setMessage(R.string.message_confirm_delete_reading)
                    .setPositiveButton(R.string.action_delete, (dialog, which) ->
                            deleteReading(reading, position))
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        }

        /**
         * Binds reading data to the ViewHolder
         * @param reading Reading object containing the data to display
         */
        public void bindReadingData(Reading reading) {
            spreadTypeTextView.setText(reading.getSpreadType());
            questionTextView.setText(reading.getQuestion());
            dateTextView.setText(readingDateFormatter.format(new Date(reading.getTimestamp())));
            deleteActionButton.setVisibility(isEditModeEnabled ? View.VISIBLE : View.GONE);
        }
    }
}
package com.yunxin.midnighttarotai.savedreadings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;
import com.yunxin.midnighttarotai.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for displaying and managing saved tarot readings.
 * Implements functionality for viewing, editing, and selecting saved readings.
 */
public class SavedReadingsActivity extends AppCompatActivity implements SavedReadingsAdapter.OnReadingClickListener {

    // UI Components
    private RecyclerView readingsRecyclerView;
    private TextView emptyStateTextView;
    private Button navigationBackButton;
    private Button editModeToggleButton;

    // Data & State Management
    private SavedReadingsAdapter readingsAdapter;
    private FirebaseReadingManager readingDataManager;
    private boolean isEditModeActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_saved_readings);

        initializeWindowInsets();
        initializeUIComponents();
        fetchAndDisplaySavedReadings();
    }

    /**
     * Initializes window insets for edge-to-edge display support
     */
    private void initializeWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container_saved_readings), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Initializes and sets up all UI components including buttons, RecyclerView, and adapters
     */
    private void initializeUIComponents() {
        // Navigation setup
        navigationBackButton = findViewById(R.id.back_button);
        navigationBackButton.setOnClickListener(v -> finish());

        // Edit mode toggle setup
        editModeToggleButton = findViewById(R.id.edit_button);
        editModeToggleButton.setOnClickListener(v -> toggleEditMode());

        // RecyclerView setup
        readingsRecyclerView = findViewById(R.id.recyclerView);
        readingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        readingsAdapter = new SavedReadingsAdapter(this);
        readingsRecyclerView.setAdapter(readingsAdapter);

        // Empty state view setup
        emptyStateTextView = findViewById(R.id.emptyView);

        // Initialize Firebase manager
        readingDataManager = new FirebaseReadingManager();
    }

    /**
     * Toggles edit mode for the readings list and updates UI accordingly
     */
    private void toggleEditMode() {
        isEditModeActive = !isEditModeActive;
        readingsAdapter.setEditMode(isEditModeActive);

        // Update button appearance based on mode
        int iconResource = isEditModeActive ?
                R.drawable.ic_check_foreground :
                R.drawable.ic_edit_foreground;
        editModeToggleButton.setBackgroundResource(iconResource);
    }

    /**
     * Fetches saved readings from Firebase and updates the UI
     * Handles authentication state and displays appropriate UI states
     */
    private void fetchAndDisplaySavedReadings() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAuthenticationError();
            return;
        }

        readingDataManager.getUserReadings(currentUser.getUid())
                .addOnSuccessListener(this::handleReadingsLoadSuccess)
                .addOnFailureListener(this::handleReadingsLoadFailure);
    }

    private void handleReadingsLoadSuccess(QuerySnapshot querySnapshot) {
        List<Reading> readings = new ArrayList<>();
        querySnapshot.forEach(doc -> readings.add(doc.toObject(Reading.class)));

        updateUIForReadings(readings);
    }

    private void handleReadingsLoadFailure(Exception e) {
        Toast.makeText(this, getString(R.string.error_loading_readings), Toast.LENGTH_SHORT).show();
    }

    private void showAuthenticationError() {
        Toast.makeText(this, getString(R.string.error_authentication_required), Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Updates the UI based on the available readings
     * @param readings List of readings to display
     */
    private void updateUIForReadings(List<Reading> readings) {
        boolean hasReadings = !readings.isEmpty();

        readingsRecyclerView.setVisibility(hasReadings ? View.VISIBLE : View.GONE);
        emptyStateTextView.setVisibility(hasReadings ? View.GONE : View.VISIBLE);
        editModeToggleButton.setVisibility(hasReadings ? View.VISIBLE : View.GONE);

        if (hasReadings) {
            readingsAdapter.setReadings(readings);
        }
    }

    @Override
    public void onReadingClick(Reading reading) {
        if (!isEditModeActive) {
            navigateToReadingDetails(reading);
        }
    }

    /**
     * Navigates to the reading detail screen
     * @param reading The reading to display details for
     */
    private void navigateToReadingDetails(Reading reading) {
        Intent intent = new Intent(this, SavedReadingDetailActivity.class);
        intent.putExtra(EXTRA_READING_ID, reading.getId());
        startActivity(intent);
    }

    // Constants
    private static final String EXTRA_READING_ID = "reading_id";
}
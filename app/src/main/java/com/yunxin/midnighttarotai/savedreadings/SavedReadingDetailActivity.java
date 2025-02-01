package com.yunxin.midnighttarotai.savedreadings;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;
import com.yunxin.midnighttarotai.R;

/**
 * Activity for displaying detailed view of a saved tarot reading.
 * Shows the reading's question, card layout, and interpretation in separate tabs.
 */
public class SavedReadingDetailActivity extends AppCompatActivity {

    private static final String TAG = "SavedReadingDetail";
    private static final int TAB_LAYOUT = 0;
    private static final int TAB_READING = 1;

    // UI Components
    private TextView questionTextView;
    private TabLayout readingTabLayout;

    // Fragments
    private ReadingDetailLayoutFragment layoutFragment;
    private ReadingDetailTextFragment textFragment;

    // Data
    private String readingId;
    private FirebaseReadingManager readingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_reading_detail);

        readingManager = new FirebaseReadingManager();
        initializeViews();
        fetchReadingData();
    }

    private void initializeViews() {
        setupBackButton();
        setupQuestionView();
        setupTabLayout();
    }

    private void setupBackButton() {
        Button backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void setupQuestionView() {
        questionTextView = findViewById(R.id.text_question);
    }

    /**
     * Sets up the tab layout for switching between card layout and reading interpretation
     */
    private void setupTabLayout() {
        readingTabLayout = findViewById(R.id.tab_layout);
        readingTabLayout.addTab(readingTabLayout.newTab().setText(R.string.tab_layout));
        readingTabLayout.addTab(readingTabLayout.newTab().setText(R.string.tab_reading));
        readingTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (!areFragmentsInitialized()) return;
                switchToTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private boolean areFragmentsInitialized() {
        return layoutFragment != null && textFragment != null;
    }

    private void switchToTab(int position) {
        Fragment targetFragment = position == TAB_LAYOUT ? layoutFragment : textFragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_fragment, targetFragment)
                .commit();
    }

    /**
     * Fetches reading data from Firebase using the reading ID from intent
     */
    private void fetchReadingData() {
        readingId = getIntent().getStringExtra(EXTRA_READING_ID);
        if (readingId == null) {
            handleError(R.string.error_loading_reading);
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            handleError(R.string.error_authentication_required);
            return;
        }

        readingManager.getUserReadings(currentUser.getUid())
                .addOnSuccessListener(this::processReadingQueryResult)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load reading", e);
                    handleError(R.string.error_loading_reading);
                });
    }

    private void processReadingQueryResult(QuerySnapshot querySnapshot) {
        querySnapshot.getDocuments().stream()
                .filter(doc -> doc.getId().equals(readingId))
                .findFirst()
                .ifPresent(doc -> displayReadingData(doc.toObject(Reading.class)));
    }

    private void displayReadingData(Reading reading) {
        questionTextView.setText(reading.getQuestion());
        initializeFragments(reading);
        showLayoutFragment();
    }

    private void initializeFragments(Reading reading) {
        layoutFragment = ReadingDetailLayoutFragment.newInstance(
                reading.getCards(),
                reading.getSpreadType()
        );

        textFragment = ReadingDetailTextFragment.newInstance(
                reading.getInterpretation()
        );
    }

    private void showLayoutFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_fragment, layoutFragment)
                .commit();
    }

    private void handleError(int messageResId) {
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
        finish();
    }

    // Constants
    public static final String EXTRA_READING_ID = "reading_id";
}
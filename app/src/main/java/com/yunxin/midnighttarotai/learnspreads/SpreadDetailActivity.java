package com.yunxin.midnighttarotai.learnspreads;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yunxin.midnighttarotai.R;

public class SpreadDetailActivity extends AppCompatActivity {

    private Button backButton;
    private TextView spreadDescription;
    private TextView cardDescription;
    private ImageView cardSpread;
    private int spreadId;
    private SpreadModel spreadModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spread_detail);

        backButton = findViewById(R.id.backButton);
        spreadDescription = findViewById(R.id.description);
        cardDescription = findViewById(R.id.card_description);
        cardSpread = findViewById(R.id.card_spread);

        Intent intent = getIntent();
        spreadId = intent.getIntExtra("spreadId", -1);

        spreadModel = SpreadRepository.getInstance().getSpreadModelById(spreadId);
        cardSpread.setImageResource(spreadModel.getPreviewLayoutRes());
        spreadDescription.setText(spreadModel.getDescription());
        cardDescription.setText(spreadModel.getCardDescription());

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gridIntent = new Intent(SpreadDetailActivity.this, SpreadGridActivity.class);
                startActivity(gridIntent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
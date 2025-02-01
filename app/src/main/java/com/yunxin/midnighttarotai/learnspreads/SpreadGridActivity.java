package com.yunxin.midnighttarotai.learnspreads;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yunxin.midnighttarotai.home.MainActivity;
import com.yunxin.midnighttarotai.R;

import java.util.List;

public class SpreadGridActivity extends AppCompatActivity {
    private RecyclerView spreadsRecyclerView;
    private List<SpreadModel> spreads;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spread_grid);
        backButton = findViewById(R.id.backButton);

        spreads = SpreadRepository.getInstance().getSpreads();

        spreadsRecyclerView = findViewById(R.id.spreadsRecyclerView);
        spreadsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        int spacing = (int) getResources().getDimension(R.dimen.grid_spacing);
        spreadsRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                int column = position % 2;

                outRect.left = spacing - column * spacing / 2;
                outRect.right = (column + 1) * spacing / 2;

                if (position < 2) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing;
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent homeIntent = new Intent(SpreadGridActivity.this, MainActivity.class);
                startActivity(homeIntent);
            }
        });

        SpreadAdapter adapter = new SpreadAdapter(this, spreads, spread -> {
            Intent intent = new Intent(this, SpreadDetailActivity.class);
            intent.putExtra("spreadId", spread.getId());
            startActivity(intent);
        });

        spreadsRecyclerView.setAdapter(adapter);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
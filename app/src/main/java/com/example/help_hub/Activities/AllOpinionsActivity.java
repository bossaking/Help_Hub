package com.example.help_hub.Activities;

import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.help_hub.Adapters.OpinionsAdapter;
import com.example.help_hub.AlertDialogues.LoadingDialog;
import com.example.help_hub.OtherClasses.Opinion;
import com.example.help_hub.R;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class AllOpinionsActivity extends AppCompatActivity {

    public static final String USER_ID = "userid", USER_NAME = "username";

    private RecyclerView recyclerView;
    private OpinionsAdapter adapter;
    private LoadingDialog loadingDialog;

    private List<Opinion> opinions;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_opinions);

        loadingDialog = new LoadingDialog(this);
        loadingDialog.StartLoadingDialog();

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(getIntent().getStringExtra(USER_NAME) + " " + getString(R.string.opinions));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userId = getIntent().getStringExtra(USER_ID);

        opinions = new ArrayList<>();
        adapter = new OpinionsAdapter(opinions);
        recyclerView = findViewById(R.id.opinions_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        getAllOpinions();
    }

    private void getAllOpinions() {

        FirebaseFirestore.getInstance().collection("users").document(userId).collection("opinions")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {

                    for (DocumentSnapshot ds : queryDocumentSnapshots) {
                        opinions.add(ds.toObject(Opinion.class));
                        adapter.notifyDataSetChanged();
                    }

                    loadingDialog.DismissDialog();
                });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
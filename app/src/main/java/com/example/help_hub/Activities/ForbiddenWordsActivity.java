package com.example.help_hub.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.help_hub.AlertDialogues.AddForbiddenWordDialog;
import com.example.help_hub.AlertDialogues.EditForbiddenWordDialog;
import com.example.help_hub.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ForbiddenWordsActivity extends AppCompatActivity {

    private RecyclerView.Adapter adapter;
    RecyclerView recyclerView;
    List<String> words = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.forbidden_words_list);

        Button add_forbidden_word_button = findViewById(R.id.add_forbidden_words_button);

        add_forbidden_word_button.setOnClickListener(v -> {
            AddForbiddenWordDialog addForbiddenWordDialog = new AddForbiddenWordDialog(this);
            addForbiddenWordDialog.setOnDialogDismissedListener(this::UpdateView);
            addForbiddenWordDialog.startAddForbiddenWordDialog();
        });

        recyclerView = findViewById(R.id.forbidden_words_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(ForbiddenWordsActivity.this));

        UpdateView();
    }


    private class ForbiddenWordHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView mWord;
        private String word;

        public ForbiddenWordHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.forbidden_words_list_item, parent, false));

            itemView.setOnClickListener(this);

            mWord = itemView.findViewById(R.id.forbidden_words_list_item);
        }

        public void bind(String word) {
            this.word = word;
            mWord.setText(word);
        }

        @Override
        public void onClick(View view) {
            EditForbiddenWordDialog editForbiddenWordDialog = new EditForbiddenWordDialog(ForbiddenWordsActivity.this, word);
            editForbiddenWordDialog.setOnDialogDismissedListener(ForbiddenWordsActivity.this::UpdateView);
            editForbiddenWordDialog.startEditForbiddenWordDialog();
        }
    }

    private class ForbiddenWordsAdapter extends RecyclerView.Adapter<ForbiddenWordHolder> {

        private final List<String> words;

        public ForbiddenWordsAdapter(List<String> words) {
            this.words = words;
        }

        @NonNull
        @Override
        public ForbiddenWordHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(ForbiddenWordsActivity.this);
            return new ForbiddenWordHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ForbiddenWordHolder holder, int position) {
            String word = words.get(position);
            holder.bind(word);
        }

        @Override
        public int getItemCount() {
            return words.size();
        }
    }

    public void UpdateView() {

        words.clear();
        FirebaseFirestore wordsStorage = FirebaseFirestore.getInstance();
        wordsStorage.collection("forbiddenWords").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                    words.add(Objects.requireNonNull(document.get("word")).toString());
                }

                if(adapter == null) {
                    adapter = new ForbiddenWordsAdapter(words);
                    recyclerView.setAdapter(adapter);
                }else{
                    adapter.notifyDataSetChanged();
                }
            }
        });

    }
}

package com.example.help_hub;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.ContentView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForbiddenWordsActivity extends AppCompatActivity {

    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.forbidden_words_list);

        Button add_forbidden_word_button = findViewById(R.id.add_forbidden_words_button);

        add_forbidden_word_button.setOnClickListener(v -> {
            AddForbiddenWordDialog addForbiddenWordDialog = new AddForbiddenWordDialog(ForbiddenWordsActivity.this);
            addForbiddenWordDialog.startAddForbiddenWordDialog();
        });

        recyclerView = findViewById(R.id.forbidden_words_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(ForbiddenWordsActivity.this));
    }

    private class ForbiddenWordHolder extends RecyclerView.ViewHolder implements  View.OnClickListener {

        private TextView mWord;
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
            editForbiddenWordDialog.startEditForbiddenWordDialog();
        }
    }

    private class ForbiddenWordsAdapter extends RecyclerView.Adapter<ForbiddenWordHolder> {

        private List<String> words;

        public ForbiddenWordsAdapter(List<String> words) {this.words = words;}

        @NonNull
        @Override
        public ForbiddenWordHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(ForbiddenWordsActivity.this);
            return new ForbiddenWordHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ForbiddenWordHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return words.size();
        }

        @Override
        public void onBindViewHolder(@NonNull ForbiddenWordHolder holder, int position, @NonNull List<Object> payloads) {
            String word = words.get(position);
            holder.bind(word);
        }
    }

    public void updateView() {
        List<String> words = new ArrayList<>();
        FirebaseFirestore wordsStorage = FirebaseFirestore.getInstance();
        wordsStorage.collection("forbiddenWords").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        words.add(document.get("word").toString());
                    }

                    if (adapter == null) {
                        adapter = new ForbiddenWordsAdapter(words);
                        recyclerView.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }
}

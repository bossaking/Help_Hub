package com.example.help_hub;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import androidx.annotation.ContentView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.Console;

public class ForbiddenWordsActivity extends AppCompatActivity {

    public String word;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.forbidden_words_list);

        Button add_forbidden_word_button = findViewById(R.id.add_forbidden_words_button);

        add_forbidden_word_button.setOnClickListener(v -> {
            AddForbiddenWordDialog addForbiddenWordDialog = new AddForbiddenWordDialog(ForbiddenWordsActivity.this);
            addForbiddenWordDialog.startAddForbiddenWordDialog();
        });
    }
}

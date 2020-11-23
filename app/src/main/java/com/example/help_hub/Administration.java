package com.example.help_hub;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class Administration extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_administration, container, false);

        Button forbidden_words_list_button = view.findViewById(R.id.forbidden_words_list_button);

        forbidden_words_list_button.setOnClickListener(v -> {
            startActivity(new Intent(getActivity().getApplicationContext(), ForbiddenWordsActivity.class));
        });

        return view;
    }
}
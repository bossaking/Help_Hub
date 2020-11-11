package com.example.help_hub.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.help_hub.Activities.ForbiddenWordsActivity;
import com.example.help_hub.R;


public class Administration extends Fragment {


    private Activity myActivity;
    private Context myContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_administration, container, false);

        Button forbiddenWordsListButton = view.findViewById(R.id.forbidden_words_list_button);

        forbiddenWordsListButton.setOnClickListener(v -> startActivity(new Intent(myContext, ForbiddenWordsActivity.class)));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        myActivity = getActivity();
        myContext = getContext();

    }
}
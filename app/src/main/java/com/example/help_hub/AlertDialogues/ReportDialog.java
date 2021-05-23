package com.example.help_hub.AlertDialogues;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.help_hub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReportDialog extends DialogFragment {

    private Spinner causesSpinner;
    private Button reportButton;
    private String reportID, type;

    private Context context;

    public ReportDialog(Context context, String reportID, String type) {
        this.context = context;
        this.reportID = reportID;
        this.type = type;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.report_dialog, null);

        reportButton = view.findViewById(R.id.dialog_report_button);
        reportButton.setOnClickListener(v -> report());

        causesSpinner = view.findViewById(R.id.causes_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.report_causes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        causesSpinner.setAdapter(adapter);

        return view;
    }

    private void report() {
        CollectionReference ref = FirebaseFirestore.getInstance().collection("reports");

        Map<String, Object> reportMap = new HashMap<>();
        reportMap.put("ReportID", reportID);
        reportMap.put("Cause", causesSpinner.getSelectedItem().toString());
        reportMap.put("UserID", FirebaseAuth.getInstance().getUid());
        reportMap.put("Type", type);

        ref.add(reportMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, getString(R.string.report_message), Toast.LENGTH_SHORT).show();
                    this.dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    public void onResume() {
        super.onResume();

        Window window = Objects.requireNonNull(getDialog()).getWindow();
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;

        window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
    }
}

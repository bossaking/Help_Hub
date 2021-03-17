package com.example.help_hub.AlertDialogues;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import com.example.help_hub.Fragments.NeedHelpFragment;
import com.example.help_hub.OtherClasses.City;
import com.example.help_hub.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FiltersDialog extends DialogFragment implements TextWatcher {

    private Button applyFiltersButton, cancelButton;
    private SwitchCompat allOrdersSwitch, onlyOwnOrdersSwitch, onlyObservableOrdersSwitch;
    private AutoCompleteTextView cityTextView;
    private ImageView clearCityFieldButton;

    private List<String> cities;
    private String city;
    private int filterIndex;

    private Activity myActivity;

    LoadingDialog loadingDialog;
    private filtersDialogListener filtersDialogListener;

    public FiltersDialog(Activity myActivity, Fragment context, int filterIndex, String city) {
        this.myActivity = myActivity;
        filtersDialogListener = (filtersDialogListener) context;
        this.filterIndex = filterIndex;
        this.city = city;
    }

//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(context);
//
//        filtersDialogListener = (filtersDialogListener) context;
//    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        cities = new ArrayList<>();

        loadingDialog = new LoadingDialog(myActivity);
        loadingDialog.StartLoadingDialog();

        View view = inflater.inflate(R.layout.filters_layout_dialog, null);

        allOrdersSwitch = view.findViewById(R.id.all_switch_compat);
        allOrdersSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                onlyOwnOrdersSwitch.setChecked(false);
                onlyObservableOrdersSwitch.setChecked(false);
            }
        });
        onlyOwnOrdersSwitch = view.findViewById(R.id.only_my_own_switch_compat);
        onlyOwnOrdersSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                allOrdersSwitch.setChecked(false);
            }else{
                if(!onlyObservableOrdersSwitch.isChecked()){
                    allOrdersSwitch.setChecked(true);
                }
            }
        });
        onlyObservableOrdersSwitch = view.findViewById(R.id.only_observable_switch_compat);
        onlyObservableOrdersSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                allOrdersSwitch.setChecked(false);
            }else{
                if(!onlyOwnOrdersSwitch.isChecked()){
                    allOrdersSwitch.setChecked(true);
                }
            }
        });

        setSwitches();

        clearCityFieldButton = view.findViewById(R.id.clear_city_field_button);
        clearCityFieldButton.setOnClickListener(v -> {
            cityTextView.setText("");
        });

        cityTextView = view.findViewById(R.id.city_autocomplete_text_view);
        cityTextView.addTextChangedListener(this);
        cityTextView.setText(city);
//        cityTextView.setOnFocusChangeListener((v, hasFocus) -> {
//            if(!cityTextView.isPopupShowing()){
//                cityTextView.showDropDown();
//            }
//        });
        getCitiesFromFirebase();



        applyFiltersButton = view.findViewById(R.id.apply_filters_button);
        applyFiltersButton.setOnClickListener(v -> {
            setFilterIndex();
            filtersDialogListener.applyFilters(cityTextView.getText().toString(), filterIndex);
            dismiss();
        });

        cancelButton = view.findViewById(R.id.cancel_filters_button);
        cancelButton.setOnClickListener(v -> dismiss());


        return view;
    }

    private void setSwitches(){
        switch (filterIndex){
            case 0:
                allOrdersSwitch.setChecked(true);
                break;
            case 1:
                onlyOwnOrdersSwitch.setChecked(true);
                break;
            case 2:
                onlyObservableOrdersSwitch.setChecked(true);
                break;
            case 3:
                onlyOwnOrdersSwitch.setChecked(true);
                onlyObservableOrdersSwitch.setChecked(true);
                break;
        }
    }

    private void setFilterIndex(){
        if(allOrdersSwitch.isChecked()){
            filterIndex = 0;
            return;
        }
        if(onlyObservableOrdersSwitch.isChecked() && onlyOwnOrdersSwitch.isChecked()){
            filterIndex = 3;
            return;
        }
        if(onlyOwnOrdersSwitch.isChecked()){
            filterIndex = 1;
            return;
        }
        if(onlyObservableOrdersSwitch.isChecked()){
            filterIndex = 2;
        }
    }

    private void getCitiesFromFirebase() {

        FirebaseFirestore.getInstance().collection("cities").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    City city = documentSnapshot.toObject(City.class);
                    cities.add(city.getTitle());
                }

                updateCitiesAdapter();
            }
        });
    }

    private void updateCitiesAdapter() {
        Collections.sort(cities);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(myActivity, R.layout.support_simple_spinner_dropdown_item, cities.toArray(new String[0]));
        cityTextView.setAdapter(adapter);

        loadingDialog.DismissDialog();
    }


    @Override
    public void onResume() {
        super.onResume();

        Window window = Objects.requireNonNull(getDialog()).getWindow();
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(s.toString().isEmpty()){
            clearCityFieldButton.setVisibility(View.GONE);
        }else{
            clearCityFieldButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public interface filtersDialogListener {
        void applyFilters(String city, int filterIndex);
    }
}


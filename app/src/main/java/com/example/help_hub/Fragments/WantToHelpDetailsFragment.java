package com.example.help_hub.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.help_hub.Activities.NeedHelpDetails;
import com.example.help_hub.Activities.WantToHelpDetails;
import com.example.help_hub.Adapters.SliderAdapter;
import com.example.help_hub.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WantToHelpDetailsFragment extends Fragment {

    private ImageView wantToHelpUserImage;

    private TextView titleTextView;
    private TextView priceTextView;
    private TextView descriptionTextView;

    private TextView userNameTextView;
    private TextView phoneNumberTextView;

    private CardView wantToHelpUserDataCardView;

    Context myContext;

    String userId, offerId;

    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;

    private Boolean isObserved=false;

    public WantToHelpDetailsFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_want_to_help_details, container, false);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        myContext = getContext();
        setHasOptionsMenu(true);

        titleTextView = view.findViewById(R.id.want_to_help_title);
        priceTextView = view.findViewById(R.id.want_to_help_price);
        descriptionTextView = view.findViewById(R.id.want_to_help_description);
        wantToHelpUserImage = view.findViewById(R.id.want_to_help_user_photo);

        userNameTextView = view.findViewById(R.id.want_to_help_user_name);
        phoneNumberTextView = view.findViewById(R.id.want_to_help_user_phone_number);

        Bundle bundle = getActivity().getIntent().getExtras();

        titleTextView.setText(bundle.getString(WantToHelpDetails.EXTRA_WANT_TO_HELP_TITLE));
        priceTextView.setText(bundle.getString(WantToHelpDetails.EXTRA_WANT_TO_HELP_PRICE) + " " + getString(R.string.new_notice_currency));
        descriptionTextView.setText(bundle.getString(NeedHelpDetails.EXTRA_NEED_HELP_DESCRIPTION));

        userId = bundle.getString(NeedHelpDetails.EXTRA_NEED_HELP_USER_ID);

        wantToHelpUserDataCardView = view.findViewById(R.id.want_to_help_user_data_card_view);
        wantToHelpUserDataCardView.setOnClickListener(viewListener -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.want_to_help_details_container,
                    new OtherUserProfileFragment(userId)).addToBackStack(null).commit();
        });


        offerId = bundle.getString(WantToHelpDetails.EXTRA_WANT_TO_HELP_ID);

        StorageReference imgUserRef = storageReference.child("users/" + userId + "/profile.jpg");
        imgUserRef.getDownloadUrl().addOnSuccessListener(v -> {
            Glide.with(myContext).load(v).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24).into(wantToHelpUserImage);
        }).addOnFailureListener(v -> {
            wantToHelpUserImage.setImageResource(R.drawable.ic_baseline_missing_image_24);
        });

        DocumentReference reference = firebaseFirestore.collection("users").document(userId);
        Task<DocumentSnapshot> userSnap = reference.get();
        userSnap.addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentSnapshot doc = task.getResult();
                userNameTextView.setText(doc.getString("Name"));
                phoneNumberTextView.setText(doc.getString("Phone number"));
            }
        });

        return view;
    }

    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.other_user_profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        if(userId.equals(FirebaseAuth.getInstance().getUid())){
            menu.findItem(R.id.add_to_bookmark_button).setVisible(false);
        }else{
            checkOffers(menu);
        }
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.add_to_bookmark_button) {

            if (isObserved) {
                removeOfferFromObserved();
                item.setIcon(R.drawable.ic_baseline_star_border_24);
            } else {
                addOfferToObserved();
                item.setIcon(R.drawable.ic_baseline_star_24);
            }

            isObserved = !isObserved;
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addOfferToObserved() {
        HashMap<String, Object> offert = new HashMap<>();
        offert.put("offer id", offerId);
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid())
                .collection("observed offers").document(offerId).set(offert).addOnSuccessListener(unused -> {
            Snackbar.make(getView(), getString(R.string.added_to_observed), Snackbar.LENGTH_SHORT).show();
        });
    }

    private void removeOfferFromObserved() {
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid())
                .collection("observed offers").document(offerId).delete().addOnSuccessListener(unused ->
                Snackbar.make(getView(), getString(R.string.removed_from_observed), Snackbar.LENGTH_SHORT).show()
        );
    }

    private void checkOffers(Menu menu) {
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid())
                .collection("observed offers").document(offerId).get().addOnCompleteListener((OnCompleteListener<DocumentSnapshot>) task -> {
            if(task.getResult().exists()) {
                isObserved = true;
                menu.findItem(R.id.add_to_bookmark_button).setIcon(R.drawable.ic_baseline_star_24);
            }else{
                isObserved = false;
                menu.findItem(R.id.add_to_bookmark_button).setIcon(R.drawable.ic_baseline_star_border_24);
            }
        }).addOnFailureListener(e -> {
        });
    }
}
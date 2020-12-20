package com.example.help_hub.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.example.help_hub.Activities.NeedHelpDetails;
import com.example.help_hub.Adapters.SliderAdapter;
import com.example.help_hub.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class NeedHelpDetailsFragment extends Fragment {



    private ImageView needHelpImage;
    private ImageView needHelpUserImage;

    private TextView titleTextView;
    private TextView priceTextView;
    private TextView descriptionTextView;

    private TextView userNameTextView;
    private TextView phoneNumberTextView;

    private CardView needHelpUserDataCardView;

    private ViewPager2 pager2;

    Context myContext;

    String userId, announcementId;

    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;

    SliderAdapter sliderAdapter;

    public NeedHelpDetailsFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_need_help_details, container, false);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        myContext = getContext();

        titleTextView = view.findViewById(R.id.need_help_title);
        priceTextView = view.findViewById(R.id.need_help_budget);
        descriptionTextView = view.findViewById(R.id.need_help_description);
        needHelpUserImage = view.findViewById(R.id.need_help_user_photo);

        userNameTextView = view.findViewById(R.id.need_help_user_name);
        phoneNumberTextView = view.findViewById(R.id.need_help_user_phone_number);

        Bundle bundle = getActivity().getIntent().getExtras();

        titleTextView.setText(bundle.getString(NeedHelpDetails.EXTRA_NEED_HELP_TITLE));
        priceTextView.setText(bundle.getString(NeedHelpDetails.EXTRA_NEED_HELP_PRICE) + " " + getString(R.string.new_notice_currency));
        descriptionTextView.setText(bundle.getString(NeedHelpDetails.EXTRA_NEED_HELP_DESCRIPTION));

        userId = bundle.getString(NeedHelpDetails.EXTRA_NEED_HELP_USER_ID);

        needHelpUserDataCardView = view.findViewById(R.id.need_help_user_data_card_view);
        needHelpUserDataCardView.setOnClickListener(viewListener -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.need_help_details_container,
                    new OtherUserProfileFragment(userId)).addToBackStack(null).commit();
        });


        announcementId = bundle.getString(NeedHelpDetails.EXTRA_NEED_HELP_ID);

        pager2 = view.findViewById(R.id.need_help_photos);
        List<Uri> imagesUri = new ArrayList<>();

        sliderAdapter = new SliderAdapter(imagesUri, pager2, getContext());

        StorageReference imgRef = storageReference.child("announcement/" + announcementId + "/images");
        imgRef.listAll().addOnSuccessListener(listResult -> {
            for(StorageReference fileRef : listResult.getItems()){
                fileRef.getMetadata().addOnSuccessListener(storageMetadata -> {
                    String name = storageMetadata.getName();
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        imagesUri.add(uri);
                        sliderAdapter.notifyDataSetChanged();
                    }).addOnCompleteListener(task -> {
                    });
                });
            }
            pager2.setAdapter(sliderAdapter);

        });

        StorageReference imgUserRef = storageReference.child("users/" + userId + "/profile.jpg");
        imgUserRef.getDownloadUrl().addOnSuccessListener(v -> {
            Glide.with(myContext).load(v).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24).into(needHelpUserImage);
        }).addOnFailureListener(v -> {
            needHelpUserImage.setImageResource(R.drawable.ic_baseline_missing_image_24);
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
}
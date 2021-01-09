package com.example.help_hub.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.help_hub.Activities.ChatActivity;
import com.example.help_hub.Activities.NeedHelpDetails;
import com.example.help_hub.Adapters.SliderAdapter;
import com.example.help_hub.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
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

    private Button writeButton;

    private ViewPager2 pager2;

    Context myContext;

    String userId, announcementId;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;

    SliderAdapter sliderAdapter;

    private Boolean isObserved = false;

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
        firebaseAuth = FirebaseAuth.getInstance();

        myContext = getContext();
        setHasOptionsMenu(true);

        titleTextView = view.findViewById(R.id.need_help_title);
        priceTextView = view.findViewById(R.id.need_help_budget);
        descriptionTextView = view.findViewById(R.id.need_help_description);
        needHelpUserImage = view.findViewById(R.id.need_help_user_photo);

        userNameTextView = view.findViewById(R.id.need_help_user_name);
        phoneNumberTextView = view.findViewById(R.id.need_help_user_phone_number);

        writeButton = view.findViewById(R.id.write_button);

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

        writeButton.setOnClickListener(viewListener -> {

            Intent intent = new Intent(myContext, ChatActivity.class);
            intent.putExtra(ChatActivity.NEED_HELP_ID_EXTRA, bundle.getString(NeedHelpDetails.EXTRA_NEED_HELP_ID));
            intent.putExtra(ChatActivity.TITLE_EXTRA, bundle.getString(NeedHelpDetails.EXTRA_NEED_HELP_TITLE));
            intent.putExtra(ChatActivity.THIS_USER_ID_EXTRA, userId);
            intent.putExtra(ChatActivity.OTHER_USER_NAME_EXTRA, userNameTextView.getText().toString());

            CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("users")
                    .document(userId).collection("chats");
            collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
                for(DocumentSnapshot ds : queryDocumentSnapshots){

                    if(ds.getString("offer id").equals(bundle.getString(NeedHelpDetails.EXTRA_NEED_HELP_ID))){
                        intent.putExtra(ChatActivity.CHAT_ID_EXTRA, ds.getId());
                        break;
                    }
                }

                startActivity(intent);
            });

        });

        announcementId = bundle.getString(NeedHelpDetails.EXTRA_NEED_HELP_ID);

        pager2 = view.findViewById(R.id.need_help_photos);
        List<Uri> imagesUri = new ArrayList<>();

        sliderAdapter = new SliderAdapter(imagesUri, pager2, getContext());

        StorageReference imgRef = storageReference.child("announcement/" + announcementId + "/images");
        imgRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference fileRef : listResult.getItems()) {
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
            if (task.isSuccessful()) {
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
        if (userId.equals(FirebaseAuth.getInstance().getUid())) {
            menu.findItem(R.id.add_to_bookmark_button).setVisible(false);
        } else {
            checkAnnouncements(menu);
        }
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.add_to_bookmark_button) {

            if (isObserved) {
                removeAnnouncementFromObserved();
                item.setIcon(R.drawable.ic_baseline_star_border_24);
            } else {
                addAnnouncementToObserved();
                item.setIcon(R.drawable.ic_baseline_star_24);
            }

            isObserved = !isObserved;
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addAnnouncementToObserved() {
        HashMap<String, Object> announcement = new HashMap<>();
        announcement.put("announcement id", announcementId);
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid())
                .collection("observed announcements").document(announcementId).set(announcement).addOnSuccessListener(unused -> {
            Snackbar.make(getView(), getString(R.string.added_to_observed), Snackbar.LENGTH_SHORT).show();
        });
    }

    private void removeAnnouncementFromObserved() {
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid())
                .collection("observed announcements").document(announcementId).delete().addOnSuccessListener(unused ->
                Snackbar.make(getView(), getString(R.string.removed_from_observed), Snackbar.LENGTH_SHORT).show()
        );
    }

    private void checkAnnouncements(Menu menu) {
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid())
                .collection("observed announcements").document(announcementId).get().addOnCompleteListener((OnCompleteListener<DocumentSnapshot>) task -> {
            if (task.getResult().exists()) {
                isObserved = true;
                menu.findItem(R.id.add_to_bookmark_button).setIcon(R.drawable.ic_baseline_star_24);
            } else {
                isObserved = false;
                menu.findItem(R.id.add_to_bookmark_button).setIcon(R.drawable.ic_baseline_star_border_24);
            }
        }).addOnFailureListener(e -> {
        });
    }
}
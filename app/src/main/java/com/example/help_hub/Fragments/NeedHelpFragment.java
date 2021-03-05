package com.example.help_hub.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.help_hub.Activities.AddNewNoticeActivity;
import com.example.help_hub.Activities.EditNeedHelpActivity;
import com.example.help_hub.Activities.NeedHelpDetails;
import com.example.help_hub.OtherClasses.NeedHelp;
import com.example.help_hub.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NeedHelpFragment extends Fragment{

    public static final int NEED_HELP_DETAILS_REQUEST_CODE = 1;
    public static final int NEED_HELP_EDIT_REQUEST_CODE = 1;
    public static final int MAX_PHOTO_LOADING_ATTEMPTS = 20;

    Activity myActivity;
    Context myContext;
    private List<NeedHelp> needHelpList;
    private RecyclerView recyclerView;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private ListenerRegistration snapshotListener;

    NeedHelpAdapter adapter;

    int photoLoadingAttempts;


    //FILTER ORDERS
    private Spinner filterOrdersSpinner;
    private int filterIndex; // 0 - All, 1 - Only my own
    private List<NeedHelp> fullNeedHelpList;

    TextView informationText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        myActivity = getActivity();
        myContext = myActivity.getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        needHelpList = new ArrayList<>();
        fullNeedHelpList = new ArrayList<>();
        filterIndex = 0;

        informationText = view.findViewById(R.id.informationText);

        FloatingActionButton add = view.findViewById(R.id.floatingActionButton);
        add.setOnClickListener(v -> {
            myActivity.startActivity(new Intent(myContext, AddNewNoticeActivity.class));
        });

        recyclerView = view.findViewById(R.id.order_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(myContext));

        adapter = new NeedHelpAdapter();
        recyclerView.setAdapter(adapter);


        snapshotListener = firebaseFirestore.collection("announcement").addSnapshotListener((queryDocumentSnapshots, e) -> {

            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                NeedHelp needHelp;
                switch (dc.getType()) {
                    case ADDED:
                        needHelp = dc.getDocument().toObject(NeedHelp.class);
                        needHelp.setId(dc.getDocument().getId());
                        fullNeedHelpList.add(dc.getNewIndex(), needHelp);
                        break;
                    case MODIFIED:
                        needHelp = dc.getDocument().toObject(NeedHelp.class);
                        needHelp.setId(dc.getDocument().getId());
                        fullNeedHelpList.remove(dc.getOldIndex());
                        fullNeedHelpList.add(dc.getOldIndex(), needHelp);
                        break;
                    case REMOVED:
                        fullNeedHelpList.remove(dc.getOldIndex());
                }
            }
            filterOrders(adapter);
            adapter.notifyDataSetChanged();
        });

        //FILTER ORDERS SPINNER IMPLEMENTATION
        filterOrdersSpinner = view.findViewById(R.id.filter_orders_spinner);
        AdapterView.OnItemSelectedListener filterSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterIndex = position;
                filterOrders(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        filterOrdersSpinner.setOnItemSelectedListener(filterSelectedListener);

        return view;
    }

    //FILTER METHOD
    private void filterOrders(NeedHelpAdapter adapter){

        needHelpList.clear();

        if(filterIndex == 0){

            needHelpList.addAll(fullNeedHelpList);

        }else if(filterIndex == 1){

            for(NeedHelp nh : fullNeedHelpList){
                if(nh.getUserId().equals(FirebaseAuth.getInstance().getUid())){
                    needHelpList.add(nh);
                }
            }

        }

        if(needHelpList.size() == 0){
            informationText.setText("Wow...It looks like you haven't asked for help yet");
            informationText.setVisibility(View.VISIBLE);
        }else{
            informationText.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private class NeedHelpHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView needHelpImage, deleteImageView, editImageView;
        private TextView needHelpTitle, needHelpPrice, needHelpDescription, needHelpShowsCount;
        private NeedHelp needHelp;

        public NeedHelpHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_need_help, parent, false));

            itemView.setOnClickListener(this);

            needHelpImage = itemView.findViewById(R.id.need_help_image);
            deleteImageView = itemView.findViewById(R.id.deleteOrderImageView);
            editImageView = itemView.findViewById(R.id.editOrderImageView);
            needHelpTitle = itemView.findViewById(R.id.need_help_title);
            needHelpPrice = itemView.findViewById(R.id.need_help_price);
            needHelpDescription = itemView.findViewById(R.id.need_help_description);
            needHelpShowsCount = itemView.findViewById(R.id.shows_count_text_view);
        }

        public void bind(NeedHelp needHelp) {
            this.needHelp = needHelp;

            if(!needHelp.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                deleteImageView.setVisibility(View.INVISIBLE);
                editImageView.setVisibility(View.INVISIBLE);
            }else{
                deleteImageView.setVisibility(View.VISIBLE);
                deleteImageView.setOnClickListener(v -> {
                    deleteOrder(needHelp);
                });

                editImageView.setVisibility(View.VISIBLE);
                editImageView.setOnClickListener(v -> {
                    editOrder(needHelp);
                });
            }


            String title, desc;
            Integer showsCount;
            title = needHelp.getTitle();
            desc = needHelp.getDescription();
            showsCount = needHelp.getShowsCount();
            if (title.length() > 18)
                title = title.substring(0, 18) + "...";
            if (desc.length() > 30)
                desc = desc.substring(0, 30) + "...";
            needHelpTitle.setText(title);
            needHelpPrice.setText(getResources().getString(R.string.budget) + " " + needHelp.getPrice() + " " + getString(R.string.new_notice_currency));
            needHelpDescription.setText(desc);
            if(showsCount != null)
            needHelpShowsCount.setText(showsCount.toString());
            photoLoadingAttempts = 0;
            getImage();
        }

        private void getImage() {

            photoLoadingAttempts++;
            StorageReference imgRef = storageReference.child("announcement/" + needHelp.getId() + "/images/photo0");
            imgRef.getDownloadUrl().addOnSuccessListener(v -> {
                Glide.with(myContext).load(v).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24).into(needHelpImage);
            }).addOnFailureListener(v -> {
                needHelpImage.setImageResource(R.drawable.ic_baseline_missing_image_24);
            });
        }

        //DELETE ORDER
        private void deleteOrder(NeedHelp needHelp){
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle("Delete");
            alertDialog.setMessage("Are you sure you want to delete this?");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                    (dialog, which) -> {
                        firebaseFirestore.collection("announcement").document(needHelp.getId()).delete();
                        dialog.dismiss();
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                    ((dialog, which) -> dialog.dismiss()));
            alertDialog.show();
        }

        private void editOrder(NeedHelp needHelp) {
            Intent intent = new Intent(getContext(), EditNeedHelpActivity.class);
            intent.putExtra(EditNeedHelpActivity.EXTRA_NEED_HELP_ID, needHelp.getId());
            intent.putExtra(EditNeedHelpActivity.EXTRA_NEED_HELP_TITLE, needHelp.getTitle());
            intent.putExtra(EditNeedHelpActivity.EXTRA_NEED_HELP_PRICE, needHelp.getPrice());
            intent.putExtra(EditNeedHelpActivity.EXTRA_NEED_HELP_DESCRIPTION, needHelp.getDescription());
            intent.putExtra(EditNeedHelpActivity.EXTRA_NEED_HELP_CATEGORY, needHelp.getCategory());
            intent.putExtra(EditNeedHelpActivity.EXTRA_NEED_HELP_SUBCATEGORY, needHelp.getSubcategory());
            startActivityForResult(intent, NEED_HELP_EDIT_REQUEST_CODE);
        }

        @Override
        public void onClick(View view) {

            DocumentReference ref = FirebaseFirestore.getInstance().collection("announcement").document(needHelp.getId());
            ref.get().addOnSuccessListener(documentSnapshot -> {
                long shows = documentSnapshot.getLong("ShowsCount");
                shows++;
                HashMap<String, Object> map = new HashMap<>();
                map.put("ShowsCount", shows);
                ref.update(map);
            });

            Intent intent = new Intent(view.getContext(), NeedHelpDetails.class);
            intent.putExtra(NeedHelpDetails.EXTRA_NEED_HELP_ID, needHelp.getId());
            intent.putExtra(NeedHelpDetails.EXTRA_NEED_HELP_TITLE, needHelp.getTitle());
            intent.putExtra(NeedHelpDetails.EXTRA_NEED_HELP_PRICE, needHelp.getPrice());
            intent.putExtra(NeedHelpDetails.EXTRA_NEED_HELP_DESCRIPTION, needHelp.getDescription());
            intent.putExtra(NeedHelpDetails.EXTRA_NEED_HELP_USER_ID, needHelp.getUserId());
            startActivityForResult(intent, NEED_HELP_DETAILS_REQUEST_CODE);
        }
    }

    private class NeedHelpAdapter extends RecyclerView.Adapter<NeedHelpHolder> {

        @NonNull
        @Override
        public NeedHelpHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(myContext);
            return new NeedHelpHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull NeedHelpHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return needHelpList.size();
        }

        @Override
        public void onBindViewHolder(@NonNull NeedHelpHolder holder, int position, @NonNull List<Object> payloads) {
            NeedHelp needHelp = needHelpList.get(position);
            holder.bind(needHelp);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        snapshotListener.remove();
    }
}
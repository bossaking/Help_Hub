package com.example.help_hub.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.help_hub.Activities.AddTheOfferActivity;
import com.example.help_hub.Activities.EditNeedHelpActivity;
import com.example.help_hub.Activities.EditWantToHelpActivity;
import com.example.help_hub.Activities.WantToHelpDetails;
import com.example.help_hub.AlertDialogues.FiltersDialog;
import com.example.help_hub.AlertDialogues.LoadingDialog;
import com.example.help_hub.OtherClasses.NeedHelp;
import com.example.help_hub.OtherClasses.WantToHelp;
import com.example.help_hub.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WantToHelpFragment extends Fragment implements FiltersDialog.filtersDialogListener {

    public static final int WANT_TO_HELP_DETAILS_REQUEST_CODE = 1;
    public static final int WANT_TO_HELP_EDIT_REQUEST_CODE = 1;

    Activity myActivity;
    Context myContext;
    private List<WantToHelp> wantToHelpList;
    private RecyclerView recyclerView;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;

    WantToHelpAdapter adapter;

    TextView informationText;

    //FILTER BY BELONGING ORDERS
    private Spinner filterOrdersSpinner;
    private int filterIndex; // 0 - All, 1 - Only my own
    private List<WantToHelp> fullWantToHelpList;

    //FILTER BY SEARCH ORDERS
    SearchView searchView;
    private String searchPhrase;

    //FILTER BY CITY
    private String city;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        setHasOptionsMenu(true);

        wantToHelpList = new ArrayList<>();
        fullWantToHelpList = new ArrayList<>();
        filterIndex = 0;

        searchPhrase = "";

        city = "";

        FloatingActionButton add = view.findViewById(R.id.floatingActionButton);
        add.setOnClickListener(v -> {
            myActivity.startActivity(new Intent(myContext, AddTheOfferActivity.class));
        });

        informationText = view.findViewById(R.id.informationText);

        recyclerView = view.findViewById(R.id.order_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(myContext));

        adapter = new WantToHelpAdapter();
        recyclerView.setAdapter(adapter);

        firebaseFirestore.collection("offers").addSnapshotListener((queryDocumentSnapshots, e) -> {

            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                WantToHelp wantToHelp;
                switch (dc.getType()) {
                    case ADDED:
                        wantToHelp = dc.getDocument().toObject(WantToHelp.class);
                        wantToHelp.setId(dc.getDocument().getId());
                        fullWantToHelpList.add(dc.getNewIndex(), wantToHelp);
                        break;
                    case MODIFIED:
                        wantToHelp = dc.getDocument().toObject(WantToHelp.class);
                        wantToHelp.setId(dc.getDocument().getId());
                        fullWantToHelpList.remove(dc.getOldIndex());
                        fullWantToHelpList.add(dc.getOldIndex(), wantToHelp);
                        break;
                    case REMOVED:
                        fullWantToHelpList.remove(dc.getOldIndex());
                }
            }
            //filterOrders(adapter);
            searchOrders();
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);


        MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView)searchItem.getActionView();

        //FOR ALWAYS EXPANDED
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();

        searchView.setQueryHint(getString(R.string.type_something));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchPhrase = newText;
                searchOrders();
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.filters:
                FiltersDialog filtersDialog = new FiltersDialog(myActivity, this, filterIndex, city);
                filtersDialog.show(getActivity().getSupportFragmentManager(), null);
                break;
        }

        return true;
    }

    //FILTER BY SEARCH METHOD
    private void searchOrders(){

        wantToHelpList.clear();


        if(searchPhrase.isEmpty()){

            wantToHelpList.addAll(fullWantToHelpList);

        }else{
            searchPhrase = searchPhrase.toLowerCase();
            for (WantToHelp wth : fullWantToHelpList){
                if(wth.getTitle().toLowerCase().contains(searchPhrase) || wth.getDescription().toLowerCase().contains(searchPhrase)){
                    wantToHelpList.add(wth);
                }
            }
        }

        filterOrders(new ArrayList<>(wantToHelpList));
    }

    //FILTER BY BELONGING METHOD
    private void filterOrders(List<WantToHelp> ordersList){

        //wantToHelpList.clear();

        switch (filterIndex) {
            case 0:
                filterByCity(new ArrayList<>(wantToHelpList));
                break;
            case 1: //ONLY MY OWN
                showOnlyMyOwn(ordersList);
                filterByCity(new ArrayList<>(wantToHelpList));
                break;
            case 2://ONLY OBSERVABLE
                showOnlyObserved(ordersList);
                break;
            case 3://OBSERVABLE AND MY OWN
                showOnlyMyOwn(ordersList);
                showOnlyObserved(ordersList);
                break;
        }
    }

    private void showOnlyMyOwn(List<WantToHelp> ordersList){
        for (WantToHelp wth : ordersList) {
            if (!wth.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                wantToHelpList.remove(wth);
            }
        }
    }

    private void showOnlyObserved(List<WantToHelp> ordersList){

        LoadingDialog loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.StartLoadingDialog();

        List<String> observedList = new ArrayList<>();

        firebaseFirestore.collection("users").document(FirebaseAuth.getInstance().getUid())
                .collection("observed offers").get().addOnCompleteListener(task -> {
            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                observedList.add(documentSnapshot.getString("offer id"));
            }
            loadingDialog.DismissDialog();
            for (WantToHelp wth : ordersList) {
                if(filterIndex == 2) {
                    if (!observedList.contains(wth.getId())) {
                        wantToHelpList.remove(wth);
                    }
                }else{
                    if (observedList.contains(wth.getId())) {
                        if(!wantToHelpList.contains(wth))
                            wantToHelpList.add(wth);
                    }
                }
            }

            filterByCity(new ArrayList<>(wantToHelpList));
        });
    }

    //FILTER BY CITY
    private void filterByCity(List<WantToHelp> ordersList) {

        //needHelpList.clear();

        if (!city.isEmpty()) {
            for (WantToHelp wth : ordersList) {
                if (!wth.getCity().equals(city)) {
                    wantToHelpList.remove(wth);
                }
            }
        }

        if (wantToHelpList.size() == 0) {
            informationText.setText("Wow...It looks like you haven't asked for help yet");
            informationText.setVisibility(View.VISIBLE);
        } else {
            informationText.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void applyFilters(String city, int filterIndex) {
        this.city = city;
        this.filterIndex = filterIndex;

        searchOrders();
    }

    private class WantToHelpHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView wantToHelpImage, deleteImageView, editImageView;
        private TextView wantToHelpTitle, wantToHelpPrice, wantToHelpDescription, wantToHelpShowsCount;
        private WantToHelp wantToHelp;

        public WantToHelpHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_want_to_help, parent, false));

            itemView.setOnClickListener(this);

            wantToHelpImage = itemView.findViewById(R.id.want_to_help_image);
            deleteImageView = itemView.findViewById(R.id.deleteOrderImageView);
            editImageView = itemView.findViewById(R.id.editOrderImageView);
            wantToHelpTitle = itemView.findViewById(R.id.want_to_help_title);
            wantToHelpPrice = itemView.findViewById(R.id.want_to_help_price);
            wantToHelpDescription = itemView.findViewById(R.id.want_to_help_description);
            wantToHelpShowsCount = itemView.findViewById(R.id.shows_count_text_view);
        }

        public void bind(WantToHelp wantToHelp) {
            this.wantToHelp = wantToHelp;

            if(!wantToHelp.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                deleteImageView.setVisibility(View.INVISIBLE);
                editImageView.setVisibility(View.INVISIBLE);
            }else{
                deleteImageView.setVisibility(View.VISIBLE);
                deleteImageView.setOnClickListener(v -> {
                    deleteOrder(wantToHelp);
                });

                editImageView.setVisibility(View.VISIBLE);
                editImageView.setOnClickListener(v -> {
                    editOrder(wantToHelp);
                });
            }

            String title, desc;
            Integer showsCount;
            title = wantToHelp.getTitle();
            desc = wantToHelp.getDescription();
            showsCount = wantToHelp.getShowsCount();
            if (title.length() > 18)
                title = title.substring(0, 20) + "...";
            if (desc.length() > 30)
                desc = desc.substring(0, 30) + "...";
            wantToHelpTitle.setText(title);
            wantToHelpPrice.setText(getResources().getString(R.string.cost) + " " + wantToHelp.getPrice() + " " + getString(R.string.new_offer_currency));
            wantToHelpDescription.setText(desc);
            wantToHelpShowsCount.setText(showsCount.toString());
            StorageReference imgRef = storageReference.child("users/" + wantToHelp.getUserId() + "/profile.jpg");
            imgRef.getDownloadUrl().addOnSuccessListener(v -> {
                Glide.with(myContext).load(v).into(wantToHelpImage);
            }).addOnFailureListener(v -> {
                wantToHelpImage.setImageResource(R.drawable.ic_baseline_missing_image_24);
            });
        }

        //DELETE ORDER
        private void deleteOrder(WantToHelp wantToHelp){
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle("Delete");
            alertDialog.setMessage("Are you sure you want to delete this?");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                    (dialog, which) -> {
                        firebaseFirestore.collection("offers").document(wantToHelp.getId()).delete();
                        dialog.dismiss();
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                    ((dialog, which) -> dialog.dismiss()));
            alertDialog.show();
        }

        private void editOrder(WantToHelp wantToHelp) {
            Intent intent = new Intent(getContext(), EditWantToHelpActivity.class);
            intent.putExtra(EditWantToHelpActivity.EXTRA_WANT_TO_HELP_ID, wantToHelp.getId());
            intent.putExtra(EditWantToHelpActivity.EXTRA_WANT_TO_HELP_TITLE, wantToHelp.getTitle());
            intent.putExtra(EditWantToHelpActivity.EXTRA_WANT_TO_HELP_PRICE, wantToHelp.getPrice());
            intent.putExtra(EditWantToHelpActivity.EXTRA_WANT_TO_HELP_DESCRIPTION, wantToHelp.getDescription());
            intent.putExtra(EditWantToHelpActivity.EXTRA_WANT_TO_HELP_CATEGORY, wantToHelp.getCategory());
            intent.putExtra(EditWantToHelpActivity.EXTRA_WANT_TO_HELP_SUBCATEGORY, wantToHelp.getSubcategory());
            startActivityForResult(intent, WANT_TO_HELP_EDIT_REQUEST_CODE);
        }

        @Override
        public void onClick(View view) {

            DocumentReference ref = FirebaseFirestore.getInstance().collection("offers").document(wantToHelp.getId());
            ref.get().addOnSuccessListener(documentSnapshot -> {
                long shows = documentSnapshot.getLong("ShowsCount");
                shows++;
                HashMap<String, Object> map = new HashMap<>();
                map.put("ShowsCount", shows);
                ref.update(map);
            });

            Intent intent = new Intent(view.getContext(), WantToHelpDetails.class);
            intent.putExtra(WantToHelpDetails.EXTRA_WANT_TO_HELP_ID, wantToHelp.getId());
            intent.putExtra(WantToHelpDetails.EXTRA_WANT_TO_HELP_TITLE, wantToHelp.getTitle());
            intent.putExtra(WantToHelpDetails.EXTRA_WANT_TO_HELP_PRICE, wantToHelp.getPrice());
            intent.putExtra(WantToHelpDetails.EXTRA_WANT_TO_HELP_DESCRIPTION, wantToHelp.getDescription());
            intent.putExtra(WantToHelpDetails.EXTRA_WANT_TO_HELP_USER_ID, wantToHelp.getUserId());
            startActivityForResult(intent, WANT_TO_HELP_DETAILS_REQUEST_CODE);
        }
    }

    private class WantToHelpAdapter extends RecyclerView.Adapter<WantToHelpFragment.WantToHelpHolder> {

        @NonNull
        @Override
        public WantToHelpFragment.WantToHelpHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(myContext);
            return new WantToHelpFragment.WantToHelpHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull WantToHelpFragment.WantToHelpHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return wantToHelpList.size();
        }

        @Override
        public void onBindViewHolder(@NonNull WantToHelpFragment.WantToHelpHolder holder, int position, @NonNull List<Object> payloads) {
            WantToHelp wantToHelp = wantToHelpList.get(position);
            holder.bind(wantToHelp);
        }
    }



}

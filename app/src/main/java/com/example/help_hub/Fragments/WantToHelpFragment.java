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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.help_hub.Activities.AddWantToHelpActivity;
import com.example.help_hub.Activities.EditWantToHelpActivity;
import com.example.help_hub.Activities.DetailsWantToHelpActivity;
import com.example.help_hub.AlertDialogues.FiltersDialog;
import com.example.help_hub.AlertDialogues.LoadingDialog;
import com.example.help_hub.OtherClasses.Category;
import com.example.help_hub.OtherClasses.WantToHelp;
import com.example.help_hub.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WantToHelpFragment extends Fragment implements FiltersDialog.filtersDialogListener {

    public static final int WANT_TO_HELP_DETAILS_REQUEST_CODE = 1,
            WANT_TO_HELP_EDIT_REQUEST_CODE = 1;

    private View selectedSubcategory, previousSelectedSubcategory, previousView;
    private TextView informationText;
    private List<WantToHelp> wantToHelpList;

    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    ListenerRegistration snapshotListener;

    private RecyclerView recyclerView;
    private WantToHelpAdapter adapter;

    //FILTER BY BELONGING ORDERS
    private int filterIndex; // 0 - All, 1 - Only my own
    private List<WantToHelp> fullWantToHelpList;

    //FILTER BY SEARCH ORDERS
    private SearchView searchView;
    private String searchPhrase;

    //FILTER BY CITY
    private String city;
    private List<Category> categories;

    private Category category, subcategory;
    private MenuItem backButton;

    private MainViewCategoriesAdapter mainViewCategoriesAdapter;

    private Activity myActivity;
    private Context myContext;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        setHasOptionsMenu(true);

        wantToHelpList = new ArrayList<>();
        fullWantToHelpList = new ArrayList<>();
        categories = new ArrayList<>();

        filterIndex = 0;
        searchPhrase = "";
        city = "";

        informationText = view.findViewById(R.id.informationText);

        FloatingActionButton add = view.findViewById(R.id.floatingActionButton);
        add.setOnClickListener(v -> myActivity.startActivity(new Intent(myContext, AddWantToHelpActivity.class)));

        recyclerView = view.findViewById(R.id.order_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(myContext));

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
                        break;
                }
            }
            adapter = new WantToHelpAdapter();
            recyclerView.setAdapter(adapter);

            searchOrders();
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchItem.getActionView();

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

        backButton = menu.findItem(R.id.category_back_button);
        backButton.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.filters:
                FiltersDialog filtersDialog = new FiltersDialog(myActivity, this, filterIndex, city);
                filtersDialog.show(getActivity().getSupportFragmentManager(), null);
                break;

            case R.id.category_back_button:
                if (previousSelectedSubcategory != null)
                    previousSelectedSubcategory.setVisibility(previousView.INVISIBLE);

                if (subcategory != null) {
                    subcategory = null;
                    searchOrders();
                } else if (category != null) {
                    category = null;
                    mainViewCategoriesAdapter.showCategories();
                    searchOrders();
                    backButton.setVisible(false);
                }
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    //FILTER BY SEARCH METHOD
    private void searchOrders() {
        wantToHelpList.clear();

        if (searchPhrase.isEmpty()) wantToHelpList.addAll(fullWantToHelpList);
        else {
            searchPhrase = searchPhrase.toLowerCase();
            for (WantToHelp wth : fullWantToHelpList) {
                if (wth.getTitle().toLowerCase().contains(searchPhrase) || wth.getDescription().toLowerCase().contains(searchPhrase))
                    wantToHelpList.add(wth);
            }
        }

        filterOrders(new ArrayList<>(wantToHelpList));
    }

    //FILTER BY BELONGING METHOD
    private void filterOrders(List<WantToHelp> ordersList) {

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

    private void showOnlyMyOwn(List<WantToHelp> ordersList) {
        for (WantToHelp wth : ordersList) {
            if (!wth.getUserId().equals(FirebaseAuth.getInstance().getUid()))
                wantToHelpList.remove(wth);
        }
    }

    private void showOnlyObserved(List<WantToHelp> ordersList) {
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
                if (filterIndex == 2) {
                    if (!observedList.contains(wth.getId())) wantToHelpList.remove(wth);
                } else {
                    if (observedList.contains(wth.getId())) {
                        if (!wantToHelpList.contains(wth)) wantToHelpList.add(wth);
                    }
                }
            }

            filterByCity(new ArrayList<>(wantToHelpList));
        });
    }

    //FILTER BY CITY
    private void filterByCity(List<WantToHelp> ordersList) {
        if (!city.isEmpty()) {
            for (WantToHelp wth : ordersList) {
                if (!wth.getCity().equals(city)) wantToHelpList.remove(wth);
            }
        }

        if (wantToHelpList.size() == 0) {
            informationText.setText(getString(R.string.no_wantHelp_offerts));
            informationText.setVisibility(View.VISIBLE);
        } else informationText.setVisibility(View.GONE);

        filterByCategory(new ArrayList<>(wantToHelpList));
    }

    private void filterByCategory(List<WantToHelp> ordersList) {
        if (category == null) {
            adapter.notifyDataSetChanged();
            return;
        }

        for (WantToHelp wth : ordersList) {
            if (!wth.getCategory().equals(category.getTitle())) wantToHelpList.remove(wth);
        }

        filterBySubcategory(new ArrayList<>(wantToHelpList));
    }

    private void filterBySubcategory(List<WantToHelp> ordersList) {
        if (subcategory == null) {
            adapter.notifyDataSetChanged();
            return;
        }

        for (WantToHelp wth : ordersList) {
            if (!wth.getSubcategory().equals(subcategory.getTitle())) wantToHelpList.remove(wth);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void applyFilters(String city, int filterIndex) {
        this.city = city;
        this.filterIndex = filterIndex;

        searchOrders();
    }

    private class MainViewCategoriesHolder extends RecyclerView.ViewHolder {
        private RecyclerView mainViewCategoriesRecyclerView;

        public MainViewCategoriesHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            mainViewCategoriesRecyclerView = itemView.findViewById(R.id.main_view_categories_recycler_view);
            mainViewCategoriesAdapter = new MainViewCategoriesAdapter(myContext, categories, adapter, wantToHelpList, fullWantToHelpList);
            mainViewCategoriesRecyclerView.setAdapter(mainViewCategoriesAdapter);
        }

        public void bind() {
            if (categories.size() != 0) return;

            firebaseFirestore.collection("categories").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        categories.add(documentSnapshot.toObject(Category.class));
                    }
                    mainViewCategoriesAdapter.notifyDataSetChanged();

                    for (Category category : categories) {
                        firebaseFirestore.collection("categories").document(category.getId()).collection("Subcategories").get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        for (QueryDocumentSnapshot documentSnapshot1 : task1.getResult()) {
                                            category.subcategories.add(documentSnapshot1.toObject(Category.class));
                                        }

                                        mainViewCategoriesAdapter.notifyDataSetChanged();
                                    }
                                });
                    }
                }
            });
        }
    }

    private class WantToHelpHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView wantToHelpImage, deleteImageView, editImageView;
        private TextView wantToHelpTitle, wantToHelpPrice, wantToHelpDescription, wantToHelpShowsCount;
        private WantToHelp wantToHelp;

        public WantToHelpHolder(@NonNull @NotNull View itemView) {
            super(itemView);

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

            if (!wantToHelp.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                deleteImageView.setVisibility(View.INVISIBLE);
                editImageView.setVisibility(View.INVISIBLE);
            } else {
                deleteImageView.setVisibility(View.VISIBLE);
                deleteImageView.setOnClickListener(v -> deleteOrder(wantToHelp));

                editImageView.setVisibility(View.VISIBLE);
                editImageView.setOnClickListener(v -> editOrder(wantToHelp));
            }

            String title, desc;
            Integer showsCount;
            title = wantToHelp.getTitle();
            desc = wantToHelp.getDescription();
            showsCount = wantToHelp.getShowsCount();

            if (title.length() > 18) title = title.substring(0, 20) + "...";
            if (desc.length() > 30) desc = desc.substring(0, 30) + "...";

            wantToHelpTitle.setText(title);
            wantToHelpPrice.setText(getResources().getString(R.string.cost) + " " + wantToHelp.getPrice() + " " + getString(R.string.new_offer_currency));
            wantToHelpDescription.setText(desc);

            if (showsCount != null) wantToHelpShowsCount.setText(showsCount.toString());

            StorageReference imgRef = storageReference.child("users/" + wantToHelp.getUserId() + "/profile.jpg");
            imgRef.getDownloadUrl()
                    .addOnSuccessListener(v -> Glide.with(myContext).load(v).into(wantToHelpImage))
                    .addOnFailureListener(v -> wantToHelpImage.setImageResource(R.drawable.ic_baseline_missing_image_24));
        }

        //DELETE ORDER
        private void deleteOrder(WantToHelp wantToHelp) {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle(getString(R.string.Delete));
            alertDialog.setMessage(getString(R.string.delete_confirmation));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.confirm), (dialog, which) -> {
                firebaseFirestore.collection("offers").document(wantToHelp.getId()).delete();
                dialog.dismiss();
            });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.decline), ((dialog, which) -> dialog.dismiss()));
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

            Intent intent = new Intent(view.getContext(), DetailsWantToHelpActivity.class);
            intent.putExtra(DetailsWantToHelpActivity.EXTRA_WANT_TO_HELP_ID, wantToHelp.getId());
            intent.putExtra(DetailsWantToHelpActivity.EXTRA_WANT_TO_HELP_TITLE, wantToHelp.getTitle());
            intent.putExtra(DetailsWantToHelpActivity.EXTRA_WANT_TO_HELP_PRICE, wantToHelp.getPrice());
            intent.putExtra(DetailsWantToHelpActivity.EXTRA_WANT_TO_HELP_DESCRIPTION, wantToHelp.getDescription());
            intent.putExtra(DetailsWantToHelpActivity.EXTRA_WANT_TO_HELP_USER_ID, wantToHelp.getUserId());
            startActivityForResult(intent, WANT_TO_HELP_DETAILS_REQUEST_CODE);
        }
    }

    private class WantToHelpAdapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(myContext);
            View view;

            if (viewType == 0) {
                view = layoutInflater.inflate(R.layout.main_view_categories_layout, parent, false);
                return new MainViewCategoriesHolder(view);
            } else {
                view = layoutInflater.inflate(R.layout.item_want_to_help, parent, false);
                return new WantToHelpHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == 0) ((MainViewCategoriesHolder) holder).bind();
            else ((WantToHelpHolder) holder).bind(wantToHelpList.get(position - 1));
        }

        @Override
        public int getItemCount() {
            return wantToHelpList.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return 0;
            return 1;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (searchView != null) searchView.clearFocus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public class MainViewCategoriesAdapter extends RecyclerView.Adapter {
        private List<Category> categoryList, actualCategories;
        private Context myContext;
        private WantToHelpAdapter wantToHelpAdapter;
        private List<WantToHelp> fullWantToHelpList, wantToHelpListCopy;
        private boolean isCategory = true;
        private int choose;
        private FirebaseFirestore firebaseFirestore;

        //VIEW HOLDER STATIC CLASS
        public class AdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView singleCategoryTitle;
            public ImageView singleCategoryImageView;

            public AdapterViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
                singleCategoryTitle = itemView.findViewById(R.id.single_category_title_text_view);
                singleCategoryImageView = itemView.findViewById(R.id.single_category_image_view);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                selectedSubcategory = v.findViewById(R.id.selectedSubcategory);

                if (previousSelectedSubcategory != null)
                    previousSelectedSubcategory.setVisibility(v.INVISIBLE);

                previousView = v;

                previousSelectedSubcategory = selectedSubcategory;
                selectedSubcategory.setVisibility(v.VISIBLE);

                if (category == null) {
                    backButton.setVisible(true);
                    if (previousSelectedSubcategory != null)
                        previousSelectedSubcategory.setVisibility(previousView.INVISIBLE);
                }

                if (isCategory) {
                    showSubcategories(new ArrayList<>(categoryList), getAdapterPosition());

                    category = categoryList.get(getLayoutPosition());
                    searchOrders();

                    choose = getLayoutPosition();
                    isCategory = false;
                } else {
                    subcategory = categoryList.get(choose).subcategories.get(getLayoutPosition());
                    searchOrders();
                }
            }
        }

        public void showCategories() {
            isCategory = true;
            actualCategories = new ArrayList<>(categoryList);
            notifyDataSetChanged();
        }

        public void showSubcategories(List<Category> categories, int position) {
            actualCategories = categories.get(position).subcategories;
            notifyDataSetChanged();
        }

        //PUBLIC CONSTRUCTOR
        public MainViewCategoriesAdapter(Context myContext, List<Category> categoryList, WantToHelpAdapter adapter, List<WantToHelp> wantToHelpList, List<WantToHelp> fullWantToHelpList) {
            this.fullWantToHelpList = fullWantToHelpList;
            wantToHelpAdapter = adapter;
            actualCategories = categoryList;
            this.categoryList = categoryList;
            this.myContext = myContext;
        }

        @NonNull
        @NotNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_category_card_view, parent, false);

            return new AdapterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
            Category category = actualCategories.get(position);
            String title = category.getTitle();
            TextView singleCategoryTitle = ((AdapterViewHolder) holder).singleCategoryTitle;
            ImageView singleCategoryImageView = ((AdapterViewHolder) holder).singleCategoryImageView;
            singleCategoryTitle.setText(title);
            getImage(category, singleCategoryImageView);
        }

        private void getImage(Category category, ImageView image) {
            StorageReference imgRef = FirebaseStorage.getInstance().getReference().child("category/" + category.getId() + "/image0.png");
            imgRef.getDownloadUrl()
                    .addOnSuccessListener(v -> Glide.with(myContext).load(v).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24).into(image))
                    .addOnFailureListener(v -> image.setImageResource(R.drawable.ic_baseline_missing_image_24));
        }

        @Override
        public int getItemCount() {
            return actualCategories.size();
        }
    }
}

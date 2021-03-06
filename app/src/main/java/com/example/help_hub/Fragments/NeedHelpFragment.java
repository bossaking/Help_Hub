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
import com.example.help_hub.Activities.AddNeedHelpActivity;
import com.example.help_hub.Activities.EditNeedHelpActivity;
import com.example.help_hub.Activities.DetailsNeedHelpActivity;
import com.example.help_hub.AlertDialogues.FiltersDialog;
import com.example.help_hub.AlertDialogues.LoadingDialog;
import com.example.help_hub.OtherClasses.Category;
import com.example.help_hub.OtherClasses.NeedHelp;
import com.example.help_hub.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NeedHelpFragment extends Fragment implements FiltersDialog.filtersDialogListener {

    public static final int NEED_HELP_DETAILS_REQUEST_CODE = 1,
            NEED_HELP_EDIT_REQUEST_CODE = 1,
            MAX_PHOTO_LOADING_ATTEMPTS = 20;

    private View selectedSubcategory, previousSelectedSubcategory, previousView;
    private List<NeedHelp> needHelpList;
    int photoLoadingAttempts;

    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    ListenerRegistration snapshotListener;

    private RecyclerView recyclerView;
    private NeedHelpAdapter adapter;

    //FILTER BY BELONGING ORDERS
    private int filterIndex; // 0 - All, 1 - Only my own
    private List<NeedHelp> fullNeedHelpList;

    //FILTER BY SEARCH ORDERS
    private SearchView searchView;
    private String searchPhrase;

    //FILTER BY CITY
    private TextView informationText;
    private String city;
    private List<Category> categories;

    private Category category, subcategory;
    private MenuItem backButton;

    private MainViewCategoriesAdapter mainViewCategoriesAdapter;

    private Activity myActivity;
    private Context myContext;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        setHasOptionsMenu(true);

        needHelpList = new ArrayList<>();
        fullNeedHelpList = new ArrayList<>();
        categories = new ArrayList<>();

        filterIndex = 0;
        searchPhrase = "";
        city = "";

        informationText = view.findViewById(R.id.informationText);

        FloatingActionButton add = view.findViewById(R.id.floatingActionButton);
        add.setOnClickListener(v -> myActivity.startActivity(new Intent(myContext, AddNeedHelpActivity.class)));

        recyclerView = view.findViewById(R.id.order_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(myContext));

        snapshotListener = firebaseFirestore.collection("announcement").addSnapshotListener((queryDocumentSnapshots, e) -> {
            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                NeedHelp needHelp;

                switch (dc.getType()) {
                    case ADDED:
                        needHelp = dc.getDocument().toObject(NeedHelp.class);

                        if (needHelp.getStatus() == null || needHelp.getStatus().equals("Available")) {
                            needHelp.setId(dc.getDocument().getId());
                            fullNeedHelpList.add(needHelp);
                        }
                        break;

                    case MODIFIED:
                        needHelp = dc.getDocument().toObject(NeedHelp.class);
                        needHelp.setId(dc.getDocument().getId());

                        int index = fullNeedHelpList.indexOf(new NeedHelp() {
                            @Override
                            public boolean equals(@Nullable Object obj) {
                                NeedHelp nh = (NeedHelp) obj;
                                return nh.getId().equals(needHelp.getId());
                            }
                        });

                        if (index == -1) return;

                        if (needHelp.getStatus() == null || needHelp.getStatus().equals("Available"))
                            fullNeedHelpList.set(index, needHelp);
                        break;

                    case REMOVED:
                        needHelp = dc.getDocument().toObject(NeedHelp.class);
                        needHelp.setId(dc.getDocument().getId());

                        fullNeedHelpList.removeIf(nh -> nh.getId().equals(needHelp.getId()));
                        break;
                }
            }

            adapter = new NeedHelpAdapter();
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
        needHelpList.clear();

        if (searchPhrase.isEmpty()) needHelpList.addAll(fullNeedHelpList);
        else {
            searchPhrase = searchPhrase.toLowerCase();
            for (NeedHelp nh : fullNeedHelpList) {
                if (nh.getTitle().toLowerCase().contains(searchPhrase) || nh.getDescription().toLowerCase().contains(searchPhrase))
                    needHelpList.add(nh);
            }
        }

        filterOrders(new ArrayList<>(needHelpList));
    }

    //FILTER BY BELONGING METHOD
    private void filterOrders(List<NeedHelp> ordersList) {

        switch (filterIndex) {
            case 0:
                filterByCity(new ArrayList<>(needHelpList));
                break;

            case 1: //ONLY MY OWN
                showOnlyMyOwn(ordersList);
                filterByCity(new ArrayList<>(needHelpList));
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

    private void showOnlyMyOwn(List<NeedHelp> ordersList) {
        for (NeedHelp nh : ordersList) {
            if (!nh.getUserId().equals(FirebaseAuth.getInstance().getUid()))
                needHelpList.remove(nh);
        }
    }

    private void showOnlyObserved(List<NeedHelp> ordersList) {
        LoadingDialog loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.StartLoadingDialog();

        List<String> observedList = new ArrayList<>();

        firebaseFirestore.collection("users").document(FirebaseAuth.getInstance().getUid())
                .collection("observed announcements").get().addOnCompleteListener(task -> {
            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                observedList.add(documentSnapshot.getString("announcement id"));
            }

            loadingDialog.DismissDialog();

            for (NeedHelp nh : ordersList) {
                if (filterIndex == 2) {
                    if (!observedList.contains(nh.getId())) needHelpList.remove(nh);
                } else {
                    if (observedList.contains(nh.getId())) {
                        if (!needHelpList.contains(nh)) needHelpList.add(nh);
                    }
                }
            }

            filterByCity(new ArrayList<>(needHelpList));
        });
    }

    //FILTER BY CITY
    private void filterByCity(List<NeedHelp> ordersList) {
        if (!city.isEmpty()) {
            for (NeedHelp nh : ordersList) {
                if (!nh.getCity().equals(city)) needHelpList.remove(nh);
            }
        }

        if (needHelpList.size() == 0) {
            informationText.setText(R.string.No_needHelp_offerts);
            informationText.setVisibility(View.VISIBLE);
        } else informationText.setVisibility(View.GONE);

        filterByCategory(new ArrayList<>(needHelpList));
    }

    private void filterByCategory(List<NeedHelp> ordersList) {
        if (category == null) {
            adapter.notifyDataSetChanged();
            return;
        }

        for (NeedHelp nh : ordersList) {
            if (!nh.getCategory().equals(category.getTitle())) needHelpList.remove(nh);
        }

        filterBySubcategory(new ArrayList<>(needHelpList));
    }

    private void filterBySubcategory(List<NeedHelp> ordersList) {
        if (subcategory == null) {
            adapter.notifyDataSetChanged();
            return;
        }

        for (NeedHelp nh : ordersList) {
            if (!nh.getSubcategory().equals(subcategory.getTitle())) needHelpList.remove(nh);
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
            mainViewCategoriesAdapter = new MainViewCategoriesAdapter(myContext, categories, adapter, needHelpList, fullNeedHelpList);
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

    private class NeedHelpHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView needHelpImage, deleteImageView, editImageView;
        private TextView needHelpTitle, needHelpPrice, needHelpDescription, needHelpShowsCount;
        private NeedHelp needHelp;

        public NeedHelpHolder(@NonNull @NotNull View itemView) {
            super(itemView);

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

            if (!needHelp.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                deleteImageView.setVisibility(View.INVISIBLE);
                editImageView.setVisibility(View.INVISIBLE);
            } else {
                deleteImageView.setVisibility(View.VISIBLE);
                deleteImageView.setOnClickListener(v -> deleteOrder(needHelp));

                editImageView.setVisibility(View.VISIBLE);
                editImageView.setOnClickListener(v -> editOrder(needHelp));
            }

            String title, desc;
            Integer showsCount;
            title = needHelp.getTitle();
            desc = needHelp.getDescription();
            showsCount = needHelp.getShowsCount();

            if (title.length() > 17) title = title.substring(0, 17) + "...";
            if (desc.length() > 30) desc = desc.substring(0, 30) + "...";

            needHelpTitle.setText(title);
            needHelpPrice.setText(getResources().getString(R.string.budget) + " " + needHelp.getPrice() + " " + getString(R.string.new_notice_currency));
            needHelpDescription.setText(desc);

            if (showsCount != null) needHelpShowsCount.setText(showsCount.toString());

            photoLoadingAttempts = 0;
            getImage();
        }

        private void getImage() {
            photoLoadingAttempts++;
            StorageReference imgRef = storageReference.child("announcement/" + needHelp.getId() + "/images/photo0");
            imgRef.getDownloadUrl()
                    .addOnSuccessListener(v -> Glide.with(myContext).load(v).placeholder(R.drawable.image_with_progress).error(R.drawable.broken_image_24).into(needHelpImage))
                    .addOnFailureListener(v -> needHelpImage.setImageResource(R.drawable.ic_baseline_missing_image_24));
        }

        //DELETE ORDER
        private void deleteOrder(NeedHelp needHelp) {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle(R.string.Delete);
            alertDialog.setMessage(getString(R.string.delete_confirmation));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.confirm), (dialog, which) -> {
                firebaseFirestore.collection("announcement").document(needHelp.getId()).delete();
                dialog.dismiss();
            });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.decline), ((dialog, which) -> dialog.dismiss()));
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

            Intent intent = new Intent(view.getContext(), DetailsNeedHelpActivity.class);
            intent.putExtra(DetailsNeedHelpActivity.EXTRA_NEED_HELP_ID, needHelp.getId());
            intent.putExtra(DetailsNeedHelpActivity.EXTRA_NEED_HELP_TITLE, needHelp.getTitle());
            intent.putExtra(DetailsNeedHelpActivity.EXTRA_NEED_HELP_PRICE, needHelp.getPrice());
            intent.putExtra(DetailsNeedHelpActivity.EXTRA_NEED_HELP_DESCRIPTION, needHelp.getDescription());
            intent.putExtra(DetailsNeedHelpActivity.EXTRA_NEED_HELP_USER_ID, needHelp.getUserId());
            startActivityForResult(intent, NEED_HELP_DETAILS_REQUEST_CODE);
        }
    }

    public class NeedHelpAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(myContext);
            View view;

            if (viewType == 0) {
                view = layoutInflater.inflate(R.layout.main_view_categories_layout, parent, false);
                return new MainViewCategoriesHolder(view);
            } else {
                view = layoutInflater.inflate(R.layout.item_need_help, parent, false);
                return new NeedHelpHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == 0) ((MainViewCategoriesHolder) holder).bind();
            else ((NeedHelpHolder) holder).bind(needHelpList.get(position - 1));
        }

        @Override
        public int getItemCount() {
            return needHelpList.size() + 1;
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
        private NeedHelpFragment.NeedHelpAdapter needHelpAdapter;
        private List<NeedHelp> fullNeedHelpList, needHelpListCopy;
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
        public MainViewCategoriesAdapter(Context myContext, List<Category> categoryList, NeedHelpFragment.NeedHelpAdapter adapter, List<NeedHelp> needHelpList, List<NeedHelp> fullNeedHelpList) {
            this.fullNeedHelpList = fullNeedHelpList;
            needHelpAdapter = adapter;
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
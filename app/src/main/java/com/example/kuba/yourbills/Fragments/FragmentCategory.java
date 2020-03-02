package com.example.kuba.yourbills.Fragments;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import com.example.kuba.yourbills.Adapters.BillsListAdapter;
import com.example.kuba.yourbills.Adapters.CategoriesListAdapter;
import com.example.kuba.yourbills.Interfaces.RecyclerViewClickListener;
import com.example.kuba.yourbills.Models.Category;
import com.example.kuba.yourbills.R;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.app.Activity.RESULT_OK;

public class FragmentCategory extends Fragment {

    public static int FRAGMENT_CODE = 92472;
    private ArrayList<Category> categoriesList;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private String selectedCategory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_category, container, false);

        init(view);

        showCategoriesList();


        return view;
    }

    private void init(View view){
        recyclerView = view.findViewById(R.id.categories_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        categoriesList = createCategoriesList();
        getArgs();
    }


    private void showCategoriesList(){
        RecyclerViewClickListener listener = (view, position) -> {
            selectedCategory = categoriesList.get(position).getTitle();
            sendParams();
        };
        mAdapter = new CategoriesListAdapter(categoriesList, selectedCategory, listener, getContext());
        recyclerView.setAdapter(mAdapter);
    }

    private ArrayList<Category> createCategoriesList(){
        ArrayList<Category> categoriesList = new ArrayList<>();
        ArrayList<String> categoriesTitles = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.category_titles)));
        TypedArray categoriesIcons = getResources().obtainTypedArray(R.array.category_icons);

        if(categoriesTitles.size()==categoriesIcons.length()){
            for(int i=0; i<categoriesTitles.size(); i++){
                Category category = new Category(categoriesTitles.get(i), categoriesIcons.getDrawable(i));
                categoriesList.add(category);
            }
        }

        return categoriesList;
    }

    public void setArgs(String selectedCategory){
        Bundle args = new Bundle();
        args.putString("selectedCategory", selectedCategory);
        this.setArguments(args);
    }

    public void getArgs(){
        selectedCategory = getArguments().getString("selectedCategory", "");
    }

    private void sendParams(){
        Intent intent = new Intent(getContext(), FragmentCategory.class);
            intent.putExtra("selectedCategory", selectedCategory);

        getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, intent);
        getFragmentManager().popBackStack();
    }

}

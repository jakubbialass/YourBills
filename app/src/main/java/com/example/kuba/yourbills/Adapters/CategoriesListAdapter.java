package com.example.kuba.yourbills.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kuba.yourbills.Interfaces.RecyclerViewClickListener;
import com.example.kuba.yourbills.Models.Category;
import com.example.kuba.yourbills.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CategoriesListAdapter extends RecyclerView.Adapter<CategoriesListAdapter.CategoriesViewHolder>  {

    private ArrayList<Category> categoriesList;
    private String selectedCategory;
    private RecyclerViewClickListener listener;
    private Context context;

    public CategoriesListAdapter(ArrayList<Category> categoriesList, String selectedCategory, RecyclerViewClickListener listener, Context context){
        this.categoriesList = categoriesList;
        this.selectedCategory = selectedCategory;
        this.listener = listener;
        this.context = context;
    }

    public static class CategoriesViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{
        private TextView categoryTitle, categoryIcon;
        private RecyclerViewClickListener listener;



        private CategoriesViewHolder(View itemView, RecyclerViewClickListener listener) {
            super(itemView);
            this.categoryTitle = itemView.findViewById(R.id.category_title);
            this.categoryIcon = itemView.findViewById(R.id.category_icon);
            this.listener = listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onClick(view, getAdapterPosition());
        }
    }


    @NonNull
    @Override
    public CategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View categoriesListViewItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_category, parent, false);
        return new CategoriesViewHolder(categoriesListViewItem, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesViewHolder holder, int position) {
        Category category = categoriesList.get(position);
        holder.categoryTitle.setText(category.getTitle());
        holder.categoryIcon.setBackground(category.getIcon());
        holder.categoryTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        if(holder.categoryTitle.getText().toString().toUpperCase().equals(selectedCategory.toUpperCase()))
            holder.categoryTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.ic_check), null);
        Log.v("porownuje", holder.categoryTitle.getText().toString().toUpperCase() + " " + selectedCategory.toUpperCase());
    }

    @Override
    public int getItemCount() {
        return categoriesList.size();
    }


    private int getSelectedCategoryPosition(){
        int selectedCategoryPosition =-1;
        for(int i=0; i<categoriesList.size(); i++){
            if(categoriesList.get(i).getTitle().equals(selectedCategory)) {
                selectedCategoryPosition = i;
                break;
            }
        }
        return selectedCategoryPosition;
    }

}

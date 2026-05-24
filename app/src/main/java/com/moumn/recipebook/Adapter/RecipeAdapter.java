package com.moumn.recipebook.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moumn.recipebook.Model.RecipeModel;
import com.moumn.recipebook.R;
import com.moumn.recipebook.View.RecipeDetailsActivity;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private Context context;
    private List<RecipeModel> recipeList;

    public RecipeAdapter(Context context, List<RecipeModel> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
    }

    public void updateList(List<RecipeModel> newList) {
        this.recipeList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeModel recipe = recipeList.get(position);
        holder.titleText.setText(recipe.getTitle());
        holder.categoryText.setText("Category: "+recipe.getCategory());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailsActivity.class);
            intent.putExtra("recipeId", recipeList.get(position).getId());
            context.startActivity(intent);
            ((Activity) context).finish();
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, categoryText;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.recipeTitleText);
            categoryText = itemView.findViewById(R.id.recipeCategoryText);
        }
    }
}

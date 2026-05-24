package com.moumn.recipebook.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.moumn.recipebook.Model.RecipeModel;
import com.moumn.recipebook.R;
import com.moumn.recipebook.View.RecipeDetailsActivity;
import com.moumn.recipebook.View.UpdateRecipeActivity;


import java.util.List;

public class RecipeAdapterProfile extends RecyclerView.Adapter<RecipeAdapterProfile.RecipeViewHolder> {

    private Context context;
    private List<RecipeModel> recipeList;

    public RecipeAdapterProfile(Context context, List<RecipeModel> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe_profile, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeModel recipe = recipeList.get(position);
        holder.titleText.setText(recipe.getTitle());
        holder.categoryText.setText(recipe.getCategory());


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailsActivity.class);
            intent.putExtra("recipeId", recipe.getId());
            context.startActivity(intent);
        });

            holder.editIcon.setVisibility(View.VISIBLE);
            holder.deleteIcon.setVisibility(View.VISIBLE);

            holder.editIcon.setOnClickListener(v -> {
                Intent intent = new Intent(context, UpdateRecipeActivity.class);
                intent.putExtra("recipeId", recipe.getId());
                context.startActivity(intent);
            });

            holder.deleteIcon.setOnClickListener(v -> {
                FirebaseFirestore.getInstance()
                        .collection("Recipes")
                        .document(recipe.getId())
                        .delete()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(context, "Recipe deleted", Toast.LENGTH_SHORT).show();
                            recipeList.remove(position);
                            notifyItemRemoved(position);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                        });
            });

    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView  editIcon, deleteIcon;
        TextView titleText, categoryText;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            editIcon = itemView.findViewById(R.id.editIcon2);
            deleteIcon = itemView.findViewById(R.id.deleteIcon2);
            titleText = itemView.findViewById(R.id.titleText);
            categoryText = itemView.findViewById(R.id.categoryText);
        }
    }
}

package com.moumn.recipebook.View;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.moumn.recipebook.R;
import com.moumn.recipebook.Model.RecipeModel;
import com.moumn.recipebook.databinding.ActivityUpdateRecipeBinding;

import java.util.HashMap;
import java.util.Map;

public class UpdateRecipeActivity extends AppCompatActivity {
    private ActivityUpdateRecipeBinding binding;
    private String recipeId;
    private FirebaseFirestore firestore;
    private String[] categories = {"Select Category", "Dessert", "Main Dish", "Drink", "Snack", "Salad"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityUpdateRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firestore = FirebaseFirestore.getInstance();
        recipeId = getIntent().getStringExtra("recipeId");
        if (recipeId != null) {
            loadRecipeData(recipeId);
        }
        binding.updateRecipeButton.setOnClickListener(v -> updateRecipe());
        binding.backButtonUpdate.setOnClickListener(view -> finish());
    }

    private void loadRecipeData(String recipeId) {
        firestore.collection("Recipes").document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        RecipeModel recipe = documentSnapshot.toObject(RecipeModel.class);
                        if (recipe != null) {
                            binding.updateTitleEditText.setText(recipe.getTitle());
                            binding.updateIngredientsEditText.setText(String.join("\n", recipe.getIngredients()));
                            binding.updateStepsEditText.setText(String.join("\n", recipe.getSteps()));

                            Spinner spinner = binding.updateCategorySpinner;
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
                            binding.updateCategorySpinner.setAdapter(adapter);
                            int position = 0;
                            for (int i = 0; i < categories.length; i++) {
                                if (categories[i].equalsIgnoreCase(recipe.getCategory())) {
                                    position = i;
                                    break;
                                }
                            }
                            spinner.setSelection(position);
                            binding.updateVideoUrlEditText.setText(recipe.getVideoUrl());
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load recipe", Toast.LENGTH_SHORT).show());
    }

    private void updateRecipe() {
        String title = binding.updateTitleEditText.getText().toString().trim();
        String ingredients = binding.updateIngredientsEditText.getText().toString().trim();
        String steps = binding.updateStepsEditText.getText().toString().trim();
        String category = binding.updateCategorySpinner.getSelectedItem().toString().trim();
        String videoUrl = binding.updateVideoUrlEditText.getText().toString().trim();

        if (title.isEmpty() || ingredients.isEmpty() || steps.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("title", title);
        updateMap.put("ingredients", ingredients.split("\n"));
        updateMap.put("steps", steps.split("\n"));
        updateMap.put("category", category);
        updateMap.put("videoUrl", videoUrl);

        firestore.collection("Recipes").document(recipeId)
                .update(updateMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Recipe updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show());
    }
}
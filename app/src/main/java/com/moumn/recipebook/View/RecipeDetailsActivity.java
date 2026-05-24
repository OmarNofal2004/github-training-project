package com.moumn.recipebook.View;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.moumn.recipebook.R;
import com.moumn.recipebook.Model.RecipeModel;
import com.moumn.recipebook.databinding.ActivityRecipeDetailsBinding;

import java.util.List;

public class RecipeDetailsActivity extends AppCompatActivity {
    private ActivityRecipeDetailsBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String recipeId;
    private String creatorId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRecipeDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding.ingredientsText.setMovementMethod(new ScrollingMovementMethod());
        binding.stepsText.setMovementMethod(new ScrollingMovementMethod());

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        recipeId = getIntent().getStringExtra("recipeId");
        if (recipeId != null) {
            loadRecipeDetails(recipeId);
        }
        binding.buttonBack.setOnClickListener(view -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }

    private void loadRecipeDetails(String id) {
        DocumentReference recipeRef = firestore.collection("Recipes").document(id);
        recipeRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                RecipeModel recipe = documentSnapshot.toObject(RecipeModel.class);
                if (recipe != null) {
                    creatorId = recipe.getOwnerId();

                    binding.titleText.setText(recipe.getTitle());
                    binding.categoryText.setText("Category: " + recipe.getCategory());
                    List<String> ingredients = recipe.getIngredients();
                    if (ingredients != null) {
                        StringBuilder ingredientsBuilder = new StringBuilder();
                        for (String item : ingredients) {
                            ingredientsBuilder.append("• ").append(item).append("\n");
                        }
                        binding.ingredientsText.setText(ingredientsBuilder.toString().trim());
                    }
                    List<String> steps = recipe.getSteps();
                    if (steps != null) {
                        StringBuilder stepsBuilder = new StringBuilder();
                        for (int i = 0; i < steps.size(); i++) {
                            stepsBuilder.append((i + 1)).append(". ").append(steps.get(i)).append("\n");
                        }
                        binding.stepsText.setText(stepsBuilder.toString().trim());
                    }
                    if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getUid().equals(creatorId)) {
                        binding.editIcon.setVisibility(View.VISIBLE);
                        binding.deleteIcon.setVisibility(View.VISIBLE);
                    } else {
                        binding.editIcon.setVisibility(View.GONE);
                        binding.deleteIcon.setVisibility(View.GONE);
                    }

                    binding.videoUrlText.setOnClickListener(v -> {
                        if (recipe.getVideoUrl().isEmpty()){
                            Toast.makeText(this,"Cant Found Video Now",Toast.LENGTH_SHORT).show();
                        }else{
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(recipe.getVideoUrl()));
                            startActivity(intent);
                        }

                    });

                    binding.deleteIcon.setOnClickListener(v -> deleteRecipe());
                    binding.editIcon.setOnClickListener(v -> {
                        Intent editIntent = new Intent(this, UpdateRecipeActivity.class);
                        editIntent.putExtra("recipeId", recipeId);
                        startActivity(editIntent);
                    });
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load recipe.", Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteRecipe() {
        firestore.collection("Recipes").document(recipeId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Recipe deleted successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete recipe", Toast.LENGTH_SHORT).show();
                });
    }
}
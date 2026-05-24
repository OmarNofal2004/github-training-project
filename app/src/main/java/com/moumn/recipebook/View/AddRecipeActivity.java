package com.moumn.recipebook.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.moumn.recipebook.R;
import com.moumn.recipebook.databinding.ActivityAddRecipeBinding;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddRecipeActivity extends AppCompatActivity {
    private static ActivityAddRecipeBinding binding;
    private String[] categories = {"Select Category", "Dessert", "Main Dish", "Drink", "Snack", "Salad"};
    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        binding.categorySpinner.setAdapter(adapter);
        binding.addRecipeButton.setOnClickListener(v -> saveRecipeToFirestore());
        binding.backButton.setOnClickListener(view -> {
            startActivity(new Intent(this,HomeActivity.class));
            finish();
        });
    }

    private void saveRecipeToFirestore() {
        String title = binding.titleEditText.getText().toString().trim();
        String ingredientsStr = binding.ingredientsEditText.getText().toString().trim();
        String stepsStr = binding.stepsEditText.getText().toString().trim();
        String category = binding.categorySpinner.getSelectedItem().toString();
        if (category.equals("Select Category")) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }        String videoUrl = binding.videoUrlEditText.getText().toString().trim();

        if (title.isEmpty() || ingredientsStr.isEmpty() || stepsStr.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> ingredients = Arrays.asList(ingredientsStr.split("\\r?\\n"));
        List<String> steps = Arrays.asList(stepsStr.split("\\r?\\n"));
        Map<String, Object> recipe = new HashMap<>();
        recipe.put("title", title);
        recipe.put("ingredients", ingredients);
        recipe.put("steps", steps);
        recipe.put("category", category.toLowerCase());
        recipe.put("videoUrl", videoUrl);
        recipe.put("ownerId", uid);

        FirebaseFirestore.getInstance()
                .collection("Recipes")
                .add(recipe)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Recipe added successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add recipe: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}

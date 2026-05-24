package com.moumn.recipebook.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.moumn.recipebook.R;
import com.moumn.recipebook.Adapter.RecipeAdapter;
import com.moumn.recipebook.Model.RecipeModel;
import com.moumn.recipebook.databinding.ActivityHomeBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {
    private static ActivityHomeBinding binding;
    private FirebaseFirestore firestore;
    private List<RecipeModel> fullRecipeList = new ArrayList<>();
    private List<RecipeModel> filteredList = new ArrayList<>();
    private RecipeAdapter adapter;
    private String selectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        firestore = FirebaseFirestore.getInstance();

        adapter = new RecipeAdapter(this, filteredList);
        binding.recipeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recipeRecyclerView.setAdapter(adapter);

        loadUserProfileImage();
        loadRecipes();
        setupListeners();

    }
        private void setupListeners() {
            binding.AddRecipeButton.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, AddRecipeActivity.class));
                finish();
            });
            binding.profileIcon.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                finish();
            });
            binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override public void onTabSelected(TabLayout.Tab tab) {
                    selectedCategory = tab.getText().toString();
                    filterRecipes();
                }
                @Override public void onTabUnselected(TabLayout.Tab tab) {}
                @Override public void onTabReselected(TabLayout.Tab tab) {}
            });

            binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filterRecipes();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterRecipes();
                    return true;
                }
            });
        }

        private void loadRecipes() {
            CollectionReference recipesRef = firestore.collection("Recipes");
            recipesRef.get().addOnSuccessListener(querySnapshot -> {
                fullRecipeList.clear();
                Set<String> categories = new HashSet<>();

                for (QueryDocumentSnapshot doc : querySnapshot) {
                    RecipeModel recipe = doc.toObject(RecipeModel.class);
                    recipe.setId(doc.getId());
                    fullRecipeList.add(recipe);
                    categories.add(recipe.getCategory());
                }

                setupTabs(categories);
                filterRecipes();

            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load recipes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }

        private void setupTabs(Set<String> categories) {
            binding.tabLayout.removeAllTabs();
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText("All"));
            for (String category : categories) {
                binding.tabLayout.addTab(binding.tabLayout.newTab().setText(category));
            }
        }

        private void filterRecipes() {
            String searchText = binding.searchView.getQuery().toString().trim().toLowerCase();
            filteredList.clear();

            for (RecipeModel recipe : fullRecipeList) {
                boolean matchCategory = selectedCategory.equals("All") || recipe.getCategory().equalsIgnoreCase(selectedCategory);
                boolean matchSearch = recipe.getTitle().toLowerCase().contains(searchText);

                if (matchCategory && matchSearch) {
                    filteredList.add(recipe);
                }
            }

            adapter.notifyDataSetChanged();
        }
    private void loadUserProfileImage() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("Users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            ImageView imageView = findViewById(R.id.profileIcon);
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.user)
                                    .error(R.drawable.user)
                                    .circleCrop()
                                    .into(imageView);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                });
    }

}
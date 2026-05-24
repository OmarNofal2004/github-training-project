package com.moumn.recipebook.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.moumn.recipebook.R;
import com.moumn.recipebook.Adapter.RecipeAdapterProfile;
import com.moumn.recipebook.Model.RecipeModel;
import com.moumn.recipebook.databinding.ActivityProfileBinding;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private static ActivityProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private List<RecipeModel> userRecipes;
    private RecipeAdapterProfile adapter;
    private String currentUserId;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        currentUserId = currentUser.getUid();

        firestore.collection("Users").document(currentUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String name = snapshot.getString("name");
                        String email = snapshot.getString("email");
                        String imageUrl = snapshot.getString("imageUrl");

                        binding.nameText.setText(name);
                        binding.emailText.setText(email);
                        Glide.with(this).load(imageUrl).into(binding.profileImage);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                });

        userRecipes = new ArrayList<>();
        adapter = new RecipeAdapterProfile(this, userRecipes);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        firestore.collection("Recipes")
                .whereEqualTo("ownerId", currentUserId)
                .get()
                .addOnSuccessListener(query -> {
                    userRecipes.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        RecipeModel recipe = doc.toObject(RecipeModel.class);
                        recipe.setId(doc.getId());
                        userRecipes.add(recipe);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load recipes", Toast.LENGTH_SHORT).show();
                });

        binding.logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            sharedPreferences.edit().putBoolean("rememberMe", false).apply();
            startActivity(new Intent(this, LoginActivity.class));
            Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
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

import com.google.firebase.auth.FirebaseAuth;
import com.moumn.recipebook.R;
import com.moumn.recipebook.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    public static ActivityLoginBinding mainBinding;
    private FirebaseAuth Auth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        mainBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("rememberMe", false)) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Auth = FirebaseAuth.getInstance();
        mainBinding.loginButton.setOnClickListener(view -> loginUser());
        mainBinding.googleButton.setOnClickListener(v -> Toast.makeText(this, "Not available now", Toast.LENGTH_SHORT).show());
        mainBinding.facebookButton.setOnClickListener(v ->
                Toast.makeText(this, "Not available now", Toast.LENGTH_SHORT).show());

        setupSignupText();
    }
    private void loginUser() {
        String email = mainBinding.emailEditText.getText().toString().trim();
        String password = mainBinding.passwordEditText.getText().toString().trim();

        if (email.isEmpty() ) {
            Toast.makeText(this, "Email required", Toast.LENGTH_SHORT).show();
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Password required", Toast.LENGTH_SHORT).show();
        } else if (password.length()<8) {
            Toast.makeText(this, "Password must be more than 8 character", Toast.LENGTH_SHORT).show();
        }else{
            Auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                        if (mainBinding.rememberCheckBox.isChecked()) {
                            sharedPreferences.edit().putBoolean("rememberMe", true).apply();
                        } else {
                            sharedPreferences.edit().putBoolean("rememberMe", false).apply();
                        }
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

    }

    private void setupSignupText() {

        mainBinding.signupText.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
            finish();
        });
    }
    }

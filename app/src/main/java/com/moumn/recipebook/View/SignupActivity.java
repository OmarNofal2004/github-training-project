package com.moumn.recipebook.View;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.moumn.recipebook.R;
import com.moumn.recipebook.databinding.ActivitySignupBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private Uri selectedImageUri;
    private final int IMAGE_PICK_CODE = 1001;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


        String[] countries = {"Select your country", "Egypt", "Palestine", "Jordan", "Lebanon", "Syria"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, countries);
        binding.countrySpinner.setAdapter(adapter);

        binding.profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE_PICK_CODE);
        });

        binding.signupButton.setOnClickListener(v -> {
            String name = binding.nameEditText.getText().toString().trim();
            String email = binding.emailEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();
            String country = binding.countrySpinner.getSelectedItem().toString();

            if (selectedImageUri == null) {
                Toast.makeText(this, "Profile image Required", Toast.LENGTH_SHORT).show();
            }else if (name.isEmpty()) {
                Toast.makeText(this, "Full Name Required", Toast.LENGTH_SHORT).show();
            } else if ( email.isEmpty()) {
                Toast.makeText(this, "Email Required", Toast.LENGTH_SHORT).show();

            } else if (password.isEmpty()) {
                Toast.makeText(this, "Password Required", Toast.LENGTH_SHORT).show();

            } else if (password.length()<8) {
                Toast.makeText(this, "Password must be more than 8 characters", Toast.LENGTH_SHORT).show();
            }
             else if (country.equals("Select your country")) {
                Toast.makeText(this, "Please select country", Toast.LENGTH_SHORT).show();
            } else{
                registerUser(name, email, password, country);
            }
        });

        binding.loginText.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser(String name, String email, String password, String country) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    uploadProfileImage(uid, name, email, country);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.signupButton.setEnabled(true);
                });
    }

    private void uploadProfileImage(String uid, String name, String email, String country) {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show();
            binding.signupButton.setEnabled(true);
            return;
        }
         try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                byte[] imageBytes = new byte[inputStream.available()];
                inputStream.read(imageBytes);
                String encodedImage = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                OkHttpClient client = new OkHttpClient();

                RequestBody requestBody = new FormBody.Builder()
                        .add("key", "7814fd04d610f953afc929922046cdf5") 
                        .add("image", encodedImage)
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.imgbb.com/1/upload")
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(SignupActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String result = response.body().string();
                            try {
                                JSONObject json = new JSONObject(result);
                                String imageUrl = json.getJSONObject("data").getString("url");

                                runOnUiThread(() -> saveUserData(uid,name, email, country, imageUrl));
                            } catch (JSONException e) {
                                runOnUiThread(() -> {
                                    Toast.makeText(SignupActivity.this, "Parse error", Toast.LENGTH_SHORT).show();
                                });
                            }
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(SignupActivity.this, "ImgBB response failed", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });

            } catch (Exception e) {
                Toast.makeText(this, "Image error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    private void saveUserData(String uid, String name, String email, String country, String imageUrl) {
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("uid", uid);
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("country", country);
        userMap.put("imageUrl", imageUrl);

        firestore.collection("Users").document(uid)
                .set(userMap)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.signupButton.setEnabled(true);
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            binding.profileImage.setImageURI(selectedImageUri);
        }
    }
}
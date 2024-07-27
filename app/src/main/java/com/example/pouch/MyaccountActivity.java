package com.example.pouch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyaccountActivity extends AppCompatActivity {
    private RecyclerView myAccountRecyclerView;
    private MyAccountProductAdapter adapter;
    public static final String TAG = "MyAccountActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myaccount);

        myAccountRecyclerView = findViewById(R.id.recyclerView_myaccount);
        myAccountRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MyAccountProductAdapter();
        myAccountRecyclerView.setAdapter(adapter);
        fetchDataFromFirestore();

        ImageButton logoutButton = findViewById(R.id.btn_logout);
        logoutButton.setOnClickListener(v -> new AlertDialog.Builder(MyaccountActivity.this)
                .setTitle("Logout")
                .setMessage("Are you going to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(MyaccountActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("No", null)
                .show());

        TextView emailTextView = findViewById(R.id.myaccount_id);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            emailTextView.setText(user.getEmail());
        } else {
            emailTextView.setText("Not logged in");
        }

        adapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(MyaccountActivity.this, ProductDetails.class);
            intent.putExtra("ProductName", product.getProductName());
            startActivity(intent);
        });

    }

    private void fetchDataFromFirestore() {
        Log.d(TAG, "fetchDataFromFirestore called");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("UserLikes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> likedProductIds = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String productId = document.getString("productId");
                            if (productId != null) {
                                likedProductIds.add(productId);
                            }
                        }

                        Log.d(TAG, "Liked Product IDs: " + likedProductIds);

                        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                        for (String productId : likedProductIds) {
                            Task<DocumentSnapshot> productTask = db.collection("Product").document(productId).get();
                            tasks.add(productTask);
                        }

                        Task<List<DocumentSnapshot>> combinedTask = Tasks.whenAllSuccess(tasks);
                        combinedTask.addOnCompleteListener(productTask -> {
                            if (productTask.isSuccessful()) {
                                List<Product> fetchedProducts = new ArrayList<>();
                                for (DocumentSnapshot document : productTask.getResult()) {
                                    fetchedProducts.add(document.toObject(Product.class));
                                }
                                Log.d(TAG, "Fetched products based on liked IDs: " + fetchedProducts);
                                adapter.setProducts(fetchedProducts);
                            } else {
                                Log.e(TAG, "Error fetching liked products.", productTask.getException());
                            }
                        });

                    } else {
                        Log.e(TAG, "Error fetching user likes.", task.getException());
                    }
                });
    }

    @Override
    public void onBackPressed() {
        Log.d("MyActivity", "onBackPressed is called");
        super.onBackPressed();
        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();
        fetchDataFromFirestore();
    }
}

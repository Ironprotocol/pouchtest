package com.example.pouch;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private static final String TAG = "MainActivity";
    private ProductAdapter adapter;
    private Button currentSelectedButton;



    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        Button skincareButton = findViewById(R.id.button7);
        Button cleansingButton = findViewById(R.id.button8);
        Button lipsButton = findViewById(R.id.button9);
        Button suncareButton = findViewById(R.id.button10);
        ImageButton btnMyAccount = findViewById(R.id.btn_myaccount);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.searching);
        List<Product> products = new ArrayList<>();
        adapter = new ProductAdapter(products);
        recyclerView.setAdapter(adapter);
        currentSelectedButton = skincareButton;


        adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                Intent intent = new Intent(MainActivity.this, ProductDetails.class);
                intent.putExtra("ProductName", product.getProductName());
                startActivity(intent);
            }
        });



        View.OnClickListener onClickListener = new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                currentSelectedButton.setBackgroundResource(R.drawable.button_states);
                currentSelectedButton.setTextColor(ContextCompat.getColorStateList(MainActivity.this, R.drawable.button_textcolor_states).getDefaultColor());
                currentSelectedButton = (Button) view;
                currentSelectedButton.setBackgroundResource(R.drawable.selected_button_background);
                currentSelectedButton.setTextColor(ContextCompat.getColor(MainActivity.this, R.drawable.selected_button_text_color));
                String category = currentSelectedButton.getText().toString().toLowerCase();
                loadProducts(category);
            }
        };

        skincareButton.setOnClickListener(onClickListener);
        cleansingButton.setOnClickListener(onClickListener);
        lipsButton.setOnClickListener(onClickListener);
        suncareButton.setOnClickListener(onClickListener);
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchProducts(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        loadProducts("skincare");
        skincareButton.setBackgroundResource(R.drawable.selected_button_background);
        skincareButton.setTextColor(ContextCompat.getColor(MainActivity.this, R.drawable.selected_button_text_color));


        btnMyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MyaccountActivity.class);
                startActivity(intent);
            }
        });
    }
    private void getProducts(String searchQuery, boolean isSearch) {
        db.collection("Product")
                .whereArrayContains("searchKeywords", searchQuery)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Product> products = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Long> ingredients = (Map<String, Long>) document.get("Ingredients");
                                String maxIngredient = Collections.max(ingredients.entrySet(), Map.Entry.comparingByValue()).getKey();

                                String replacement;
                                switch (maxIngredient) {
                                    case "A":
                                        replacement = "aaa";
                                        break;
                                    case "B":
                                        replacement = "bbb";
                                        break;
                                    case "C":
                                        replacement = "ccc";
                                        break;
                                    case "D":
                                        replacement = "ddd";
                                        break;
                                    default:
                                        replacement = "unknown";
                                }

                                Product product = new Product(
                                        document.getString("Company"),
                                        document.getString("ProductName"),
                                        replacement,
                                        document.getString("Image"),
                                        ingredients,
                                        document.getString("Category"),
                                        (List<String>) document.get("searchKeywords"),
                                        document.getId(),
                                        document.getLong("Like").intValue()
                                );
                                products.add(product);
                            }
                            adapter.updateProducts(products);
                            adapter.notifyDataSetChanged();
                            if (isSearch) {
                                Log.d(TAG, "Searched products loaded.");
                            } else {
                                Log.d(TAG, "Default products loaded.");
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void loadProducts(String searchQuery) {
        getProducts(searchQuery, false);
    }
    private void searchProducts(String query) {
        getProducts(query.toLowerCase(), true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 현재 선택된 카테고리의 상품을 다시 로드합니다.
        String category = currentSelectedButton.getText().toString().toLowerCase();
        loadProducts(category);
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Activity")
                .setMessage("Are you sure you want to close this App?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
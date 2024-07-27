package com.example.pouch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private static final String TAG = "MainActivity";
    private ProductAdapter adapter;
    private Button currentSelectedButton;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;

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
        ImageButton btnFakeSearching = findViewById(R.id.btn_fakesearching);
        ImageView instaIcon = findViewById(R.id.insta_icon);
        ImageView questionIcon = findViewById(R.id.question_icon);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<Product> products = new ArrayList<>();
        adapter = new ProductAdapter(products);
        recyclerView.setAdapter(adapter);
        currentSelectedButton = skincareButton;

        adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {

                Bundle clickBundle = new Bundle();
                clickBundle.putString("product_name", product.getProductName());
                FirebaseAnalytics.getInstance(MainActivity.this).logEvent("product_click", clickBundle);

                DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                database.child("product_clicks").push().setValue(product.getProductName());

                Intent intent = new Intent(MainActivity.this, ProductDetails.class);
                intent.putExtra("ProductName", product.getProductName());
                startActivity(intent);

            }
        });

        List<Integer> imageResources = Arrays.asList(R.drawable.add1, R.drawable.add2, R.drawable.add3);
        ViewPager2 viewPager = findViewById(R.id.AdView);
        BannerAdapter bannerAdapter = new BannerAdapter(imageResources);
        viewPager.setAdapter(bannerAdapter);

        int initialPosition = Integer.MAX_VALUE / 2;
        initialPosition = initialPosition - (initialPosition % imageResources.size());
        viewPager.setCurrentItem(initialPosition);


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
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                sliderHandler.postDelayed(this, 5000);
            }
        };

        questionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://digitalcentury.imweb.me/Notice/?q=YToxOntzOjEyOiJrZXl3b3JkX3R5cGUiO3M6MzoiYWxsIjt9&board=b202308067d429c4339332&bmode=write&back_url=L05vdGljZQ%3D%3D";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        instaIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://instagram.com/ironmasks90?igshid=MzMyNGUyNmU2YQ==";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        btnFakeSearching.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
            }
        });

        skincareButton.setOnClickListener(onClickListener);
        cleansingButton.setOnClickListener(onClickListener);
        lipsButton.setOnClickListener(onClickListener);
        suncareButton.setOnClickListener(onClickListener);

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

                            Collections.sort(products, (product1, product2) -> {
                                if (product1.getLike() == product2.getLike()) {
                                    return product1.getProductName().compareTo(product2.getProductName());
                                }
                                return Integer.compare(product2.getLike(), product1.getLike()); // 내림차순 정렬
                            });
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

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, query);
        FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.SEARCH, bundle);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child("searches").push().setValue(query);

        getProducts(query.toLowerCase(), true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasWindowFocus()) {
            Log.d("MainActivity", "Activity does not have window focus");
        }// 현재 선택된 카테고리의 상품을 다시 로드합니다.
        String category = currentSelectedButton.getText().toString().toLowerCase();
        loadProducts(category);
        sliderHandler.postDelayed(sliderRunnable, 3000);

    }
    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }


    @Override
    public void onBackPressed() {
        hideKeyboard();
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
        Log.d("MyActivity", "onBackPressed is called");
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.alert_dark_frame)
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
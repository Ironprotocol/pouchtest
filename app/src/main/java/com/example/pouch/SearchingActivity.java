package com.example.pouch;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import androidx.appcompat.widget.SearchView;

public class SearchingActivity extends AppCompatActivity {
    private RecyclerView recyclerView1;
    private FirebaseFirestore db;
    private ProductAdapter adapter;
    private static final String TAG = "SearchingActivity";
    private SearchView searchView;
    private static final int PAGE_SIZE = 20;
    private DocumentSnapshot lastVisible;
    private boolean isLoading = false;
    private String currentQuery = "";
    private TextView noproducttext;
    private RelatedSearchAdapter relatedSearchAdapter;
    private List<String> relatedSearchList = new ArrayList<>();
    private RecyclerView relatedSearchRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchingpage);
        noproducttext = findViewById(R.id.noproduct_text);
        db = FirebaseFirestore.getInstance();
        recyclerView1 = findViewById(R.id.recyclerView1);
        recyclerView1.setLayoutManager(new LinearLayoutManager(this));
        List<Product> products = new ArrayList<>();
        adapter = new ProductAdapter(products);
        recyclerView1.setAdapter(adapter);
        searchView = findViewById(R.id.searching2);



        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setHintTextColor(Color.WHITE);
        recyclerView1.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == adapter.getItemCount() - 1) {
                    loadMoreProducts(searchView.getQuery().toString());
                    isLoading = true;
                }
            }
        });


        adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                // 여기에서 ProductDetails 액티비티로 이동하는 인텐트를 설정합니다.
                Intent intent = new Intent(SearchingActivity.this, ProductDetails.class);
                intent.putExtra("selectedProduct", product);  // 상품 객체를 인텐트에 추가 (Product 클래스는 Serializable 또는 Parcelable 인터페이스를 구현해야 함)
                startActivity(intent);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadMoreProducts(query);
                relatedSearchRecyclerView.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateRelatedSearchList(newText);
                relatedSearchAdapter.notifyDataSetChanged();
                updateRelatedSearchRecyclerViewVisibility(newText);
                return false;
            }
            private void updateRelatedSearchList(String query) {
                Log.d(TAG, "updateRelatedSearchList is called with query: " + query);
                relatedSearchList.clear();

                if (!query.isEmpty()) {
                    db.collection("Product")
                            .whereArrayContains("searchKeywords", query.toLowerCase())
                            .limit(10)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Query result size: " + task.getResult().size());
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                            String productName = document.getString("ProductName");
                                            if (productName != null) {
                                                relatedSearchList.add(productName);
                                                Log.d(TAG, "Added to related search list: " + productName);
                                            } else {
                                                Log.d(TAG, "ProductName is null");
                                            }
                                        }
                                        relatedSearchAdapter.notifyDataSetChanged();
                                    } else {
                                        Log.w(TAG, "Error getting documents.", task.getException());
                                    }
                                    updateRelatedSearchRecyclerViewVisibility(query);
                                }
                            });
                }
            }





            private void updateRelatedSearchRecyclerViewVisibility(String query) {
                Log.d(TAG, "updateRelatedSearchRecyclerViewVisibility is called with query: " + query);
                if (query.isEmpty() || relatedSearchList.isEmpty()) {
                    relatedSearchRecyclerView.setVisibility(View.GONE);
                } else {
                    relatedSearchRecyclerView.setVisibility(View.VISIBLE);
                }
            }

        });
        relatedSearchRecyclerView = findViewById(R.id.relatedSearchRecyclerView);
        relatedSearchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        relatedSearchAdapter = new RelatedSearchAdapter(relatedSearchList);
        relatedSearchRecyclerView.setAdapter(relatedSearchAdapter);

        relatedSearchAdapter.setOnItemClickListener(new RelatedSearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String searchQuery) {
                searchView.setQuery(searchQuery, true);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void loadMoreProducts(String query) {
        if (isLoading) return;

        String lowercaseQuery = query.toLowerCase();

        Query nextQuery = db.collection("Product")
                .whereArrayContains("searchKeywords", lowercaseQuery)
                .orderBy("ProductName")
                .limit(PAGE_SIZE);

        if (!query.equals(currentQuery)) {
            lastVisible = null;
            adapter.clearProducts();
        }
        currentQuery = query;

        if (lastVisible != null) {
            nextQuery = nextQuery.startAfter(lastVisible);
        }

        isLoading = true;

        nextQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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

                    if (!task.getResult().isEmpty()) {
                        lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                        adapter.addProducts(products);
                        adapter.notifyDataSetChanged();

                        noproducttext.setVisibility(View.GONE);
                    } else if(adapter.getItemCount() == 0) {
                        noproducttext.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
                isLoading = false;
            }
        });

    }
}
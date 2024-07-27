package com.example.pouch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;
import java.util.Map;

public class ProductDetails extends AppCompatActivity {
    private static final String TAG = "ProductDetails";
    private Product product;

    @Override
    public void onBackPressed() {
        Log.d("MyActivity", "onBackPressed is called");
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        ImageView productImage = findViewById(R.id.productImage_details);
        TextView company = findViewById(R.id.company_details);
        TextView productName = findViewById(R.id.productName_details);
        TextView category = findViewById(R.id.category_details);
        TextView ingredients = findViewById(R.id.ingredients_details);
        ImageButton btn_like = findViewById(R.id.btn_like_details);
        TextView like_amount = findViewById(R.id.like_amount_details);

        Intent intent = getIntent();
        String productNameStr = intent.getStringExtra("ProductName");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Product").whereEqualTo("ProductName", productNameStr).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                product = document.toObject(Product.class);
                                product.setProductId(document.getId());
                                company.setText(product.getCompany());
                                productName.setText(product.getProductName());
                                category.setText(product.getCategory());

                                Map<String, Long> ingredientsMap = product.getIngredients();
                                if (ingredientsMap != null && !ingredientsMap.isEmpty()) {
                                    String maxIngredient = Collections.max(ingredientsMap.entrySet(), Map.Entry.comparingByValue()).getKey();
                                    String replacement = getReplacementForIngredient(maxIngredient);
                                    ingredients.setText(replacement);
                                    checkIngredientsAndUpdateIcons(ingredientsMap);
                                } else {
                                    ingredients.setText("N/A");
                                }

                                like_amount.setText(String.valueOf(product.getLike()));

                                Glide.with(ProductDetails.this)
                                        .load(product.getImage())
                                        .into(productImage);

                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                String userLikeId = userId + "_" + product.getProductId();
                                db.collection("UserLikes").document(userLikeId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                btn_like.setImageResource(R.drawable.after_likebtn_selected);
                                                btn_like.setTag("Default");
                                            } else {
                                                btn_like.setImageResource(R.drawable.before_likebtn_selected);
                                                btn_like.setTag("Like");
                                            }
                                        } else {
                                            Log.w(TAG, "Error getting documents.", task.getException());
                                        }
                                    }
                                });
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

        btn_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String userLikeId = userId + "_" + product.getProductId();
                if (btn_like.getTag().equals("Like")) {
                    db.collection("UserLikes").document(userLikeId)
                            .set(new UserLike(userId, product.getProductId()))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    btn_like.setImageResource(R.drawable.after_likebtn_selected);
                                    btn_like.setTag("Default");
                                    int newLikeAmount = Integer.parseInt(like_amount.getText().toString()) + 1;
                                    like_amount.setText(String.valueOf(newLikeAmount));
                                    db.collection("Product").document(product.getProductId())
                                            .update("Like", newLikeAmount);
                                }
                            });
                } else {
                    db.collection("UserLikes").document(userLikeId)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    btn_like.setImageResource(R.drawable.before_likebtn_selected);
                                    btn_like.setTag("Like");
                                    int newLikeAmount = Integer.parseInt(like_amount.getText().toString()) - 1;
                                    like_amount.setText(String.valueOf(newLikeAmount));
                                    db.collection("Product").document(product.getProductId())
                                            .update("Like", newLikeAmount);
                                }
                            });
                }
            }
        });
    }

    private String getReplacementForIngredient(String ingredient) {
        switch (ingredient) {
            case "A":
                return "aaa";
            case "B":
                return "bbb";
            case "C":
                return "ccc";
            case "D":
                return "ddd";
            default:
                return "";
        }
    }

    private void checkIngredientsAndUpdateIcons(Map<String, Long> ingredientsMap) {
        for (String ingredient : ingredientsMap.keySet()) {
            int textViewId = findViewIdByText(ingredient);
            if (textViewId != 0) {
                String imageViewIdName = getResources().getResourceEntryName(textViewId) + "_checked";
                int imageViewId = getResources().getIdentifier(imageViewIdName, "id", getPackageName());
                ImageView ingredientImageView = findViewById(imageViewId);
                if (ingredientImageView != null) {
                    ingredientImageView.setImageResource(R.drawable.check_icon);
                }
            }
        }
    }

    private int findViewIdByText(String text) {
        int id = 0;
        int[] allTextViewIDs = {R.id.ing_1_1, R.id.ing_1_2};
        //, R.id.ing_1_3, R.id.ing_1_4 이 두개 제거되어있는 상태
        for (int textViewId : allTextViewIDs) {
            TextView textView = findViewById(textViewId);
            if (textView != null && textView.getText().toString().equals(text)) {
                id = textViewId;
                break;
            }
        }
        return id;
    }
}

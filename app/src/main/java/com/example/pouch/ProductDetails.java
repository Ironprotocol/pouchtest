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

public class ProductDetails extends AppCompatActivity {
    private static final String TAG = "ProductDetails";
    private Product product;

    @Override
    public void onBackPressed() {
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
                                product = document.toObject(Product.class);  // Assign the Product instance to the field
                                product.setProductId(document.getId());  // set the ProductId field
                                company.setText(product.getCompany());
                                productName.setText(product.getProductName());
                                category.setText(product.getCategory());
                                ingredients.setText(product.getReplacement());
                                like_amount.setText(String.valueOf(product.getLike()));

                                Glide.with(ProductDetails.this)
                                        .load(product.getImage())
                                        .into(productImage);

                                // Check if the user has liked this product
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
                    // If the user has not liked this product yet, add it to the UserLikes collection
                    db.collection("UserLikes").document(userLikeId)
                            .set(new UserLike(userId, product.getProductId()))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    btn_like.setImageResource(R.drawable.after_likebtn_selected);
                                    btn_like.setTag("Default");
                                    int newLikeAmount = Integer.parseInt(like_amount.getText().toString()) + 1;
                                    like_amount.setText(String.valueOf(newLikeAmount));
                                    // Update the like count in the Product collection
                                    db.collection("Product").document(product.getProductId())
                                            .update("Like", newLikeAmount);
                                }
                            });
                } else {
                    // If the user has already liked this product, remove it from the UserLikes collection
                    db.collection("UserLikes").document(userLikeId)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    btn_like.setImageResource(R.drawable.before_likebtn_selected);
                                    btn_like.setTag("Like");
                                    int newLikeAmount = Integer.parseInt(like_amount.getText().toString()) - 1;
                                    like_amount.setText(String.valueOf(newLikeAmount));
                                    // Update the like count in the Product collection
                                    db.collection("Product").document(product.getProductId())
                                            .update("Like", newLikeAmount);
                                }
                            });
                }
            }
        });
    }
}

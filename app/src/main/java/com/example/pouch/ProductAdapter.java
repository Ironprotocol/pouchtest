package com.example.pouch;
import androidx.annotation.NonNull;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    public static final String TAG = "ProductAdapter";
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        public TextView companyView;
        public TextView productNameView;
        public TextView ingredientsView;
        public TextView categoryView;
        public ImageView productImageView;
        public ImageButton btn_like;
        public TextView like_amount;

        public ProductViewHolder(View v){
            super(v);
            companyView = v.findViewById(R.id.company);
            productNameView = v.findViewById(R.id.productName);
            ingredientsView = v.findViewById(R.id.ingredients);
            productImageView = v.findViewById(R.id.productImage);
            categoryView = v.findViewById(R.id.category);
            btn_like = v.findViewById(R.id.btn_like);
            like_amount = v.findViewById(R.id.like_amount);
        }
    }

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }
    public void setProducts(List<Product> products) {
        this.productList = products;
    }

    @Override
    public ProductAdapter.ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_product_item, parent, false);
        ProductViewHolder pvh = new ProductViewHolder(v);
        return pvh;
    }


    @Override
    public void onBindViewHolder(final ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.companyView.setText(product.getCompany());
        holder.productNameView.setText(product.getProductName());
        holder.ingredientsView.setText(product.getReplacement());
        holder.categoryView.setText(product.getCategory());
        holder.like_amount.setText(String.valueOf(product.getLike()));

        Log.d(TAG, "onBindViewHolder called for position " + position);
        String imageUrl = product.getImage();
        Log.d(TAG, "Image URL: " + imageUrl);

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e(TAG, "Load failed", e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(holder.productImageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int position = holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(productList.get(position));
                    }
                }
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();  // Get the current user's ID
        String userLikeId = userId + "_" + product.getProductId();  // Create the UserLike ID

        // 🟠 Check if the user has already liked the product
        db.collection("UserLikes").document(userLikeId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // 🟠 If the user has already liked the product, set the like button to the "liked" state
                        holder.btn_like.setTag("Default");
                        holder.btn_like.setImageResource(R.drawable.after_likebtn_selected);
                    } else {
                        // 🟠 If the user has not liked the product, set the like button to the "not liked" state
                        holder.btn_like.setTag("Like");
                        holder.btn_like.setImageResource(R.drawable.before_likebtn_selected);
                    }
                }
            }
        });

        holder.btn_like.setOnClickListener(new View.OnClickListener() {
            private long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                if (now - lastClickTime < 1000) {
                    // If clicks are too close together in time, ignore this click
                    return;
                }
                lastClickTime = now;
                // Disable the button to prevent additional clicks while processing
                holder.btn_like.setEnabled(false);

                db.collection("UserLikes").document(userLikeId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        db.collection("UserLikes").document(userLikeId).delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                                        holder.btn_like.setTag("Like");
                                                        holder.btn_like.setImageResource(R.drawable.before_likebtn_selected);
                                                        db.collection("Product").document(product.getProductId())
                                                                .update("Like", FieldValue.increment(-1));
                                                        holder.btn_like.setEnabled(true);

                                                        // 🟠 Update the like count immediately in the local product data
                                                        product.setLike(product.getLike() - 1);
                                                        holder.like_amount.setText(String.valueOf(product.getLike()));
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error deleting document", e);
                                                        holder.btn_like.setEnabled(true);
                                                    }
                                                });
                                    } else {
                                        Map<String, String> userLike = new HashMap<>();
                                        userLike.put("userId", userId);
                                        userLike.put("productId", product.getProductId());
                                        db.collection("UserLikes").document(userLikeId).set(userLike)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                                        holder.btn_like.setTag("Default");
                                                        holder.btn_like.setImageResource(R.drawable.after_likebtn_selected);
                                                        db.collection("Product").document(product.getProductId())
                                                                .update("Like", FieldValue.increment(1));
                                                        holder.btn_like.setEnabled(true);

                                                        // 🟠 Update the like count immediately in the local product data
                                                        product.setLike(product.getLike() + 1);
                                                        holder.like_amount.setText(String.valueOf(product.getLike()));
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error writing document", e);
                                                        holder.btn_like.setEnabled(true);
                                                    }
                                                });
                                    }
                                } else {
                                    Log.w(TAG, "get failed with ", task.getException());
                                    holder.btn_like.setEnabled(true);
                                }
                            }
                        });
            }
        });
    }



    public void updateProducts(List<Product> newProducts) {
        this.productList = newProducts;
        notifyDataSetChanged();
        Log.d("ProductAdapter", "updateProducts called with " + newProducts.size() + " products.");

    }
    @Override
    public int getItemCount() {
        return productList.size();
    }



}
package com.example.pouch;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAccountProductAdapter extends RecyclerView.Adapter<MyAccountProductAdapter.ProductViewHolder> {
    private List<Product> productList = new ArrayList<>();
    public static final String TAG = "MyAccountProductAdapter";
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
        public long lastClickTime;

        public ProductViewHolder(View v) {
            super(v);
            companyView = v.findViewById(R.id.company_myaccount);
            productNameView = v.findViewById(R.id.productName_myaccount);
            ingredientsView = v.findViewById(R.id.ingredients_myaccount);
            productImageView = v.findViewById(R.id.productImage_myaccount);
            categoryView = v.findViewById(R.id.category_myaccount);
            btn_like = v.findViewById(R.id.btn_like_myaccount);
            like_amount = v.findViewById(R.id.like_amount_myaccount);
            lastClickTime = 0;
        }
    }

    public MyAccountProductAdapter() {
        fetchDataFromFirestore();
    }

    private void fetchDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("UserLikes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> likedProductIds = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            likedProductIds.add(document.getString("productId"));
                        }

                        db.collection("Product")
                                .whereIn("productId", likedProductIds)
                                .get()
                                .addOnCompleteListener(productTask -> {
                                    if (productTask.isSuccessful()) {
                                        productList = productTask.getResult().toObjects(Product.class);
                                        notifyDataSetChanged();
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
    public MyAccountProductAdapter.ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.myaccount_product_details, parent, false);
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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int positionInside = holder.getAdapterPosition();
                if (positionInside != RecyclerView.NO_POSITION) {
                    listener.onItemClick(productList.get(positionInside));
                }
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userLikeId = userId + "_" + product.getProductId();

        db.collection("UserLikes").document(userLikeId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    holder.btn_like.setTag("Default");
                    holder.btn_like.setImageResource(R.drawable.after_likebtn_selected);
                } else {
                    holder.btn_like.setTag("Like");
                    holder.btn_like.setImageResource(R.drawable.before_likebtn_selected);
                }
            }
        });

        holder.btn_like.setOnClickListener(v -> {
            long now = System.currentTimeMillis();
            if (now - holder.lastClickTime < 1000) {
                return;
            }
            holder.lastClickTime = now;
            holder.btn_like.setEnabled(false);

            db.collection("UserLikes").document(userLikeId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                db.collection("UserLikes").document(userLikeId).delete()
                                        .addOnSuccessListener(aVoid -> {
                                            holder.btn_like.setTag("Like");
                                            holder.btn_like.setImageResource(R.drawable.before_likebtn_selected);
                                            db.collection("Product").document(product.getProductId())
                                                    .update("Like", FieldValue.increment(-1));
                                            holder.btn_like.setEnabled(true);
                                            product.setLike(product.getLike() - 1);
                                            holder.like_amount.setText(String.valueOf(product.getLike()));
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w(TAG, "Error deleting document", e);
                                            holder.btn_like.setEnabled(true);
                                        });
                            } else {
                                Map<String, String> userLike = new HashMap<>();
                                userLike.put("userId", userId);
                                userLike.put("productId", product.getProductId());
                                db.collection("UserLikes").document(userLikeId).set(userLike)
                                        .addOnSuccessListener(aVoid -> {
                                            holder.btn_like.setTag("Default");
                                            holder.btn_like.setImageResource(R.drawable.after_likebtn_selected);
                                            db.collection("Product").document(product.getProductId())
                                                    .update("Like", FieldValue.increment(1));
                                            holder.btn_like.setEnabled(true);
                                            product.setLike(product.getLike() + 1);
                                            holder.like_amount.setText(String.valueOf(product.getLike()));
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w(TAG, "Error writing document", e);
                                            holder.btn_like.setEnabled(true);
                                        });
                            }
                        } else {
                            Log.w(TAG, "get failed with ", task.getException());
                            holder.btn_like.setEnabled(true);
                        }
                    });
        });
    }

    public void updateProducts(List<Product> newProducts) {
        this.productList = newProducts;
        notifyDataSetChanged();
        Log.d(TAG, "updateProducts called with " + newProducts.size() + " products.");
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}

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

    // 인터페이스 정의
    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    // 리스너 설정 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // ViewHolder 정의
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        public TextView companyView;
        public TextView productNameView;
        public TextView ingredientsView;
        public TextView categoryView;
        public ImageView productImageView;
        public TextView like_amount;
        public long lastClickTime;

        public ProductViewHolder(View v) {
            super(v);
            companyView = v.findViewById(R.id.company_myaccount);
            productNameView = v.findViewById(R.id.productName_myaccount);
            ingredientsView = v.findViewById(R.id.ingredients_myaccount);
            productImageView = v.findViewById(R.id.productImage_myaccount);
            categoryView = v.findViewById(R.id.category_myaccount);
            like_amount = v.findViewById(R.id.like_amount_myaccount);
            lastClickTime = 0;
        }
    }

    // 어댑터 생성자
    public MyAccountProductAdapter() {
    }

    // ViewHolder 생성
    @Override
    public MyAccountProductAdapter.ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.myaccount_product_details, parent, false);
        return new ProductViewHolder(v);
    }

    // ViewHolder 바인딩
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

    }

    // 상품 리스트 설정
    public void setProducts(List<Product> products) {
        this.productList = products;
        notifyDataSetChanged();
    }

    // 아이템 개수 반환
    @Override
    public int getItemCount() {
        return productList.size();
    }
}

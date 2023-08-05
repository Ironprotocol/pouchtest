package com.example.pouch;

public class UserLike {
    private String userId;
    private String productId;

    public UserLike() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public UserLike(String userId, String productId) {
        this.userId = userId;
        this.productId = productId;
    }

    public String getUserId() {
        return userId;
    }

    public String getProductId() {
        return productId;
    }
}

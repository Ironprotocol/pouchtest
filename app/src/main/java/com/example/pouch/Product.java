package com.example.pouch;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Product implements Serializable {
    private String Company;
    private String ProductName;
    private Map<String, Long> Ingredients;
    private String Image;
    private String Category;
    private String Replacement;
    private List<String> searchKeywords;
    private String ProductId;
    private int Like;

    public Product() {}

    public Product(String Company, String ProductName, String Replacement, String Image,
                   Map<String, Long> Ingredients, String Category,
                   List<String> searchKeywords, String ProductId, int Like) {
        this.Company = Company;
        this.ProductName = ProductName;
        this.Replacement = Replacement;
        this.Image = Image;
        this.Ingredients = Ingredients;
        this.Category = Category;
        this.searchKeywords = searchKeywords;
        this.ProductId = ProductId;
        this.Like = Like;
    }

    public void setProductId(String productId) {
        this.ProductId = productId;
    }

    public String getImage() {return Image;}
    public String getCompany() {
        return Company;
    }
    public String getProductName() {
        return ProductName;
    }
    public Map<String, Long> getIngredients() {
        return Ingredients;
    }
    public String getCategory() {return Category;}
    public String getReplacement() {return Replacement;}
    public List<String> getSearchKeywords() {return searchKeywords;}
    public String getProductId() { return ProductId; }
    public int getLike() { return Like; }
    public void setLike(int Like) {this.Like = Like;}
    public void setImage(String image) { this.Image = image; }
}

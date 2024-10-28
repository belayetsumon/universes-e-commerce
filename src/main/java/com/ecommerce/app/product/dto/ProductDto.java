package com.ecommerce.app.product.dto;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.product.model.ProductTypeEnum;
import com.ecommerce.app.product.model.Productcategory;

/**
 *
 * @author libertyerp_local
 */
public class ProductDto {
    private Long id;
    private int sku;
    private Users userId;
    private Productcategory productcategory;
    private String title;
    private String slug;
    private int orderno;
    private String shortDescription;
    private String description;
    private String video;
    private double price;
    private double buyPrice;
    private double salePrice;
    private double companyProfit;
    private ProductTypeEnum productType;
    private double companyDiscount;
    private double discount;
    private String uom;
    private String imageName;

    public ProductDto() {
    }

    public ProductDto(Long id, int sku, Users userId, Productcategory productcategory, String title, String slug, int orderno, String shortDescription, String description, String video, double price, double buyPrice, double salePrice, double companyProfit, ProductTypeEnum productType, double companyDiscount, double discount, String uom, String imageName) {
        this.id = id;
        this.sku = sku;
        this.userId = userId;
        this.productcategory = productcategory;
        this.title = title;
        this.slug = slug;
        this.orderno = orderno;
        this.shortDescription = shortDescription;
        this.description = description;
        this.video = video;
        this.price = price;
        this.buyPrice = buyPrice;
        this.salePrice = salePrice;
        this.companyProfit = companyProfit;
        this.productType = productType;
        this.companyDiscount = companyDiscount;
        this.discount = discount;
        this.uom = uom;
        this.imageName = imageName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSku() {
        return sku;
    }

    public void setSku(int sku) {
        this.sku = sku;
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
    }

    public Productcategory getProductcategory() {
        return productcategory;
    }

    public void setProductcategory(Productcategory productcategory) {
        this.productcategory = productcategory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public int getOrderno() {
        return orderno;
    }

    public void setOrderno(int orderno) {
        this.orderno = orderno;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }

    public double getCompanyProfit() {
        return companyProfit;
    }

    public void setCompanyProfit(double companyProfit) {
        this.companyProfit = companyProfit;
    }

    public ProductTypeEnum getProductType() {
        return productType;
    }

    public void setProductType(ProductTypeEnum productType) {
        this.productType = productType;
    }

    public double getCompanyDiscount() {
        return companyDiscount;
    }

    public void setCompanyDiscount(double companyDiscount) {
        this.companyDiscount = companyDiscount;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}

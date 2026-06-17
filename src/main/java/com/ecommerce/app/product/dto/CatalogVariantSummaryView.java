package com.ecommerce.app.product.dto;

import com.ecommerce.app.product.model.ProductStatusEnum;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 2026-05-15: Read model for generic product variants.
 */
public class CatalogVariantSummaryView {

    private String uuid;
    private String sku;
    private String barcode;
    private BigDecimal sellingPrice;
    private BigDecimal specialPrice;
    private BigDecimal stockQuantity;
    private BigDecimal reservedQuantity;
    private BigDecimal soldQuantity;
    private Boolean active;
    private ProductStatusEnum status;
    private String optionSummary;
    private List<CatalogVariantOptionView> options = new ArrayList<>();

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public BigDecimal getSpecialPrice() {
        return specialPrice;
    }

    public void setSpecialPrice(BigDecimal specialPrice) {
        this.specialPrice = specialPrice;
    }

    public BigDecimal getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(BigDecimal stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public BigDecimal getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(BigDecimal reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public BigDecimal getSoldQuantity() {
        return soldQuantity;
    }

    public void setSoldQuantity(BigDecimal soldQuantity) {
        this.soldQuantity = soldQuantity;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public ProductStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ProductStatusEnum status) {
        this.status = status;
    }

    public String getOptionSummary() {
        return optionSummary;
    }

    public void setOptionSummary(String optionSummary) {
        this.optionSummary = optionSummary;
    }

    public List<CatalogVariantOptionView> getOptions() {
        return options;
    }

    public void setOptions(List<CatalogVariantOptionView> options) {
        this.options = options;
    }
}

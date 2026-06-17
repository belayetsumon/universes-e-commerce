/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ecommerce.app.module.shipping.dto;

import java.math.BigDecimal;
import com.ecommerce.app.module.shipping.model.CodCollectionMode;
import com.ecommerce.app.module.shipping.model.DeliverySpeed;
import com.ecommerce.app.module.shipping.model.DeliveryType;
import com.ecommerce.app.module.shipping.model.CarrierMode;
import com.ecommerce.app.module.shipping.model.SettlementMode;
import com.ecommerce.app.module.shipping.model.ShippingChargeOwner;

/**
 *
 * @author libertyerp_local
 */
public class ShippingOption {

    private String code;
    private String title;
    private BigDecimal price;
    private String estimatedDelivery;
    private String carrierCode;
    private String carrierName;
    private String rateUuid;
    private DeliverySpeed speed;
    private DeliveryType deliveryType;
    private BigDecimal codFee;
    private boolean codAvailable;
    private CarrierMode carrierMode;
    private SettlementMode settlementMode;
    private ShippingChargeOwner shippingChargeOwner;
    private CodCollectionMode codCollectionMode;

    public ShippingOption(String code, String title, BigDecimal price, String estimatedDelivery, String carrierCode) {
        this.code = code;
        this.title = title;
        this.price = price;
        this.estimatedDelivery = estimatedDelivery;
        this.carrierCode = carrierCode;
    }

    public ShippingOption() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(String estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }

    public String getCarrierCode() {
        return carrierCode;
    }

    public void setCarrierCode(String carrierCode) {
        this.carrierCode = carrierCode;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public String getRateUuid() {
        return rateUuid;
    }

    public void setRateUuid(String rateUuid) {
        this.rateUuid = rateUuid;
    }

    public DeliverySpeed getSpeed() {
        return speed;
    }

    public void setSpeed(DeliverySpeed speed) {
        this.speed = speed;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public BigDecimal getCodFee() {
        return codFee;
    }

    public void setCodFee(BigDecimal codFee) {
        this.codFee = codFee;
    }

    public boolean isCodAvailable() {
        return codAvailable;
    }

    public void setCodAvailable(boolean codAvailable) {
        this.codAvailable = codAvailable;
    }

    public CarrierMode getCarrierMode() {
        return carrierMode;
    }

    public void setCarrierMode(CarrierMode carrierMode) {
        this.carrierMode = carrierMode;
    }

    public SettlementMode getSettlementMode() {
        return settlementMode;
    }

    public void setSettlementMode(SettlementMode settlementMode) {
        this.settlementMode = settlementMode;
    }

    public ShippingChargeOwner getShippingChargeOwner() {
        return shippingChargeOwner;
    }

    public void setShippingChargeOwner(ShippingChargeOwner shippingChargeOwner) {
        this.shippingChargeOwner = shippingChargeOwner;
    }

    public CodCollectionMode getCodCollectionMode() {
        return codCollectionMode;
    }

    public void setCodCollectionMode(CodCollectionMode codCollectionMode) {
        this.codCollectionMode = codCollectionMode;
    }

}

package com.ecommerce.app.product.services;

import com.ecommerce.app.module.order.model.OrderItem;
import com.ecommerce.app.module.order.model.SalesOrder;
import com.ecommerce.app.module.order.repository.OrderItemRepository;
import com.ecommerce.app.module.order.repository.SalesOrderRepository;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.model.ProductVariant;
import com.ecommerce.app.product.model.StockBucketEnum;
import com.ecommerce.app.product.model.StockTransaction;
import com.ecommerce.app.product.model.StockTransactionTypeEnum;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.product.ripository.ProductVariantRepository;
import com.ecommerce.app.product.ripository.StockTransactionRepository;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockLedgerService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    public BigDecimal getAvailableQuantity(Long productId, String catalogVariantUuid) {
        StockTarget target = getStockTarget(productId, catalogVariantUuid, false);
        return target.available;
    }

    @Transactional
    public StockTransaction receiveStock(
            Long productId,
            BigDecimal quantity,
            String idempotencyKey,
            String note
    ) {
        return receiveStock(productId, null, quantity, idempotencyKey, note);
    }

    @Transactional
    public StockTransaction receiveStock(
            Long productId,
            String catalogVariantUuid,
            BigDecimal quantity,
            String idempotencyKey,
            String note
    ) {
        validateQuantity(quantity);

        StockTransaction existing = getExisting(idempotencyKey);
        if (existing != null) {
            return existing;
        }

        StockTarget target = getStockTarget(productId, catalogVariantUuid, true);
        target.available = target.available.add(quantity);
        persistStockTarget(target);

        return saveTransaction(
                target,
                null,
                StockBucketEnum.AVAILABLE,
                StockTransactionTypeEnum.RECEIVE,
                quantity,
                null,
                null,
                idempotencyKey,
                note
        );
    }

    @Transactional
    public StockTransaction reserveStock(
            Long productId,
            BigDecimal quantity,
            Long salesOrderId,
            Long orderItemId,
            String idempotencyKey,
            String note
    ) {
        return reserveStock(productId, null, quantity, salesOrderId, orderItemId, idempotencyKey, note);
    }

    @Transactional
    public StockTransaction reserveStock(
            Long productId,
            String catalogVariantUuid,
            BigDecimal quantity,
            Long salesOrderId,
            Long orderItemId,
            String idempotencyKey,
            String note
    ) {
        validateQuantity(quantity);

        StockTransaction existing = getExisting(idempotencyKey);
        if (existing != null) {
            return existing;
        }

        StockTarget target = getStockTarget(productId, catalogVariantUuid, true);

        if (isStockManaged(target)
                && target.available.compareTo(quantity) < 0) {
            throw new IllegalStateException("Not enough available stock");
        }

        target.available = target.available.subtract(quantity);
        target.reserved = target.reserved.add(quantity);
        persistStockTarget(target);

        return saveTransaction(
                target,
                StockBucketEnum.AVAILABLE,
                StockBucketEnum.RESERVED,
                StockTransactionTypeEnum.RESERVE,
                quantity,
                salesOrderId,
                orderItemId,
                idempotencyKey,
                note
        );
    }

    @Transactional
    public StockTransaction releaseReservedStock(
            Long productId,
            BigDecimal quantity,
            Long salesOrderId,
            Long orderItemId,
            String idempotencyKey,
            String note
    ) {
        return releaseReservedStock(productId, null, quantity, salesOrderId, orderItemId, idempotencyKey, note);
    }

    @Transactional
    public StockTransaction releaseReservedStock(
            Long productId,
            String catalogVariantUuid,
            BigDecimal quantity,
            Long salesOrderId,
            Long orderItemId,
            String idempotencyKey,
            String note
    ) {
        validateQuantity(quantity);

        StockTransaction existing = getExisting(idempotencyKey);
        if (existing != null) {
            return existing;
        }

        StockTarget target = getStockTarget(productId, catalogVariantUuid, true);
        if (target.reserved.compareTo(quantity) < 0) {
            throw new IllegalStateException("Not enough reserved stock");
        }

        target.reserved = target.reserved.subtract(quantity);
        target.available = target.available.add(quantity);
        persistStockTarget(target);

        return saveTransaction(
                target,
                StockBucketEnum.RESERVED,
                StockBucketEnum.AVAILABLE,
                StockTransactionTypeEnum.RELEASE,
                quantity,
                salesOrderId,
                orderItemId,
                idempotencyKey,
                note
        );
    }

    @Transactional
    public StockTransaction completeSale(
            Long productId,
            BigDecimal quantity,
            Long salesOrderId,
            Long orderItemId,
            String idempotencyKey,
            String note
    ) {
        return completeSale(productId, null, quantity, salesOrderId, orderItemId, idempotencyKey, note);
    }

    @Transactional
    public StockTransaction completeSale(
            Long productId,
            String catalogVariantUuid,
            BigDecimal quantity,
            Long salesOrderId,
            Long orderItemId,
            String idempotencyKey,
            String note
    ) {
        validateQuantity(quantity);

        StockTransaction existing = getExisting(idempotencyKey);
        if (existing != null) {
            return existing;
        }

        StockTarget target = getStockTarget(productId, catalogVariantUuid, true);
        if (target.reserved.compareTo(quantity) < 0) {
            throw new IllegalStateException("Not enough reserved stock");
        }

        target.reserved = target.reserved.subtract(quantity);
        target.sold = target.sold.add(quantity);
        persistStockTarget(target);

        return saveTransaction(
                target,
                StockBucketEnum.RESERVED,
                StockBucketEnum.SOLD,
                StockTransactionTypeEnum.SALE,
                quantity,
                salesOrderId,
                orderItemId,
                idempotencyKey,
                note
        );
    }

    @Transactional
    public StockTransaction returnSoldStock(
            Long productId,
            BigDecimal quantity,
            Long salesOrderId,
            Long orderItemId,
            String idempotencyKey,
            String note
    ) {
        return returnSoldStock(productId, null, quantity, salesOrderId, orderItemId, idempotencyKey, note);
    }

    @Transactional
    public StockTransaction returnSoldStock(
            Long productId,
            String catalogVariantUuid,
            BigDecimal quantity,
            Long salesOrderId,
            Long orderItemId,
            String idempotencyKey,
            String note
    ) {
        validateQuantity(quantity);

        StockTransaction existing = getExisting(idempotencyKey);
        if (existing != null) {
            return existing;
        }

        StockTarget target = getStockTarget(productId, catalogVariantUuid, true);
        if (target.sold.compareTo(quantity) < 0) {
            throw new IllegalStateException("Not enough sold stock to return");
        }

        target.sold = target.sold.subtract(quantity);
        target.available = target.available.add(quantity);
        persistStockTarget(target);

        return saveTransaction(
                target,
                StockBucketEnum.SOLD,
                StockBucketEnum.AVAILABLE,
                StockTransactionTypeEnum.RETURN_TO_STOCK,
                quantity,
                salesOrderId,
                orderItemId,
                idempotencyKey,
                note
        );
    }

    @Transactional
    public StockTransaction adjustAvailableStock(
            Long productId,
            BigDecimal delta,
            String idempotencyKey,
            String note
    ) {
        return adjustAvailableStock(productId, null, delta, idempotencyKey, note);
    }

    @Transactional
    public StockTransaction adjustAvailableStock(
            Long productId,
            String catalogVariantUuid,
            BigDecimal delta,
            String idempotencyKey,
            String note
    ) {
        if (delta == null || delta.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Adjustment cannot be zero");
        }

        StockTransaction existing = getExisting(idempotencyKey);
        if (existing != null) {
            return existing;
        }

        StockTarget target = getStockTarget(productId, catalogVariantUuid, true);
        StockTransactionTypeEnum type;

        if (delta.compareTo(BigDecimal.ZERO) > 0) {
            target.available = target.available.add(delta);
            type = StockTransactionTypeEnum.ADJUST_IN;
        } else {
            BigDecimal remove = delta.abs();
            if (target.available.compareTo(remove) < 0) {
                throw new IllegalStateException("Insufficient stock");
            }
            target.available = target.available.subtract(remove);
            type = StockTransactionTypeEnum.ADJUST_OUT;
        }

        persistStockTarget(target);

        return saveTransaction(
                target,
                StockBucketEnum.AVAILABLE,
                StockBucketEnum.ADJUSTMENT,
                type,
                delta.abs(),
                null,
                null,
                idempotencyKey,
                note
        );
    }

    private StockTransaction getExisting(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Idempotency key required");
        }

        return stockTransactionRepository.findByIdempotencyKey(key).orElse(null);
    }

    private StockTarget getStockTarget(Long productId, String catalogVariantUuid, boolean forUpdate) {
        if (catalogVariantUuid != null && !catalogVariantUuid.isBlank()) {
            ProductVariant catalogVariant = (forUpdate
                    ? stockTransactionRepository.findCatalogVariantByUuidForUpdate(catalogVariantUuid)
                    : productVariantRepository.findByUuid(catalogVariantUuid))
                    .orElseThrow(() -> new IllegalArgumentException("Catalog variant not found"));

            if (productId != null
                    && (catalogVariant.getProduct() == null
                    || !catalogVariant.getProduct().getId().equals(productId))) {
                throw new IllegalArgumentException("Catalog variant does not belong to product");
            }

            return new StockTarget(
                    catalogVariant.getProduct(),
                    catalogVariant,
                    defaultAmount(catalogVariant.getStockQuantity()),
                    defaultAmount(catalogVariant.getReservedQuantity()),
                    defaultAmount(catalogVariant.getSoldQuantity())
            );
        }

        if (productId == null) {
            throw new IllegalArgumentException("A product is required when no catalog variant is selected.");
        }

        Product product = (forUpdate
                ? productRepository.findByIdForUpdate(productId)
                : productRepository.findById(productId))
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        return new StockTarget(
                product,
                null,
                defaultAmount(product.getStockAvailableQuantity()),
                defaultAmount(product.getStockReservedQuantity()),
                defaultAmount(product.getStockSoldQuantity())
        );
    }

    private void persistStockTarget(StockTarget target) {
        if (target.catalogVariant != null) {
            target.catalogVariant.setStockQuantity(target.available);
            target.catalogVariant.setReservedQuantity(target.reserved);
            target.catalogVariant.setSoldQuantity(target.sold);
            productVariantRepository.save(target.catalogVariant);
            return;
        }

        target.product.setStockAvailableQuantity(target.available);
        target.product.setStockReservedQuantity(target.reserved);
        target.product.setStockSoldQuantity(target.sold);
        productRepository.save(target.product);
    }

    private void validateQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
    }

    private StockTransaction saveTransaction(
            StockTarget target,
            StockBucketEnum from,
            StockBucketEnum to,
            StockTransactionTypeEnum type,
            BigDecimal qty,
            Long salesOrderId,
            Long orderItemId,
            String idempotencyKey,
            String note
    ) {
        StockTransaction tx = new StockTransaction();
        tx.setProduct(target.product);
        tx.setCatalogVariant(target.catalogVariant);
        tx.setSalesOrder(resolveSalesOrder(salesOrderId));
        tx.setOrderItem(resolveOrderItem(orderItemId));
        tx.setFromBucket(from);
        tx.setToBucket(to);
        tx.setTransactionType(type);
        tx.setQuantity(qty);
        tx.setAvailableAfter(target.available);
        tx.setReservedAfter(target.reserved);
        tx.setSoldAfter(target.sold);
        tx.setIdempotencyKey(idempotencyKey);
        tx.setNote(note);
        return stockTransactionRepository.save(tx);
    }

    private SalesOrder resolveSalesOrder(Long id) {
        return id == null ? null
                : salesOrderRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("SalesOrder not found"));
    }

    private OrderItem resolveOrderItem(Long id) {
        return id == null ? null
                : orderItemRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("OrderItem not found"));
    }

    private BigDecimal defaultAmount(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }

    private boolean isStockManaged(StockTarget target) {

        if (target.catalogVariant != null) {
            return Boolean.TRUE.equals(target.product.getManageStock());
        }

        return Boolean.TRUE.equals(target.product.getManageStock());
    }

    private static class StockTarget {

        private final Product product;
        private final ProductVariant catalogVariant;
        private BigDecimal available;
        private BigDecimal reserved;
        private BigDecimal sold;

        private StockTarget(Product product, ProductVariant catalogVariant,
                BigDecimal available, BigDecimal reserved, BigDecimal sold) {
            this.product = product;
            this.catalogVariant = catalogVariant;
            this.available = available;
            this.reserved = reserved;
            this.sold = sold;
        }
    }
}

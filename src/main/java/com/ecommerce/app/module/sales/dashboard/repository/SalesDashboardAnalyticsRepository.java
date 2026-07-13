package com.ecommerce.app.module.sales.dashboard.repository;

import com.ecommerce.app.module.sales.dashboard.dto.SalesDashboardFilter;
import com.ecommerce.app.order.model.OrderStatus;
import com.ecommerce.app.order.model.PaymentMethod;
import com.ecommerce.app.order.model.PaymentStatus;
import com.ecommerce.app.product.model.ProductStatusEnum;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class SalesDashboardAnalyticsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public SalesTotals loadTotals(LocalDateTime start, LocalDateTime end, SalesDashboardFilter filter, Long vendorId) {
        Object[] row = entityManager.createQuery("""
                select coalesce(sum(case when o.status <> :cancelled then o.grandTotal else 0 end), 0),
                       count(distinct o.id),
                       count(distinct o.customer.id),
                       coalesce(sum(case when o.status <> :cancelled then o.totalMarketPlaceCommissionAmount else 0 end), 0),
                       coalesce(sum(case when o.status in (:returnedStatuses) then o.grandTotal else 0 end), 0)
                from SalesOrder o
                left join o.shippingAddress sa
                where """ + orderWhere("o", "sa"), Object[].class)
                .setParameter("cancelled", OrderStatus.CANCELLED)
                .setParameter("returnedStatuses", List.of(OrderStatus.RETURNED, OrderStatus.PARTIALLY_RETURNED))
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter("vendorId", vendorId)
                .setParameter("customerId", filter.getCustomerId())
                .setParameter("orderStatus", filter.getOrderStatus())
                .setParameter("categoryId", filter.getCategoryId())
                .setParameter("brandId", filter.getBrandId())
                .setParameter("productId", filter.getProductId())
                .setParameter("paymentMethod", filter.getPaymentMethod())
                .setParameter("shippingCarrierId", filter.getShippingCarrierId())
                .setParameter("regionLike", like(filter.getRegion()))
                .getSingleResult();
        return new SalesTotals(decimal(row[0]), number(row[1]), number(row[2]), decimal(row[3]), decimal(row[4]));
    }

    public BigDecimal loadRefundTotal(LocalDateTime start, LocalDateTime end, SalesDashboardFilter filter, Long vendorId) {
        Object value = entityManager.createQuery("""
                select coalesce(sum(oi.returnRefundAmount), 0)
                from OrderItem oi
                join oi.salesOrder o
                left join o.shippingAddress sa
                where """ + orderWhere("o", "sa") + """
                  and (:categoryId is null or oi.product.productcategory.id = :categoryId)
                  and (:brandId is null or oi.product.manufacturer.id = :brandId)
                  and (:productId is null or oi.product.id = :productId)
                """, Object.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter("vendorId", vendorId)
                .setParameter("customerId", filter.getCustomerId())
                .setParameter("orderStatus", filter.getOrderStatus())
                .setParameter("categoryId", filter.getCategoryId())
                .setParameter("brandId", filter.getBrandId())
                .setParameter("productId", filter.getProductId())
                .setParameter("paymentMethod", filter.getPaymentMethod())
                .setParameter("shippingCarrierId", filter.getShippingCarrierId())
                .setParameter("regionLike", like(filter.getRegion()))
                .getSingleResult();
        return decimal(value);
    }

    public BigDecimal loadGrossProfit(LocalDateTime start, LocalDateTime end, SalesDashboardFilter filter, Long vendorId) {
        Object value = entityManager.createQuery("""
                select coalesce(sum(oi.itemTotal - (coalesce(p.purchasePrice, 0) * oi.quantity)), 0)
                from OrderItem oi
                join oi.salesOrder o
                join oi.product p
                left join o.shippingAddress sa
                where """ + orderWhere("o", "sa") + """
                  and o.status <> :cancelled
                  and (:categoryId is null or p.productcategory.id = :categoryId)
                  and (:brandId is null or p.manufacturer.id = :brandId)
                  and (:productId is null or p.id = :productId)
                """, Object.class)
                .setParameter("cancelled", OrderStatus.CANCELLED)
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter("vendorId", vendorId)
                .setParameter("customerId", filter.getCustomerId())
                .setParameter("orderStatus", filter.getOrderStatus())
                .setParameter("categoryId", filter.getCategoryId())
                .setParameter("brandId", filter.getBrandId())
                .setParameter("productId", filter.getProductId())
                .setParameter("paymentMethod", filter.getPaymentMethod())
                .setParameter("shippingCarrierId", filter.getShippingCarrierId())
                .setParameter("regionLike", like(filter.getRegion()))
                .getSingleResult();
        return decimal(value);
    }

    public List<Object[]> loadStatusCounts(LocalDateTime start, LocalDateTime end, SalesDashboardFilter filter, Long vendorId) {
        Query query = entityManager.createQuery("""
                select o.status, count(distinct o.id)
                from SalesOrder o
                left join o.shippingAddress sa
                where """ + orderWhere("o", "sa") + """
                group by o.status
                order by count(distinct o.id) desc
                """, Object[].class);
        setOrderParameters(query, start, end, filter, vendorId);
        return query.getResultList();
    }

    public List<Object[]> loadDailyTrend(LocalDateTime start, LocalDateTime end, SalesDashboardFilter filter, Long vendorId) {
        Query query = entityManager.createQuery("""
                select function('date', o.created),
                       coalesce(sum(case when o.status <> :cancelled then o.grandTotal else 0 end), 0),
                       count(distinct o.id),
                       sum(case when o.status in (:completedStatuses) then 1 else 0 end),
                       sum(case when o.status = :cancelled then 1 else 0 end),
                       sum(case when o.status in (:returnedStatuses) then 1 else 0 end)
                from SalesOrder o
                left join o.shippingAddress sa
                where """ + orderWhere("o", "sa") + """
                group by function('date', o.created)
                order by function('date', o.created)
                """, Object[].class);
        setOrderParameters(query, start, end, filter, vendorId);
        query.setParameter("cancelled", OrderStatus.CANCELLED);
        query.setParameter("completedStatuses", List.of(OrderStatus.DELIVERED, OrderStatus.COMPLETED));
        query.setParameter("returnedStatuses", List.of(OrderStatus.RETURN_REQUESTED, OrderStatus.PARTIALLY_RETURNED, OrderStatus.RETURNED));
        return query.getResultList();
    }

    public List<Object[]> loadTopProducts(LocalDateTime start, LocalDateTime end, SalesDashboardFilter filter, Long vendorId, int limit) {
        Query query = entityManager.createQuery("""
                select p.title, p.sku, c.name, m.name, v.companyName,
                       coalesce(sum(oi.quantity), 0),
                       coalesce(sum(oi.itemTotal), 0),
                       coalesce(sum(oi.itemTotal - (coalesce(p.purchasePrice, 0) * oi.quantity)), 0),
                       p.stockAvailableQuantity
                from OrderItem oi
                join oi.salesOrder o
                join oi.product p
                left join p.productcategory c
                left join p.manufacturer m
                left join p.vendorprofile v
                left join o.shippingAddress sa
                where """ + orderWhere("o", "sa") + """
                  and o.status <> :cancelled
                  and (:categoryId is null or c.id = :categoryId)
                  and (:brandId is null or m.id = :brandId)
                  and (:productId is null or p.id = :productId)
                group by p.id, p.title, p.sku, c.name, m.name, v.companyName, p.stockAvailableQuantity
                order by sum(oi.itemTotal) desc
                """, Object[].class);
        setOrderParameters(query, start, end, filter, vendorId);
        query.setParameter("cancelled", OrderStatus.CANCELLED);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public List<Object[]> loadSalesByCategory(LocalDateTime start, LocalDateTime end, SalesDashboardFilter filter, Long vendorId, int limit) {
        Query query = entityManager.createQuery("""
                select coalesce(c.name, 'Uncategorized'), coalesce(sum(oi.itemTotal), 0),
                       count(distinct o.id), coalesce(sum(oi.quantity), 0)
                from OrderItem oi
                join oi.salesOrder o
                join oi.product p
                left join p.productcategory c
                left join o.shippingAddress sa
                where """ + orderWhere("o", "sa") + """
                  and o.status <> :cancelled
                  and (:categoryId is null or c.id = :categoryId)
                  and (:brandId is null or p.manufacturer.id = :brandId)
                  and (:productId is null or p.id = :productId)
                group by c.name
                order by sum(oi.itemTotal) desc
                """, Object[].class);
        setOrderParameters(query, start, end, filter, vendorId);
        query.setParameter("cancelled", OrderStatus.CANCELLED);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public List<Object[]> loadSalesByBrand(LocalDateTime start, LocalDateTime end, SalesDashboardFilter filter, Long vendorId, int limit) {
        Query query = entityManager.createQuery("""
                select coalesce(m.name, 'Unbranded'), coalesce(sum(oi.itemTotal), 0),
                       count(distinct o.id), coalesce(sum(oi.quantity), 0)
                from OrderItem oi
                join oi.salesOrder o
                join oi.product p
                left join p.manufacturer m
                left join o.shippingAddress sa
                where """ + orderWhere("o", "sa") + """
                  and o.status <> :cancelled
                  and (:categoryId is null or p.productcategory.id = :categoryId)
                  and (:brandId is null or m.id = :brandId)
                  and (:productId is null or p.id = :productId)
                group by m.name
                order by sum(oi.itemTotal) desc
                """, Object[].class);
        setOrderParameters(query, start, end, filter, vendorId);
        query.setParameter("cancelled", OrderStatus.CANCELLED);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public List<Object[]> loadTopVendors(LocalDateTime start, LocalDateTime end, SalesDashboardFilter filter, Long vendorId, int limit) {
        Query query = entityManager.createQuery("""
                select o.vendorId, count(distinct o.id), coalesce(sum(o.grandTotal), 0),
                       coalesce(sum(o.totalMarketPlaceCommissionAmount), 0)
                from SalesOrder o
                left join o.shippingAddress sa
                where """ + orderWhere("o", "sa") + """
                  and o.status <> :cancelled
                  and o.vendorId is not null
                group by o.vendorId
                order by sum(o.grandTotal) desc
                """, Object[].class);
        setOrderParameters(query, start, end, filter, vendorId);
        query.setParameter("cancelled", OrderStatus.CANCELLED);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public List<Object[]> loadPaymentAnalytics(LocalDateTime start, LocalDateTime end, SalesDashboardFilter filter, Long vendorId) {
        Query query = entityManager.createQuery("""
                select p.paymentMethod,
                       coalesce(sum(p.paidAmount), 0),
                       count(p.id),
                       sum(case when p.paymentStatus = :paid then 1 else 0 end),
                       sum(case when p.paymentStatus in (:failedStatuses) then 1 else 0 end),
                       sum(case when p.paymentStatus in (:pendingStatuses) then 1 else 0 end)
                from Payment p
                join p.order o
                left join o.shippingAddress sa
                where """ + orderWhere("o", "sa") + """
                  and (:paymentMethod is null or p.paymentMethod = :paymentMethod)
                group by p.paymentMethod
                order by sum(p.paidAmount) desc
                """, Object[].class);
        setOrderParameters(query, start, end, filter, vendorId);
        query.setParameter("paid", PaymentStatus.Paid);
        query.setParameter("failedStatuses", List.of(PaymentStatus.Failed, PaymentStatus.Cancelled));
        query.setParameter("pendingStatuses", List.of(PaymentStatus.Partial, PaymentStatus.Remaining));
        return query.getResultList();
    }

    public List<Object[]> loadTopCustomers(LocalDateTime start, LocalDateTime end, SalesDashboardFilter filter, Long vendorId, int limit) {
        Query query = entityManager.createQuery("""
                select concat(coalesce(c.firstName, ''), ' ', coalesce(c.lastName, '')),
                       count(distinct o.id), coalesce(sum(o.grandTotal), 0), max(function('date', o.created))
                from SalesOrder o
                join o.customer c
                left join o.shippingAddress sa
                where """ + orderWhere("o", "sa") + """
                  and o.status <> :cancelled
                group by c.id, c.firstName, c.lastName
                order by sum(o.grandTotal) desc
                """, Object[].class);
        setOrderParameters(query, start, end, filter, vendorId);
        query.setParameter("cancelled", OrderStatus.CANCELLED);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public List<Object[]> loadGeographicSales(LocalDateTime start, LocalDateTime end, SalesDashboardFilter filter, Long vendorId, int limit) {
        Query query = entityManager.createQuery("""
                select coalesce(sa.district, coalesce(sa.city, coalesce(sa.country, 'Unmapped'))),
                       coalesce(sum(o.grandTotal), 0), count(distinct o.id)
                from SalesOrder o
                left join o.shippingAddress sa
                where """ + orderWhere("o", "sa") + """
                  and o.status <> :cancelled
                group by coalesce(sa.district, coalesce(sa.city, coalesce(sa.country, 'Unmapped')))
                order by sum(o.grandTotal) desc
                """, Object[].class);
        setOrderParameters(query, start, end, filter, vendorId);
        query.setParameter("cancelled", OrderStatus.CANCELLED);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public Object[] loadShipmentSummary(LocalDateTime start, LocalDateTime end, Long vendorId) {
        return entityManager.createQuery("""
                select sum(case when s.status in (:transitStatuses) then 1 else 0 end),
                       sum(case when s.status = :delivered then 1 else 0 end),
                       coalesce(sum(s.shippingCost), 0),
                       count(s.id)
                from Shipment s
                where s.created >= :start and s.created < :end
                  and (:vendorId is null or s.vendorId = :vendorId)
                """, Object[].class)
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter("vendorId", vendorId)
                .setParameter("transitStatuses", List.of(
                        com.ecommerce.app.module.shipping.model.ShipmentStatus.PENDING,
                        com.ecommerce.app.module.shipping.model.ShipmentStatus.IN_TRANSIT))
                .setParameter("delivered", com.ecommerce.app.module.shipping.model.ShipmentStatus.DELIVERED)
                .getSingleResult();
    }

    public List<Object[]> loadCarrierPerformance(LocalDateTime start, LocalDateTime end, Long vendorId, int limit) {
        return entityManager.createQuery("""
                select coalesce(c.name, 'Manual carrier'), coalesce(sum(s.shippingCost), 0), count(s.id),
                       sum(case when s.status = :delivered then 1 else 0 end)
                from Shipment s
                left join s.carrier c
                where s.created >= :start and s.created < :end
                  and (:vendorId is null or s.vendorId = :vendorId)
                group by c.name
                order by count(s.id) desc
                """, Object[].class)
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter("vendorId", vendorId)
                .setParameter("delivered", com.ecommerce.app.module.shipping.model.ShipmentStatus.DELIVERED)
                .setMaxResults(limit)
                .getResultList();
    }

    public Object[] loadInventoryImpact(Long vendorId) {
        return entityManager.createQuery("""
                select sum(case when p.stockAvailableQuantity > 0 and p.stockAvailableQuantity <= 5 then 1 else 0 end),
                       sum(case when p.stockAvailableQuantity <= 0 then 1 else 0 end),
                       sum(case when p.stockSoldQuantity > 0 then 1 else 0 end),
                       sum(case when p.stockAvailableQuantity > 100 then 1 else 0 end)
                from Product p
                where (:vendorId is null or p.vendorprofile.id = :vendorId)
                  and p.status = :active
                """, Object[].class)
                .setParameter("vendorId", vendorId)
                .setParameter("active", ProductStatusEnum.Active)
                .getSingleResult();
    }

    public List<Object[]> loadActivity(LocalDateTime start, LocalDateTime end, Long vendorId, int limit) {
        return entityManager.createQuery("""
                select o.orderCode, o.status, o.grandTotal, o.created
                from SalesOrder o
                where o.created >= :start and o.created < :end
                  and (:vendorId is null or o.vendorId = :vendorId)
                order by o.created desc
                """, Object[].class)
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter("vendorId", vendorId)
                .setMaxResults(limit)
                .getResultList();
    }

    public BigDecimal loadCouponUsage(LocalDateTime start, LocalDateTime end) {
        Object value = entityManager.createQuery("""
                select coalesce(sum(c.discountAmount), 0)
                from CouponRedemption c
                where c.redeemedAt >= :start and c.redeemedAt < :end
                """, Object.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
        return decimal(value);
    }

    public BigDecimal loadCashbackIssued(LocalDateTime start, LocalDateTime end) {
        Object value = entityManager.createQuery("""
                select coalesce(sum(c.amount), 0)
                from CashbackTransaction c
                where c.created >= :start and c.created < :end
                """, Object.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
        return decimal(value);
    }

    public BigDecimal loadGiftCardSales(LocalDateTime start, LocalDateTime end) {
        Object value = entityManager.createQuery("""
                select coalesce(sum(g.amount), 0)
                from GiftCardPurchase g
                where g.paidAt >= :start and g.paidAt < :end
                """, Object.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
        return decimal(value);
    }

    public List<SalesOption> loadVendorOptions() {
        return entityManager.createQuery("""
                select v.id, v.companyName
                from Vendorprofile v
                order by v.companyName asc, v.id asc
                """, Object[].class)
                .getResultList()
                .stream()
                .map(row -> new SalesOption(number(row[0]), text(row[1], "Unnamed vendor")))
                .toList();
    }

    public List<SalesOption> loadCategoryOptions() {
        return optionQuery("select c.id, c.name from Productcategory c order by c.name asc");
    }

    public List<SalesOption> loadBrandOptions() {
        return optionQuery("select m.id, m.name from Manufacturer m order by m.name asc");
    }

    public List<SalesOption> loadProductOptions(Long vendorId) {
        return entityManager.createQuery("""
                select p.id, p.title
                from Product p
                where (:vendorId is null or p.vendorprofile.id = :vendorId)
                order by p.title asc
                """, Object[].class)
                .setParameter("vendorId", vendorId)
                .setMaxResults(300)
                .getResultList()
                .stream()
                .map(row -> new SalesOption(number(row[0]), text(row[1], "Untitled product")))
                .toList();
    }

    public List<SalesOption> loadCustomerOptions() {
        return entityManager.createQuery("""
                select u.id, concat(coalesce(u.firstName, ''), ' ', coalesce(u.lastName, ''), ' - ', coalesce(u.mobile, ''))
                from Users u
                order by u.createdOn desc
                """, Object[].class)
                .setMaxResults(300)
                .getResultList()
                .stream()
                .map(row -> new SalesOption(number(row[0]), text(row[1], "Customer #" + number(row[0]))))
                .toList();
    }

    public List<SalesOption> loadCarrierOptions() {
        return optionQuery("select c.id, c.name from Carrier c order by c.name asc");
    }

    private List<SalesOption> optionQuery(String jpql) {
        return entityManager.createQuery(jpql, Object[].class)
                .getResultList()
                .stream()
                .map(row -> new SalesOption(number(row[0]), text(row[1], "Option #" + number(row[0]))))
                .toList();
    }

    private String orderWhere(String orderAlias, String shippingAlias) {
        return orderAlias + ".created >= :start and " + orderAlias + ".created < :end\n"
                + "                  and (:vendorId is null or " + orderAlias + ".vendorId = :vendorId)\n"
                + "                  and (:customerId is null or " + orderAlias + ".customer.id = :customerId)\n"
                + "                  and (:orderStatus is null or " + orderAlias + ".status = :orderStatus)\n"
                + "                  and (:regionLike is null or lower(coalesce(" + shippingAlias + ".country, '')) like :regionLike"
                + " or lower(coalesce(" + shippingAlias + ".district, '')) like :regionLike"
                + " or lower(coalesce(" + shippingAlias + ".city, '')) like :regionLike)\n"
                + "                  and (:categoryId is null or exists (select 1 from OrderItem fi join fi.product fp where fi.salesOrder = " + orderAlias + " and fp.productcategory.id = :categoryId))\n"
                + "                  and (:brandId is null or exists (select 1 from OrderItem fi join fi.product fp where fi.salesOrder = " + orderAlias + " and fp.manufacturer.id = :brandId))\n"
                + "                  and (:productId is null or exists (select 1 from OrderItem fi join fi.product fp where fi.salesOrder = " + orderAlias + " and fp.id = :productId))\n"
                + "                  and (:paymentMethod is null or exists (select 1 from Payment fp where fp.order = " + orderAlias + " and fp.paymentMethod = :paymentMethod))\n"
                + "                  and (:shippingCarrierId is null or exists (select 1 from Shipment sh where sh.salesOrderId = " + orderAlias + ".id and sh.carrier.id = :shippingCarrierId))\n";
    }

    private void setOrderParameters(Query query, LocalDateTime start, LocalDateTime end, SalesDashboardFilter filter, Long vendorId) {
        query.setParameter("start", start);
        query.setParameter("end", end);
        query.setParameter("vendorId", vendorId);
        query.setParameter("customerId", filter.getCustomerId());
        query.setParameter("orderStatus", filter.getOrderStatus());
        query.setParameter("categoryId", filter.getCategoryId());
        query.setParameter("brandId", filter.getBrandId());
        query.setParameter("productId", filter.getProductId());
        query.setParameter("paymentMethod", filter.getPaymentMethod());
        query.setParameter("shippingCarrierId", filter.getShippingCarrierId());
        query.setParameter("regionLike", like(filter.getRegion()));
    }

    private String like(String value) {
        return value == null || value.isBlank() ? null : "%" + value.trim().toLowerCase() + "%";
    }

    private BigDecimal decimal(Object value) {
        return value instanceof BigDecimal amount ? amount : value instanceof Number number
                ? BigDecimal.valueOf(number.doubleValue()) : BigDecimal.ZERO;
    }

    private long number(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private String text(Object value, String fallback) {
        return value instanceof String text && !text.isBlank() ? text.trim() : fallback;
    }

    public record SalesTotals(BigDecimal grossSales, long totalOrders, long uniqueCustomers,
            BigDecimal marketplaceCommission, BigDecimal returnedAmount) { }

    public record SalesOption(long id, String name) { }
}

package com.ecommerce.app.review.services;

import com.ecommerce.app.module.user.model.UserType;
import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.order.model.OrderItem;
import com.ecommerce.app.order.model.OrderStatus;
import com.ecommerce.app.order.model.SalesOrder;
import com.ecommerce.app.order.repository.OrderItemRepository;
import com.ecommerce.app.order.repository.SalesOrderRepository;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.ripository.ProductRepository;
import com.ecommerce.app.review.model.ProductReview;
import com.ecommerce.app.review.repository.ProductReviewRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductReviewService {

    private static final Set<OrderStatus> REVIEWABLE_STATUSES = EnumSet.of(
            OrderStatus.DELIVERED,
            OrderStatus.COMPLETED,
            OrderStatus.RETURN_REQUESTED,
            OrderStatus.PARTIALLY_RETURNED,
            OrderStatus.RETURNED
    );

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    public Map<String, Object> getProductReviewSummary(Long productId) {
        List<ProductReview> reviews = productId == null
                ? List.of()
                : productReviewRepository.findByProduct_IdAndVisibleTrueOrderByCreatedDesc(productId);

        return buildSummary(reviews);
    }

    public Map<String, Object> getProductReviewSummary(String productUuid) {
        List<ProductReview> reviews = productUuid == null || productUuid.isBlank()
                ? List.of()
                : productReviewRepository.findByProduct_UuidAndVisibleTrueOrderByCreatedDesc(productUuid);

        return buildSummary(reviews);
    }

    public List<Map<String, Object>> getPublicReviewsForProduct(Long productId) {
        if (productId == null) {
            return List.of();
        }

        return productReviewRepository.findByProduct_IdAndVisibleTrueOrderByCreatedDesc(productId)
                .stream()
                .sorted(Comparator.comparing(this::resolveActivityAt).reversed())
                .map(this::toPublicReviewCard)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getPublicReviewsForProduct(String productUuid) {
        if (productUuid == null || productUuid.isBlank()) {
            return List.of();
        }

        return productReviewRepository.findByProduct_UuidAndVisibleTrueOrderByCreatedDesc(productUuid)
                .stream()
                .sorted(Comparator.comparing(this::resolveActivityAt).reversed())
                .map(this::toPublicReviewCard)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getCustomerReviewContextForProduct(Long productId, Long customerId) {
        Map<String, Object> context = new HashMap<>();
        context.put("authenticated", customerId != null);
        context.put("customerAccount", false);
        context.put("eligible", false);
        context.put("reviewExists", false);
        context.put("submitLabel", "Publish review");
        context.put("helperText", "Sign in with a customer account to leave a verified review.");

        if (customerId == null || productId == null) {
            return context;
        }

        Users customer = usersRepository.findById(customerId).orElse(null);
        if (customer == null || customer.getUserType() != UserType.customer) {
            context.put("helperText", "Only customer accounts can publish verified product reviews.");
            return context;
        }

        context.put("customerAccount", true);

        ProductReview existingReview = productReviewRepository.findByCustomer_IdAndProduct_Id(customerId, productId).orElse(null);
        OrderItem eligibleOrderItem = findLatestEligibleOrderItem(customerId, productId);

        if (existingReview != null) {
            context.put("reviewExists", true);
            context.put("rating", existingReview.getRating());
            context.put("title", existingReview.getTitle());
            context.put("reviewText", existingReview.getReviewText());
            context.put("reviewActivityAt", resolveActivityAt(existingReview));
            context.put("submitLabel", "Update review");
        }

        if (eligibleOrderItem != null && eligibleOrderItem.getSalesOrder() != null) {
            context.put("eligible", true);
            context.put("orderId", eligibleOrderItem.getSalesOrder().getId());
            context.put(
                    "helperText",
                    existingReview == null
                            ? "Share what stood out after your verified purchase."
                            : "Your review is already live. You can update it any time."
            );
            return context;
        }

        context.put("helperText", "Reviews unlock after this product is delivered on a completed customer order.");
        return context;
    }

    public Map<String, Object> getCustomerReviewContextForProduct(String productUuid, Long customerId) {
        Map<String, Object> context = new HashMap<>();
        context.put("authenticated", customerId != null);
        context.put("customerAccount", false);
        context.put("eligible", false);
        context.put("reviewExists", false);
        context.put("submitLabel", "Publish review");
        context.put("helperText", "Sign in with a customer account to leave a verified review.");

        if (customerId == null || productUuid == null || productUuid.isBlank()) {
            return context;
        }

        Product product = productRepository.findByUuid(productUuid).orElse(null);
        if (product == null || product.getId() == null) {
            return context;
        }

        Users customer = usersRepository.findById(customerId).orElse(null);
        if (customer == null || customer.getUserType() != UserType.customer) {
            context.put("helperText", "Only customer accounts can publish verified product reviews.");
            return context;
        }

        context.put("customerAccount", true);

        ProductReview existingReview = productReviewRepository.findByCustomer_IdAndProduct_Uuid(customerId, productUuid).orElse(null);
        OrderItem eligibleOrderItem = findLatestEligibleOrderItem(customerId, product.getId());

        if (existingReview != null) {
            context.put("reviewExists", true);
            context.put("rating", existingReview.getRating());
            context.put("title", existingReview.getTitle());
            context.put("reviewText", existingReview.getReviewText());
            context.put("reviewActivityAt", resolveActivityAt(existingReview));
            context.put("submitLabel", "Update review");
        }

        if (eligibleOrderItem != null && eligibleOrderItem.getSalesOrder() != null) {
            context.put("eligible", true);
            context.put("orderId", eligibleOrderItem.getSalesOrder().getId());
            context.put(
                    "helperText",
                    existingReview == null
                            ? "Share what stood out after your verified purchase."
                            : "Your review is already live. You can update it any time."
            );
            return context;
        }

        context.put("helperText", "Reviews unlock after this product is delivered on a completed customer order.");
        return context;
    }

    public List<Map<String, Object>> enrichOrderItemsWithReviewData(List<Map<String, Object>> orderItems, Long customerId, OrderStatus orderStatus) {
        if (orderItems == null || orderItems.isEmpty() || customerId == null) {
            return orderItems == null ? List.of() : orderItems;
        }

        List<Long> productIds = orderItems.stream()
                .map(item -> getLong(item.get("productid")))
                .filter(productId -> productId > 0)
                .distinct()
                .collect(Collectors.toList());

        if (productIds.isEmpty()) {
            return orderItems;
        }

        Map<Long, ProductReview> reviewsByProductId = productReviewRepository.findByCustomer_IdAndProduct_IdIn(customerId, productIds)
                .stream()
                .filter(review -> review.getProduct() != null && review.getProduct().getId() != null)
                .collect(Collectors.toMap(review -> review.getProduct().getId(), review -> review, (first, ignored) -> first));

        boolean reviewEligible = isReviewableStatus(orderStatus);

        for (Map<String, Object> orderItem : orderItems) {
            Long productId = getLong(orderItem.get("productid"));
            ProductReview review = reviewsByProductId.get(productId);

            orderItem.put("reviewEligible", reviewEligible);
            orderItem.put(
                    "reviewLockedReason",
                    reviewEligible
                            ? "Your review will appear publicly as a verified purchase."
                            : "Reviewing becomes available after this order reaches a delivered or completed state."
            );
            orderItem.put("reviewExists", review != null);
            orderItem.put("reviewSubmitLabel", review == null ? "Publish review" : "Update review");

            if (review != null) {
                orderItem.put("reviewRating", review.getRating());
                orderItem.put("reviewTitle", review.getTitle());
                orderItem.put("reviewText", review.getReviewText());
                orderItem.put("reviewActivityAt", resolveActivityAt(review));
            }
        }

        return orderItems;
    }

    public List<Map<String, Object>> enrichProductCardsWithReviewSummary(List<Map<String, Object>> productCards) {
        if (productCards == null || productCards.isEmpty()) {
            return productCards == null ? List.of() : productCards;
        }

        List<Long> productIds = productCards.stream()
                .map(product -> getLong(product.get("id")))
                .filter(productId -> productId > 0)
                .distinct()
                .collect(Collectors.toList());

        if (productIds.isEmpty()) {
            return productCards;
        }

        Map<Long, List<ProductReview>> reviewsByProductId = productReviewRepository.findByProduct_IdInAndVisibleTrue(productIds)
                .stream()
                .filter(review -> review.getProduct() != null && review.getProduct().getId() != null)
                .collect(Collectors.groupingBy(review -> review.getProduct().getId()));

        for (Map<String, Object> productCard : productCards) {
            Long productId = getLong(productCard.get("id"));
            Map<String, Object> summary = buildSummary(reviewsByProductId.getOrDefault(productId, List.of()));
            productCard.put("averageRating", summary.get("averageRating"));
            productCard.put("reviewCount", summary.get("reviewCount"));
            productCard.put("hasReviews", summary.get("hasReviews"));
        }

        return productCards;
    }

    public void saveCustomerReview(Long customerId, Long orderId, Long productId, Integer rating, String title, String reviewText) {
        Users customer = usersRepository.findById(customerId).orElseThrow(() -> new IllegalArgumentException("Customer account not found."));
        if (customer.getUserType() != UserType.customer) {
            throw new IllegalArgumentException("Only customer accounts can submit product reviews.");
        }

        if (orderId == null || productId == null) {
            throw new IllegalArgumentException("A valid order and product are required to publish a review.");
        }

        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Please choose a star rating between 1 and 5.");
        }

        String normalizedTitle = normalizeTitle(title);
        String normalizedReviewText = normalizeReviewText(reviewText);

        SalesOrder salesOrder = salesOrderRepository.findByIdAndCustomer_Id(orderId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("The selected order could not be verified for your account."));

        if (!isReviewableStatus(salesOrder.getStatus())) {
            throw new IllegalArgumentException("This order needs to reach delivery before you can review the product.");
        }

        boolean productBelongsToOrder = orderItemRepository.findBySalesOrder_Id(orderId)
                .stream()
                .anyMatch(orderItem -> isMatchingProduct(orderItem, productId));

        if (!productBelongsToOrder) {
            throw new IllegalArgumentException("That product was not found in the selected order.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        ProductReview review = productReviewRepository.findByCustomer_IdAndProduct_Id(customerId, productId)
                .orElseGet(ProductReview::new);

        review.setProduct(product);
        review.setCustomer(customer);
        review.setSalesOrder(salesOrder);
        review.setRating(rating);
        review.setTitle(normalizedTitle);
        review.setReviewText(normalizedReviewText);
        review.setVerifiedPurchase(Boolean.TRUE);
        review.setVisible(Boolean.TRUE);

        productReviewRepository.save(review);
    }

    public void saveCustomerReview(Long customerId, Long orderId, String productUuid, Integer rating, String title, String reviewText) {
        Users customer = usersRepository.findById(customerId).orElseThrow(() -> new IllegalArgumentException("Customer account not found."));
        if (customer.getUserType() != UserType.customer) {
            throw new IllegalArgumentException("Only customer accounts can submit product reviews.");
        }

        if (orderId == null || productUuid == null || productUuid.isBlank()) {
            throw new IllegalArgumentException("A valid order and product are required to publish a review.");
        }

        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Please choose a star rating between 1 and 5.");
        }

        String normalizedTitle = normalizeTitle(title);
        String normalizedReviewText = normalizeReviewText(reviewText);

        SalesOrder salesOrder = salesOrderRepository.findByIdAndCustomer_Id(orderId, customerId)
                .orElseThrow(() -> new IllegalArgumentException("The selected order could not be verified for your account."));

        if (!isReviewableStatus(salesOrder.getStatus())) {
            throw new IllegalArgumentException("This order needs to reach delivery before you can review the product.");
        }

        Product product = productRepository.findByUuid(productUuid)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        boolean productBelongsToOrder = orderItemRepository.findBySalesOrder_Id(orderId)
                .stream()
                .anyMatch(orderItem -> isMatchingProduct(orderItem, product.getId()));

        if (!productBelongsToOrder) {
            throw new IllegalArgumentException("That product was not found in the selected order.");
        }

        ProductReview review = productReviewRepository.findByCustomer_IdAndProduct_Uuid(customerId, productUuid)
                .orElseGet(ProductReview::new);

        review.setProduct(product);
        review.setCustomer(customer);
        review.setSalesOrder(salesOrder);
        review.setRating(rating);
        review.setTitle(normalizedTitle);
        review.setReviewText(normalizedReviewText);
        review.setVerifiedPurchase(Boolean.TRUE);
        review.setVisible(Boolean.TRUE);

        productReviewRepository.save(review);
    }

    public boolean isReviewableStatus(OrderStatus status) {
        return status != null && REVIEWABLE_STATUSES.contains(status);
    }

    private Map<String, Object> buildSummary(List<ProductReview> reviews) {
        List<ProductReview> safeReviews = reviews == null ? List.of() : reviews;
        Map<String, Object> summary = new LinkedHashMap<>();

        long reviewCount = safeReviews.size();
        BigDecimal total = safeReviews.stream()
                .map(ProductReview::getRating)
                .filter(Objects::nonNull)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageRating = reviewCount == 0
                ? BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP)
                : total.divide(BigDecimal.valueOf(reviewCount), 1, RoundingMode.HALF_UP);

        long recommendedCount = safeReviews.stream()
                .map(ProductReview::getRating)
                .filter(Objects::nonNull)
                .filter(rating -> rating >= 4)
                .count();

        List<Map<String, Object>> ratingBreakdown = new ArrayList<>();
        for (int star = 5; star >= 1; star--) {
            int currentStar = star;
            long count = safeReviews.stream()
                    .map(ProductReview::getRating)
                    .filter(Objects::nonNull)
                    .filter(rating -> rating == currentStar)
                    .count();

            BigDecimal percent = reviewCount == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(count)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(reviewCount), 1, RoundingMode.HALF_UP);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("rating", currentStar);
            row.put("count", count);
            row.put("percent", percent);
            ratingBreakdown.add(row);
        }

        BigDecimal recommendationPercent = reviewCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(recommendedCount)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(reviewCount), 0, RoundingMode.HALF_UP);

        summary.put("averageRating", averageRating);
        summary.put("averageRatingRounded", averageRating.setScale(0, RoundingMode.HALF_UP).intValue());
        summary.put("reviewCount", reviewCount);
        summary.put("recommendationPercent", recommendationPercent.intValue());
        summary.put("ratingBreakdown", ratingBreakdown);
        summary.put("hasReviews", reviewCount > 0);
        return summary;
    }

    private Map<String, Object> toPublicReviewCard(ProductReview review) {
        Map<String, Object> card = new LinkedHashMap<>();
        String firstName = review.getCustomer() != null ? safeText(review.getCustomer().getFirstName()) : "";
        String lastName = review.getCustomer() != null ? safeText(review.getCustomer().getLastName()) : "";
        String displayName = (firstName + " " + lastName).trim();

        card.put("rating", review.getRating());
        card.put("title", review.getTitle());
        card.put("reviewText", review.getReviewText());
        card.put("verifiedPurchase", Boolean.TRUE.equals(review.getVerifiedPurchase()));
        card.put("reviewerName", displayName.isBlank() ? "Verified customer" : displayName);
        card.put("reviewerInitials", buildInitials(firstName, lastName));
        card.put("activityAt", resolveActivityAt(review));
        return card;
    }

    private OrderItem findLatestEligibleOrderItem(Long customerId, Long productId) {
        return orderItemRepository.findReviewEligibleItems(customerId, productId, REVIEWABLE_STATUSES)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private boolean isMatchingProduct(OrderItem orderItem, Long productId) {
        if (orderItem == null || productId == null) {
            return false;
        }

        if (orderItem.getProductid() != null && productId.equals(orderItem.getProductid())) {
            return true;
        }

        return orderItem.getProduct() != null
                && orderItem.getProduct().getId() != null
                && productId.equals(orderItem.getProduct().getId());
    }

    private LocalDateTime resolveActivityAt(ProductReview review) {
        return review.getModified() != null ? review.getModified() : review.getCreated();
    }

    private String normalizeTitle(String title) {
        String normalized = safeText(title);
        if (normalized.isBlank()) {
            return null;
        }

        if (normalized.length() > 140) {
            throw new IllegalArgumentException("Review headline must be 140 characters or fewer.");
        }

        return normalized;
    }

    private String normalizeReviewText(String reviewText) {
        String normalized = safeText(reviewText);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Please write a short review before submitting.");
        }

        if (normalized.length() > 2000) {
            throw new IllegalArgumentException("Review text must be 2000 characters or fewer.");
        }

        return normalized;
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String buildInitials(String firstName, String lastName) {
        StringBuilder initials = new StringBuilder();
        if (!safeText(firstName).isBlank()) {
            initials.append(Character.toUpperCase(safeText(firstName).charAt(0)));
        }
        if (!safeText(lastName).isBlank()) {
            initials.append(Character.toUpperCase(safeText(lastName).charAt(0)));
        }
        return initials.length() == 0 ? "VC" : initials.toString();
    }

    private Long getLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Long.valueOf(text.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return 0L;
    }
}

package com.ecommerce.app.module.wishlist.repository;

import com.ecommerce.app.module.wishlist.model.WishlistItem;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    Optional<WishlistItem> findByUser_IdAndProduct_Id(Long userId, Long productId);

    Optional<WishlistItem> findByUser_IdAndProduct_Uuid(Long userId, String productUuid);

    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);

    boolean existsByUser_IdAndProduct_Uuid(Long userId, String productUuid);

    long countByUser_Id(Long userId);

    void deleteByUser_IdAndProduct_Id(Long userId, Long productId);

    void deleteByUser_IdAndProduct_Uuid(Long userId, String productUuid);

    @Query("""
            select wi
            from WishlistItem wi
            join fetch wi.product p
            where wi.user.id = :userId
            order by wi.createdOn desc
            """)
    List<WishlistItem> findWishlistByUserId(@Param("userId") Long userId);

    @Query("select wi.product.id from WishlistItem wi where wi.user.id = :userId")
    Set<Long> findProductIdsByUserId(@Param("userId") Long userId);

    @Query("select wi.product.uuid from WishlistItem wi where wi.user.id = :userId and wi.product.uuid is not null")
    Set<String> findProductUuidsByUserId(@Param("userId") Long userId);
}

package com.ecommerce.app.module.wishlist.service;

import com.ecommerce.app.module.user.model.Users;
import com.ecommerce.app.module.user.ripository.UsersRepository;
import com.ecommerce.app.module.wishlist.model.WishlistItem;
import com.ecommerce.app.module.wishlist.repository.WishlistItemRepository;
import com.ecommerce.app.product.model.Product;
import com.ecommerce.app.product.ripository.ProductRepository;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WishlistService {

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Users requireUser(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new IllegalArgumentException("Please log in to use wishlist.");
        }
        return usersRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
    }

    @Transactional(readOnly = true)
    public List<WishlistItem> getWishlistItems(Principal principal) {
        Users user = requireUser(principal);
        return wishlistItemRepository.findWishlistByUserId(user.getId());
    }

    @Transactional(readOnly = true)
    public Set<String> getWishlistProductUuids(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return Collections.emptySet();
        }
        return usersRepository.findByEmail(principal.getName())
                .map(user -> wishlistItemRepository.findProductUuidsByUserId(user.getId()))
                .orElse(Collections.emptySet());
    }

    @Transactional(readOnly = true)
    public long getWishlistCount(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return 0L;
        }
        return usersRepository.findByEmail(principal.getName())
                .map(user -> wishlistItemRepository.countByUser_Id(user.getId()))
                .orElse(0L);
    }

    @Transactional(readOnly = true)
    public boolean isInWishlist(Principal principal, String productUuid) {
        if (productUuid == null || productUuid.isBlank()
                || principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return false;
        }
        return usersRepository.findByEmail(principal.getName())
                .map(user -> wishlistItemRepository.existsByUser_IdAndProduct_Uuid(user.getId(), productUuid))
                .orElse(false);
    }

    public boolean addProduct(Principal principal, String productUuid) {
        Users user = requireUser(principal);
        if (productUuid == null || productUuid.isBlank()) {
            throw new IllegalArgumentException("Product is required.");
        }
        if (wishlistItemRepository.existsByUser_IdAndProduct_Uuid(user.getId(), productUuid)) {
            return false;
        }

        Product product = productRepository.findByUuid(productUuid)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        WishlistItem wishlistItem = new WishlistItem();
        wishlistItem.setUser(user);
        wishlistItem.setProduct(product);
        wishlistItemRepository.save(wishlistItem);
        return true;
    }

    public boolean removeProduct(Principal principal, String productUuid) {
        Users user = requireUser(principal);
        if (productUuid == null || productUuid.isBlank()) {
            throw new IllegalArgumentException("Product is required.");
        }
        if (!wishlistItemRepository.existsByUser_IdAndProduct_Uuid(user.getId(), productUuid)) {
            return false;
        }
        wishlistItemRepository.deleteByUser_IdAndProduct_Uuid(user.getId(), productUuid);
        return true;
    }
}

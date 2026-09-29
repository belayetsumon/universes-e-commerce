# Marketplace Commission Settings Feature - Implementation Guide

## Overview
This feature allows administrators to set marketplace commission rates at multiple levels (Category, Vendor, Product, or Default), with automatic application to products when they are created or updated.

## Feature Components

### 1. Database Table
- **Table**: `commission_settings`
- **Location**: `src/main/resources/db/commission_settings_init.sql`
- **Purpose**: Stores commission configuration with support for hierarchical rates

### 2. Core Entities & Services

#### CommissionSettings Model
- **File**: `src/main/java/com/ecommerce/app/commission/model/CommissionSettings.java`
- **Key Fields**:
  - `commissionType`: DEFAULT, CATEGORY, VENDOR, or PRODUCT
  - `commissionRate`: Percentage value (0-100)
  - `categoryId`, `vendorId`, `productId`: References for specific commission types
  - `status`: ACTIVE, INACTIVE, or ARCHIVED
  - Date range support (`startDate`, `endDate`)

#### CommissionSettingsService
- **File**: `src/main/java/com/ecommerce/app/commission/service/CommissionSettingsService.java`
- **Key Methods**:
  - `getApplicableCommissionRate(Long productId, Long vendorId, Long categoryId)` - Determines applicable rate with hierarchy
  - `getApplicableCommissionRateForCategory(Long categoryId)`
  - `getApplicableCommissionRateForVendor(Long vendorId)`
  - CRUD operations for managing settings

#### ProductCommissionApplierService
- **File**: `src/main/java/com/ecommerce/app/commission/service/ProductCommissionApplierService.java`
- **Purpose**: Integration point with product creation/update flow
- **Key Methods**:
  - `applyDefaultCommission(Product product)` - Apply commission on creation
  - `applyVendorProductCommission(Product product, Long vendorId, Long categoryId)` - For vendor product creation
  - `reApplyCommissionOnUpdate(Product product, Long oldVendorId, Long oldCategoryId)` - Reapply if vendor/category changes

### 3. Admin Interface

#### CommissionSettingsController
- **File**: `src/main/java/com/ecommerce/app/commission/controller/CommissionSettingsController.java`
- **Routes**:
  - `GET /admin/commission-settings/list` - List all settings
  - `GET /admin/commission-settings/create` - Show create form
  - `POST /admin/commission-settings/create` - Save new setting
  - `GET /admin/commission-settings/edit/{id}` - Edit form
  - `POST /admin/commission-settings/edit/{id}` - Update setting
  - `POST /admin/commission-settings/delete/{id}` - Delete setting
  - `POST /admin/commission-settings/activate/{id}` - Activate
  - `POST /admin/commission-settings/deactivate/{id}` - Deactivate

#### Admin Templates
- **List View**: `src/main/resources/templates/admin/commission/settings_list.html`
- **Form**: `src/main/resources/templates/admin/commission/settings_form.html`

### 4. REST API for Integration

#### CommissionSettingsApiController
- **File**: `src/main/java/com/ecommerce/app/commission/controller/CommissionSettingsApiController.java`
- **Endpoints**:
  - `GET /api/commission/applicable-rate?productId=X&vendorId=Y&categoryId=Z`
  - `GET /api/commission/category/{categoryId}`
  - `GET /api/commission/vendor/{vendorId}`
  - `GET /api/commission/product/{productId}`

## Integration with Product Service

### Step 1: Update ProductService to Apply Commission

Add the following to your `ProductService` class (around the save/create methods):

```java
@Autowired
private ProductCommissionApplierService commissionApplierService;

// In the method where products are created/saved
public Product saveProduct(Product product) {
    // Apply default commission if not already set
    if (product.getMarketPlaceCommissionRate() == null) {
        commissionApplierService.applyDefaultCommission(product);
    }
    
    // Your existing save logic
    return repository.save(product);
}

// For vendor product creation
public Product saveVendorProduct(Product product, Long vendorId) {
    // Apply vendor's applicable commission
    Long categoryId = product.getProductcategory() != null ? 
        product.getProductcategory().getId() : null;
    
    commissionApplierService.applyVendorProductCommission(product, vendorId, categoryId);
    
    // Your existing save logic
    return repository.save(product);
}

// When updating product and vendor/category changes
public Product updateProduct(Product product, Long oldVendorId, Long oldCategoryId) {
    // Re-apply commission if vendor or category changed
    commissionApplierService.reApplyCommissionOnUpdate(product, oldVendorId, oldCategoryId);
    
    // Your existing update logic
    return repository.save(product);
}
```

### Step 2: Update ProductController

Modify your product creation endpoint:

```java
@PostMapping("/create")
public String createProduct(@ModelAttribute Product product, ...) {
    // Commission will be auto-applied via ProductService
    productService.saveProduct(product);
    return "redirect:/admin/products/list";
}
```

### Step 3: Update Vendor Product Controller

Modify the vendor product creation:

```java
@PostMapping("/create")
public String createVendorProduct(@ModelAttribute Product product, ...) {
    Long vendorId = getCurrentVendorId(); // Get from context
    productService.saveVendorProduct(product, vendorId);
    return "redirect:/vendor/products/list";
}
```

## Commission Hierarchy (Priority Order)

When determining applicable commission rate, the system checks in this order:

1. **Product-specific** commission (highest priority)
   - If a commission rate is set for this specific product
   
2. **Vendor-specific** commission
   - If no product-specific rate, check vendor's commission
   
3. **Category-specific** commission
   - If no vendor rate, check product's category commission
   
4. **Default** commission (fallback)
   - If no specific rates found, use default rate

**Example**:
- Default Commission: 10%
- Category "Electronics" Commission: 15%
- Vendor "TechStore" Commission: 12%
- Product "Samsung TV" Commission: 8%

When product "Samsung TV" from "TechStore" in "Electronics" is created:
- Commission applied: **8%** (Product specific)

If product commission is not set:
- Commission applied: **12%** (Vendor specific)

## Admin Usage Guide

### Setting Default Commission

1. Navigate to Admin Dashboard > Commission Settings
2. Click "Add New Commission Setting"
3. Select Commission Type: "DEFAULT"
4. Enter Commission Rate: e.g., 10.50
5. Status: ACTIVE
6. Save

### Setting Category-Specific Commission

1. Click "Add New Commission Setting"
2. Select Commission Type: "CATEGORY"
3. Enter Commission Rate: e.g., 15.00
4. Category ID: Enter the category ID
5. Save

### Setting Vendor-Specific Commission

1. Click "Add New Commission Setting"
2. Select Commission Type: "VENDOR"
3. Enter Commission Rate: e.g., 12.00
4. Vendor ID: Enter the vendor ID
5. Status: ACTIVE
6. Save

### Setting Product-Specific Commission

1. Click "Add New Commission Setting"
2. Select Commission Type: "PRODUCT"
3. Enter Commission Rate: e.g., 8.00
4. Product ID: Enter the product ID
5. Save

### Setting Time-Limited Commission

You can set temporary commission rates:
1. Create/Edit a setting
2. Set "Start Date" and "End Date"
3. Outside this period, the commission won't apply
4. Previous hierarchy rules will apply instead

## Database Migration

Run the SQL migration to create the table:

```sql
-- File location: src/main/resources/db/commission_settings_init.sql
```

Or if using Flyway/Liquibase, add it to your migration folder:
- Flyway: `src/main/resources/db/migration/V{version}__commission_settings.sql`

## Testing the Feature

### Test 1: Create Product with Default Commission
1. Ensure default commission is set (e.g., 10%)
2. Create a product without specifying commission
3. Verify: Product's `marketPlaceCommissionRate` = 10%

### Test 2: Category-Specific Commission
1. Set Category "Electronics" commission to 15%
2. Create a product in "Electronics" category
3. Verify: Product's `marketPlaceCommissionRate` = 15%

### Test 3: Vendor-Specific Commission
1. Set Vendor commission to 12%
2. Vendor creates a product in "Electronics" category (which has 15%)
3. Verify: Product's `marketPlaceCommissionRate` = 12% (Vendor takes precedence)

### Test 4: Product-Specific Commission
1. Set Product-specific commission to 8%
2. Verify when retrieving product order items

## API Usage Examples

### Check Applicable Commission via API

```bash
# Get commission for a vendor
curl "http://localhost:8080/api/commission/vendor/123"

# Get commission for a category
curl "http://localhost:8080/api/commission/category/456"

# Get applicable commission with all factors
curl "http://localhost:8080/api/commission/applicable-rate?vendorId=123&categoryId=456&productId=789"
```

## Performance Considerations

- Commission lookups use indexed queries
- Service caches rates via Spring's transaction context
- Typically very fast (<10ms per lookup)
- Consider caching if checking rates repeatedly in high-volume scenarios

## Future Enhancements

1. **Bulk Import**: Import commission settings from CSV
2. **Tiered Rates**: Different rates based on order amount
3. **Time-Based**: Schedule rate changes
4. **Audit Trail**: Track all commission changes
5. **Analytics**: Dashboard showing average commissions by category/vendor
6. **Commission Calculator**: Admin tool to preview calculated commissions

## Troubleshooting

### Products not getting commission applied
- Check if `CommissionSettingsService` is properly autowired in `ProductService`
- Verify `@Transactional` annotations are present
- Check database for `commission_settings` table

### API returns null commission
- Ensure default commission setting exists and is ACTIVE
- Check date ranges if time-limited commission is used
- Verify IDs are correct

### Admin page not showing
- Add menu item to `admin-nav-left.html`:
  ```html
  <li><a href="/admin/commission-settings/list">
    <i class="fas fa-percentage"></i> Commission Settings
  </a></li>
  ```

## File Structure Summary

```
src/
├── main/
│   ├── java/com/ecommerce/app/commission/
│   │   ├── model/
│   │   │   └── CommissionSettings.java
│   │   ├── repository/
│   │   │   └── CommissionSettingsRepository.java
│   │   ├── service/
│   │   │   ├── CommissionSettingsService.java
│   │   │   └── ProductCommissionApplierService.java
│   │   └── controller/
│   │       ├── CommissionSettingsController.java
│   │       └── CommissionSettingsApiController.java
│   └── resources/
│       ├── db/
│       │   └── commission_settings_init.sql
│       └── templates/admin/commission/
│           ├── settings_list.html
│           └── settings_form.html
```

## Support & Documentation

For questions or issues:
1. Check the logs for errors
2. Verify all required beans are registered
3. Ensure database migrations have run
4. Test using the REST API endpoints

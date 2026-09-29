# Codebase Exploration Summary

## 1. Product Creation and Save Flow

### Product Model
- **File**: `main/java/com/ecommerce/app/product/model/Product.java`
- **Key Fields**:
  - `id` (Long) - Primary key
  - `sku` (int) - Stock Keeping Unit
  - `uuid` (String) - Unique identifier
  - `userId` (ManyToOne) - User who created/owns the product
  - `vendorprofile` (ManyToOne) - Vendor who owns the product (nullable for admin products)
  - `productcategory` (ManyToOne) - Category assignment
  - Timestamps: `created`, `modified` (with audit trail)
  - Creator/Modifier tracking: `createdBy`, `modifiedBy`

### ProductService
- **File**: `main/java/com/ecommerce/app/product/services/ProductService.java`
- **Key Responsibilities**:
  - Handles all product business logic
  - Contains entity manager for advanced queries
  - Supports complex filtering, sorting, and searching
  - Works with products, variants, categories, and delivery options
  - Uses Criteria API for dynamic queries

### ProductController (Admin)
- **File**: `main/java/com/ecommerce/app/product/controller/ProductController.java`
- **Key Features**:
  - Handles product creation/editing by admins
  - Image upload and thumbnail generation using Thumbnailator
  - Product validation using @Valid annotation
  - Manages: Product images, dimensions, delivery options, attributes
  - Related Repositories: ProductRepository, ProductImageRepository, WarrantyRepository

---

## 2. Vendor Product Creation

### VendorProductController
- **File**: `main/java/com/ecommerce/app/vendor/controller/VendorProductController.java`
- **Request Mapping**: `/productvendor`
- **Key Features**:
  - Vendors create products via `/productvendor` endpoint
  - Uses `VendorUserContext` for vendor session management
  - Inherits same product creation logic as admin but:
    - Automatically links product to vendor via `vendorprofile`
    - Restricts products to active vendor only
    - Validates vendor ownership before operations
  - Services used:
    - `ProductService`
    - `UnitsOfMeasureService`
    - `CatalogProductAttributeService`
    - `ProductVariantCatalogService`
  - Image upload with same Thumbnailator processing

### Vendor Model
- **File**: `main/java/com/ecommerce/app/vendor/model/Vendorprofile.java`
- **Key Fields**:
  - `id` (Long) - Primary key
  - `uuid` (String) - Unique identifier
  - `vendorCode` (String) - Format: VEN-XX-YYYY-NNNN
  - `companyName` (String) - Store/company name
  - `userId` (OneToOne) - Primary vendor user
  - `users` (ManyToMany) - Multiple vendor staff members
  - `phone`, `email`, `address` - Contact information
  - `description` (Lob) - Detailed vendor information
  - `vendorStatusEnum` - Status of vendor (Active, Inactive, Suspended, etc.)
  - `vendorLogo` (String) - Logo path
  - Audit fields: `createdBy`, `created`, `modifiedBy`, `modified`

---

## 3. Current Commission Implementation

### MultiLavelRateSettings Model
- **File**: `main/java/com/ecommerce/app/module/ReferralRewards/model/MultiLavelRateSettings.java`
- **Table**: `promotions_multi_lavel_rate_settings`
- **Key Fields**:
  - `id` (Long) - Primary key
  - `level` (LevelEnum) - Commission level/tier
  - `amount` (BigDecimal) - Monetary threshold
  - `totalRef` (BigDecimal) - Total referrals required
  - `commissionRate` (BigDecimal) - Commission percentage/rate
  - Audit fields: `createdBy`, `created`, `modifiedBy`, `modified`

### MultiLavelRateSettingsController
- **File**: `main/java/com/ecommerce/app/module/ReferralRewards/controller/MultiLavelRateSettingsController.java`

### Related Commission Services
- `LavelRateSettingsService.java` - Service for managing rate settings
- `ReferralRewardService.java` - Handles referral rewards logic
- `PromotionReportingService.java` - Reports on promotions and commissions

### ReferralRewards Module Structure
- **Location**: `main/java/com/ecommerce/app/module/ReferralRewards/`
- **Key Components**:
  - **Services** (92 total in the module):
    - `WalletService.java` - Vendor wallet management
    - `RewardAccountService.java` - Reward account tracking
    - `CouponService.java` - Coupon management
    - `GiftCardService.java` - Gift card handling
    - `PromotionFraudService.java` - Fraud detection
    - `EmailService.java` - Notification emails
    - `RedemptionService.java` - Reward redemption
  - **Controllers**: For managing various commission/reward features
  - **Models**: Entities for the rewards module

---

## 4. Category Model and Structure

### Productcategory Model
- **File**: `main/java/com/ecommerce/app/product/model/Productcategory.java`
- **Table**: `productcategory`
- **Key Fields**:
  - `id` (Long) - Primary key
  - `uuid` (String) - Unique identifier
  - `name` (String, NotBlank) - Category name
  - `slug` (String) - URL-friendly name
  - `orderno` (int) - Display order
  - `description` (Lob) - Category description
  - `imageName` (String) - Category image
  - `discount` (double) - Category-level discount
  - `featuredCat` (Boolean) - Featured flag
  - `discountStartDate`, `discountEndDate` - Discount period
  - `status` (ProductStatusEnum) - Active/Inactive status
  - `parent` (ManyToOne) - Parent category (for hierarchical categories)
  - Audit fields: `createdBy`, `created`, `modifiedBy`, `modified`

### Related Classes
- **ProductcategoryService**: `main/java/com/ecommerce/app/product/services/ProductcategoryService.java`
- **ProductcategoryController**: `main/java/com/ecommerce/app/product/controller/ProductcategoryController.java`
- **ProductcategoryRepository**: `main/java/com/ecommerce/app/product/ripository/ProductcategoryRepository.java`

### Category Attributes
- **CategoryAttribute Model**: `main/java/com/ecommerce/app/product/model/CategoryAttribute.java`
- **CategoryAttributeService**: Manages attribute mappings to categories
- **CategoryAttributeRepository**: Data access layer

---

## 5. Vendor Model Structure

### Vendorprofile (Main Vendor Entity)
- **File**: `main/java/com/ecommerce/app/vendor/model/Vendorprofile.java`
- **Relationships**:
  - OneToOne: `Users userId` - Primary vendor owner
  - ManyToMany: `Set<Users> users` - Vendor staff members
  - Referenced by: `Product.vendorprofile`, `VendorWallet`, `VendorPayout`

### Related Vendor Models
- **VendorStatusEnum**: Vendor status (Active, Inactive, Suspended, etc.)
- **VendorWallet.java**: Vendor financial wallet
- **VendorTransaction.java**: Transaction history
- **VendorPayout.java**: Payout records
- **VendorPayoutMethod.java**: Payout method configuration
- **VendorPayoutStatusEnum**: Payout status tracking

### Vendor Services
- `VendorprofileService.java` - Main vendor management
- `VendorPayoutService.java` - Handles payouts
- `VendorFinanceService.java` - Financial operations
- `VendorDashboardService.java` - Dashboard data
- `VendorTransactionService.java` - Transaction management

### Vendor User Management
- **VendorRole.java**: Roles for vendor users
- **VendorPrivilege.java**: Privileges/permissions
- **VendorRoleService.java**: Role management
- **VendorPrivilegeService.java**: Privilege management

---

## 6. Admin Dashboard and Menu Structure

### Admin Controller
- **File**: `main/java/com/ecommerce/app/admin/controller/AdminController.java`
- **Base Route**: `/admin`
- **Key Endpoints**:
  - `/admin/`, `/admin/index` - Main dashboard
  - `/admin/returns-refunds`, `/admin/refunds` - Refund management
  - Requires `@PreAuthorize("hasAuthority('admin')")`

### AdminDashboardService
- **File**: `main/java/com/ecommerce/app/admin/services/AdminDashboardService.java`
- **Responsibilities**:
  - Builds dashboard data
  - Provides empty dashboard fallback
  - Aggregates various statistics and metrics

### Admin Layout Templates
- **Main Layout**: `main/resources/templates/admin-layout.html`
  - Uses Thymeleaf with layout decorator
  - Includes Bootstrap 5.3.1, Bootstrap Icons
  - Loads DataTables, FilePond for file uploads
  - Loads HTMX for dynamic content
  
### Admin Sidebar Navigation
- **File**: `main/resources/templates/admin-nav-left.html`
- **Structure**: Collapsible menu with main sections:

#### Main Menu Sections:
1. **Dashboard** - Main dashboard link
2. **Sales**
   - Orders
   - Transactions
   - Refunds
   - Returns

3. **Shipping & Fulfillment**
   - Carriers
   - Shipping Locations
   - Shipping Zones
   - Carrier Rates
   - Shipments
   - Shipping Profiles
   - Delivery Persons
   - Pickup Addresses
   - Shipping Rules
   - Labels
   - Manifests
   - Shipment Invoices
   - Packaging Rates

4. **Customers**
   - Manage Customers
   - New Customers
   - Manage Invoice
   - Customer Roles
   - Online Customers

5. **Vendor Management**
   - Vendor List (`/adminvendor/list`)
   - Vendor Payout List (`/admin/payouts/list`)
   - Finance Dashboard (`/admin/finance/dashboard`)
   - Settlement Ledger (`/admin/finance/settlements`)
   - Vendor Role List (`/adminvendorusers/rolelist`)
   - Vendor Privilege List (`/adminvendorusers/privilegeslist`)

6. **Product Catalog**
   - Product Category (`/productcategory/index`)
   - Catalog Attributes (`/catalog-attributes/list`)
   - Manufacturer (`/manufacturer/list`)
   - *(more items available)*

### Admin UI Components
- **Header**: `main/resources/templates/admin-nav-top_new.html`
- **Footer**: `main/resources/templates/admin-footer_new.html`
- **Dashboard Pages**: Located in `main/resources/templates/admin/`
  - Main dashboard: `admin/index.html`
  - Finance section: `admin/finance/`
  - Sales section: `admin/sales/`
  - Shipping section: `admin/shipping/`
  - Stock section: `admin/stock/`
  - Vendor section: `admin/vendor/`
  - Settings section: `admin/settings/`
  - Referral/Rewards: `admin/referral_rewards/`
  - Customer section: `admin/customer/`

---

## Key Package Structure

### Product Module
```
main/java/com/ecommerce/app/product/
├── model/          (Product, Productcategory, ProductAttribute, etc.)
├── services/       (ProductService, ProductcategoryService, etc.)
├── controller/     (ProductController, ProductcategoryController, etc.)
└── ripository/     (ProductRepository, ProductcategoryRepository, etc.)
```

### Vendor Module
```
main/java/com/ecommerce/app/vendor/
├── model/          (Vendorprofile, VendorWallet, VendorPayout, etc.)
├── services/       (VendorprofileService, VendorPayoutService, etc.)
├── controller/     (VendorProductController, VendorProfileController, etc.)
├── repository/     (VendorprofileRepository, etc.)
└── user/
    ├── model/      (VendorRole, VendorPrivilege, etc.)
    ├── services/   (VendorRoleService, VendorPrivilegeService, etc.)
    ├── controller/ (VendorRoleManagementController, etc.)
    └── componant/  (VendorUserContext, VendorRoleChecker, etc.)
```

### ReferralRewards Module
```
main/java/com/ecommerce/app/module/ReferralRewards/
├── model/          (MultiLavelRateSettings, LevelEnum, etc.)
├── services/       (90+ service classes)
├── controller/     (MultiLavelRateSettingsController, etc.)
└── repository/     (Data access layer)
```

### Admin Module
```
main/java/com/ecommerce/app/admin/
├── controller/     (AdminController.java)
└── services/       (AdminDashboardService.java)
```

---

## Key Observations for Feature Implementation

### 1. **Product Creation Flow**:
   - Products are created by both Admin and Vendors
   - Vendor products are auto-linked to `vendorprofile`
   - Admin products have `vendorprofile` as nullable
   - All products require a category

### 2. **Vendor Integration**:
   - Vendors have multi-user support via `VendorRole` and `VendorPrivilege`
   - Vendor context is managed via `VendorUserContext`
   - Vendor filtering is critical (ownership validation)

### 3. **Commission System**:
   - Uses multi-level rate settings with tiers
   - Supports referral-based rewards
   - Has wallet and transaction tracking
   - Includes fraud detection and reversal mechanisms

### 4. **Admin Dashboard**:
   - Highly modular with separate sections
   - Menu structure supports easy expansion
   - Uses Thymeleaf fragments for reusability
   - No Java configuration for menu structure (menu is in HTML template)

### 5. **Database Patterns**:
   - All entities use audit trail (`createdBy`, `created`, `modifiedBy`, `modified`)
   - UUID fields for external identifiers
   - Status enums for state management
   - Relationship tracking (ManyToOne, OneToOne, ManyToMany)


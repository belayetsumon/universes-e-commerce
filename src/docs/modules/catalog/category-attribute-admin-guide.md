# Category Attribute Administration Guide

## Purpose

This guide explains how to manage product categories, catalog attributes, category attribute mappings, dynamic product specifications, and catalog variants in this project.

The real flow in this system is:

1. Create a product category.
2. Create reusable attributes.
3. Add options for selectable attributes.
4. Map attributes to a category.
5. Create or edit products in that category.
6. Fill dynamic specifications.
7. Create catalog variants if needed.

## Main Concepts

### 1. Product Category

Product categories are managed from:

- `/productcategory/index`

Each category can have:

- `Name`
- `Parent Category`
- `Featured Category`
- `Order Position No`
- `Discount By Category`
- `Discount Start Date`
- `Discount End Date`
- `Description`
- `Status`
- `Image`

Important:

- Only categories with `Status = Active` are available in attribute mapping and product forms.
- `Featured Category` is used on the home page featured category area.

### 2. Attribute

Attributes are reusable fields like:

- Color
- Size
- RAM
- Storage
- Battery Capacity
- Warranty Type

Attributes are managed from:

- `/catalog-attributes/list`

Each attribute can define:

- `Name`
- `Code`
- `Description`
- `Input Type`
- `Value Type`
- `Allow multiple values`
- `Variant capable`
- `Filterable`
- `Searchable`
- `Comparable`
- `Active`

### 3. Attribute Option

Options are used for selectable attributes such as:

- Color -> Red, Blue, Black
- Size -> S, M, L, XL

Options are managed from the attribute `Options` screen:

- `/catalog-attributes/options/{attributeUuid}`

Each option can define:

- `Label`
- `Code`
- `Stored Value`
- `Description`
- `Sort Order`
- `Active`

### 4. Category Attribute Mapping

This is the connection between a category and an attribute.

Mappings are managed from:

- `/catalog-attributes/category-mappings`

Each mapping controls:

- Which attribute appears for a category
- Whether the field is required
- Whether the field should drive variants
- The display order
- Helper text shown on product forms
- Whether the mapping is active

### 5. Product Attribute

This is the actual saved value for a product after a user fills dynamic specifications on the product form.

### 6. Catalog Variant

Catalog variants are generated from mapped category attributes marked as `Variant Attribute`.

Examples:

- Color: Red + Size: XL
- Color: Black + Storage: 256 GB

Variants are managed from the product details page.

## Step By Step Administration

### Step 1: Create the Product Category

Open:

- `/productcategory/index`

Then click:

- `Add Product Category`

Fill the form carefully:

- `Name`: category name shown in admin and storefront
- `Parent Category`: use this if it is a child category
- `Featured Category`: check this if it should appear in featured category sections
- `Order Position No`: use this for sorting
- `Discount By Category`: optional category-level discount setting
- `Discount Start Date`: optional
- `Discount End Date`: optional
- `Description`: optional details
- `Status`: usually set to `Active`
- `Image`: optional category image

Best practice:

- Save with `Status = Active` if products will use this category now.
- Keep naming clear and stable before mapping attributes.

### Step 2: Open Catalog Attributes

Open:

- `/catalog-attributes/list`

You can reach it from the Product list page using the `Catalog Attributes` button.

This page shows:

- all existing attributes
- whether they are filterable
- whether they are marked variant-capable
- whether they are active

### Step 3: Create a Reusable Attribute

Click:

- `Add Attribute`

Fill the form:

- `Name`: user-friendly name
- `Code`: machine-friendly stable identifier
- `Description`: optional admin explanation
- `Input Type`: how the field appears on forms
- `Value Type`: how the value is stored and validated
- `Allow multiple values`: use only when the attribute should hold more than one value
- `Variant capable`: descriptive flag for variant-ready attributes
- `Filterable`: allows storefront category filters to use this attribute
- `Searchable`: metadata flag
- `Comparable`: metadata flag
- `Active`: enable the attribute

Recommended naming:

- Good `Code` examples:
- `color`
- `size`
- `battery_capacity`
- `screen_refresh_rate`

The system automatically normalizes codes to lowercase underscore format.

### Step 4: Choose Correct Input Type and Value Type

Use these combinations:

- `TEXT` + `TEXT` for short plain values
- `TEXTAREA` + `LONG_TEXT` for long descriptions
- `NUMBER` + `INTEGER` for whole numbers
- `DECIMAL` + `DECIMAL` for numeric values with decimals
- `BOOLEAN` + `BOOLEAN` for yes/no values
- `DATE` + `DATE` for date fields
- `SINGLE_SELECT` + `TEXT` for one selected option
- `MULTI_SELECT` + `TEXT` for multiple selected options

Important:

- `SINGLE_SELECT` and `MULTI_SELECT` should normally have attribute options.
- `Allow multiple values` is mainly useful for multiline text-style inputs and multi-value storage.

### Step 5: Add Options for Selectable Attributes

If the attribute uses:

- `SINGLE_SELECT`
- `MULTI_SELECT`

Then open its `Options` page.

Add each option with:

- `Label`: what users see
- `Code`: stable machine code
- `Stored Value`: saved text value
- `Description`: optional
- `Sort Order`: option ordering
- `Active`: enable the option

Example for `Color`:

- Label: `Red`
- Code: `red`
- Stored Value: `Red`

Example for `Storage`:

- Label: `128 GB`
- Code: `128_gb`
- Stored Value: `128 GB`

Best practice:

- Keep labels human-readable.
- Keep codes stable even if labels change later.
- Use sort order so the storefront and admin screens show logical option order.

### Step 6: Map Attributes to the Category

Open:

- `/catalog-attributes/category-mappings`

Choose the category first.

Then add one mapping per attribute.

Fill:

- `Category`
- `Attribute`
- `Attribute Group`
- `Helper Text`
- `Display Order`
- `Required`
- `Variant Attribute`
- `Active`

How to use the key fields:

- `Attribute Group`: visual grouping label like `Display`, `Performance`, `General`, `Fashion`
- `Helper Text`: instruction shown on the product form
- `Display Order`: smaller number appears first
- `Required`: product save should require the value
- `Variant Attribute`: this is the real switch that makes the attribute usable in catalog variants
- `Active`: keeps the mapping enabled

Important:

- If a category needs 5 specs, create 5 mappings.
- Parent category mappings do not automatically flow to child categories.
- If both parent and child categories need the same attribute, map it to both.

### Step 7: Create or Edit the Product

Admin product form:

- `/product/create`

Vendor product form:

- `/productvendor/create`

When you select a category, the system loads the `Dynamic Specifications` block automatically.

That block is built from the mapped category attributes.

Fill:

- normal product fields first
- then all dynamic specification fields

Examples:

- If category has `Color`, `Size`, `Material`, you will see those fields
- If category has `Battery Capacity` and `Screen Size`, you will see those fields

When you save the product:

- dynamic attributes are validated
- values are stored as product attribute values

### Step 8: Review Saved Dynamic Specifications

After saving the product, open the product details page.

The system shows a `Dynamic Specifications` table that lists:

- attribute name
- saved value

This is available on:

- admin product details
- vendor product details
- public single product page

### Step 9: Manage Catalog Variants

Open the product details page.

Use the `Generic Catalog Variants` section.

You will see two main actions:

- `Add Catalog Variant`
- `Auto Generate`

#### Manual Variant Creation

Use `Add Catalog Variant` when you want to create one specific combination.

Fill:

- `SKU`
- `Barcode`
- `Selling Price`
- `Special Price`
- `Stock Quantity`
- `Status`
- `Active`
- Variant option selections

The variant option selectors come only from category mappings marked as:

- `Variant Attribute = true`

#### Auto Generate Variants

Use `Auto Generate` when you want the system to create many combinations from selected options.

Example:

- Color: Red, Blue
- Size: M, L

The system can generate:

- Red + M
- Red + L
- Blue + M
- Blue + L

Important:

- If you leave a generate selector empty, the system includes all active options for that attribute.
- Duplicate combinations are blocked.
- Duplicate variant SKU values are blocked.

## What Each Flag Really Does Today

### Category Flags

- `Active`: controls whether the category is available for mapping and product selection
- `Featured Category`: used for storefront featured category display

### Attribute Flags

- `Active`: enables the attribute record
- `Filterable`: used by storefront category filters
- `Variant capable`: descriptive metadata, but variant generation actually depends on mapping-level `Variant Attribute`
- `Searchable`: exists as metadata, not strongly used in current admin flow
- `Comparable`: exists as metadata, not strongly used in current admin flow

### Mapping Flags

- `Required`: validates product submission
- `Variant Attribute`: powers variant selectors and generation
- `Active`: used by storefront filters and variant loading

## Storefront Effects

### On Category Pages

If an attribute is:

- mapped to the category
- active
- filterable
- option-based with active options

Then it can appear as a dynamic filter on:

- `/public/product-by-category/{prodcatid}`

### On Product Pages

Saved product specifications appear on:

- `/public/single-product/{prodid}`

### On Variant Selection

If a product has catalog variants, the public single product page shows attribute selectors and resolves the selected combination to the correct variant.

## Practical Examples

### Example 1: Clothing Category

Category:

- T-Shirts

Attributes:

- Color
- Size
- Fabric

Mappings:

- Color -> Required, Variant Attribute, Filterable
- Size -> Required, Variant Attribute, Filterable
- Fabric -> Optional, not variant

Result:

- product form shows color, size, fabric
- storefront can filter by color and size
- variants can be created like `Red + L`

### Example 2: Mobile Category

Category:

- Smartphones

Attributes:

- RAM
- Storage
- Battery Capacity
- Display Size
- Warranty

Mappings:

- RAM -> Variant Attribute if each RAM option has separate stock
- Storage -> Variant Attribute if each storage option has separate stock
- Battery Capacity -> normal specification
- Display Size -> normal specification
- Warranty -> normal specification

Result:

- variants can be based on `RAM + Storage`
- the rest stay as specifications only

## Best Practices

- Create categories before attributes are mapped.
- Reuse attributes instead of creating duplicates like `Color`, `Product Color`, `Item Color`.
- Use stable attribute codes from the beginning.
- Use options for anything that should be filtered or used for variants.
- Keep `Status = Active` for production categories.
- Keep `Active = true` on mappings and options that are in use.
- Use `Display Order` to keep product forms clean.
- Use `Helper Text` to guide vendors and admins.
- Test one product after every new mapping setup.

## Common Mistakes to Avoid

- Creating select attributes without adding options
- Forgetting to map the attribute to the category
- Expecting parent category mappings to apply automatically to child categories
- Marking an attribute as `Variant capable` but forgetting to set mapping `Variant Attribute`
- Using inactive categories for live product entry
- Expecting text attributes to behave like storefront checkbox filters

## Current System Caveats

- Mapping-level `Variant Attribute` is what really controls variants.
- Attribute-level `Variant capable` is visible in admin but is not the final decision point for variant generation.
- `Filterable` currently works best for selectable attributes with options.
- `Searchable` and `Comparable` are present but are not deeply enforced across all catalog behaviors yet.
- There is legacy migration support for old `Size` and `Color` data in code, but no direct admin screen button for it.

## Legacy Migration Note

The codebase includes an `AttributeMigrationService` that can:

- bootstrap legacy `Size`
- bootstrap legacy `Color`
- attach migrated attributes to categories

This is developer-level support and is not exposed as a standard admin button in the current UI.

## Recommended Admin Workflow

For any new category setup, use this order every time:

1. Create the category.
2. Activate the category.
3. Create reusable attributes.
4. Add options for select-type attributes.
5. Map attributes to the category.
6. Mark required and variant mappings correctly.
7. Create one sample product.
8. Check the dynamic specifications on product details.
9. If needed, create or auto-generate variants.
10. Check storefront filters and product display.

## Useful Routes

- Product categories: `/productcategory/index`
- Catalog attributes: `/catalog-attributes/list`
- Category mappings: `/catalog-attributes/category-mappings`
- Admin products: `/product/index`
- Admin product create: `/product/create`
- Vendor products: `/productvendor/index`
- Vendor product create: `/productvendor/create`


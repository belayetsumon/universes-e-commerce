-- PostgreSQL indexes for module.sales.dashboard aggregate queries.
-- Apply through the deployment migration process before enabling the dashboard
-- against a high-volume production order history.

CREATE INDEX IF NOT EXISTS idx_sales_order_created_status
    ON sales_order (created, status);

CREATE INDEX IF NOT EXISTS idx_sales_order_vendor_created_status
    ON sales_order (vendor_id, created, status);

CREATE INDEX IF NOT EXISTS idx_order_item_sales_order
    ON order_item (sales_order_id);

CREATE INDEX IF NOT EXISTS idx_order_item_product_sales_order
    ON order_item (product_id, sales_order_id);

CREATE INDEX IF NOT EXISTS idx_sales_order_customer_created
    ON sales_order (customer_id, created);

CREATE INDEX IF NOT EXISTS idx_payment_order_method_status
    ON payment (order_id, payment_method, payment_status);

CREATE INDEX IF NOT EXISTS idx_shipment_order_carrier_created
    ON shipment (sales_order_id, carrier_id, created);

CREATE INDEX IF NOT EXISTS idx_shipment_vendor_created_status
    ON shipment (vendor_id, created, status);

CREATE INDEX IF NOT EXISTS idx_product_category_brand_vendor
    ON product (productcategory_id, manufacturer_id, vendorprofile_id);

CREATE INDEX IF NOT EXISTS idx_shipping_address_order_region
    ON shipping_address (order_id, country, district, city);

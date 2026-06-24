# Shipping Module Update Workflow

This file tracks the step-by-step upgrade from the current shipping module to the fuller workflow described in `docs/Shiping_module.docx`.

## Current Goal

Upgrade shipping and fulfillment in controlled phases:

1. Add zone-based pricing.
2. Add weight slab pricing.
3. Add shipment tracking events.
4. Add vendor pickup addresses.
5. Add shipping rule/quote improvements.
6. Update admin/vendor screens.
7. Record testing evidence for each phase.

## Status Legend

- `Pending`: not started
- `In Progress`: currently being developed
- `Done`: implementation completed
- `Tested`: verified with documented test flow
- `Blocked`: waiting for clarification or dependency

## Phase 1: Foundation

Status: `Done`

Create these files:

- `main/java/com/ecommerce/app/module/shipping/model/ShippingZone.java`
- `main/java/com/ecommerce/app/module/shipping/model/CarrierRateSlab.java`
- `main/java/com/ecommerce/app/module/shipping/repository/ShippingZoneRepository.java`
- `main/java/com/ecommerce/app/module/shipping/repository/CarrierRateSlabRepository.java`
- `main/java/com/ecommerce/app/module/shipping/services/ShippingZoneService.java`
- `main/java/com/ecommerce/app/module/shipping/services/CarrierRateSlabService.java`
- `main/java/com/ecommerce/app/module/shipping/controller/ShippingZoneController.java`
- `main/java/com/ecommerce/app/module/shipping/controller/CarrierRateSlabController.java`
- `main/resources/templates/admin/shipping/zones/list.html`
- `main/resources/templates/admin/shipping/zones/form.html`
- `main/resources/templates/admin/shipping/carriers/carrier_rate_slab_list.html`
- `main/resources/templates/admin/shipping/carriers/carrier_rate_slab_form.html`

Update these files:

- `main/java/com/ecommerce/app/module/shipping/model/CarrierRate.java`
- `main/java/com/ecommerce/app/module/shipping/services/CarrierRateService.java`
- `main/java/com/ecommerce/app/module/shipping/services/ShippingOptionService.java`
- `main/java/com/ecommerce/app/module/shipping/controller/CarrierRateController.java`
- `main/resources/templates/admin/shipping/carriers/carriers_rate_form.html`
- `main/resources/templates/admin/shipping/carriers/carriers_rate_list.html`
- `main/resources/templates/admin-nav-left.html`

Implementation comments:

- Keep existing district-based rates working while adding zone support.
- Do not remove the current `district` field until old data is migrated.
- Carrier rate calculation should prefer slabs when slabs exist, otherwise fall back to the current base weight plus additional unit logic.

Test documentation:

- Create one zone named `Inside Dhaka`.
- Add `DHAKA` district to that zone.
- Create one carrier rate for the zone.
- Add slabs such as `0-1kg`, `1-2kg`, and `2-5kg`.
- Confirm checkout returns the expected shipping option for a Dhaka customer.
- Confirm old district-based carrier rates still work.

## Phase 2: Shipment Tracking Timeline

Status: `Done`

Create these files:

- `main/java/com/ecommerce/app/module/shipping/model/ShipmentTrackingEvent.java`
- `main/java/com/ecommerce/app/module/shipping/repository/ShipmentTrackingEventRepository.java`
- `main/java/com/ecommerce/app/module/shipping/services/ShipmentTrackingService.java`

Update these files:

- `main/java/com/ecommerce/app/module/shipping/model/Shipment.java`
- `main/java/com/ecommerce/app/module/shipping/model/ShipmentStatus.java`
- `main/java/com/ecommerce/app/module/shipping/services/ShipmentService.java`
- `main/java/com/ecommerce/app/module/shipping/services/ShippingServiceImpl.java`
- `main/java/com/ecommerce/app/module/shipping/controller/AdminShippingController.java`
- `main/java/com/ecommerce/app/vendor/controller/Vendor_ShipmentController.java`
- `main/resources/templates/admin/shipping/shipment_list.html`
- `main/resources/templates/vendor/shipments/list.html`

Implementation comments:

- Add tracking events whenever shipment status changes.
- Keep the main `Shipment.status` as the latest status snapshot.
- Add missing statuses only if the business flow needs them: `PICKUP_REQUESTED`, `PICKED_UP`, `OUT_FOR_DELIVERY`, `FAILED`.

Test documentation:

- Create shipment for a packed order.
- Change status from `PENDING` to `SHIPPED`.
- Confirm a tracking event is recorded.
- Change status to `IN_TRANSIT`.
- Confirm timeline order is correct.
- Confirm vendor can only see tracking for their own shipment.

## Phase 3: Vendor Pickup Address

Status: `Done`

Create these files:

- `main/java/com/ecommerce/app/module/shipping/model/PickupAddress.java`
- `main/java/com/ecommerce/app/module/shipping/repository/PickupAddressRepository.java`
- `main/java/com/ecommerce/app/module/shipping/services/PickupAddressService.java`
- `main/java/com/ecommerce/app/module/shipping/controller/PickupAddressController.java`

Update these files:

- `main/java/com/ecommerce/app/module/shipping/model/Shipment.java`
- `main/java/com/ecommerce/app/module/shipping/controller/AdminShippingController.java`
- `main/java/com/ecommerce/app/vendor/controller/Vendor_ShipmentController.java`
- `main/resources/templates/admin/shipping/admin_shipments_form.html`
- `main/resources/templates/vendor/shipments/form.html`
- `main/resources/templates/admin/shipping/pickup_addresses/list.html`
- `main/resources/templates/admin/shipping/pickup_addresses/form.html`
- `main/resources/templates/admin-nav-left.html`

Implementation comments:

- Each vendor can have multiple pickup addresses.
- One pickup address should be marked default per vendor.
- Shipment creation should be able to snapshot or reference the selected pickup address.

Test documentation:

- Create two pickup addresses for one vendor.
- Mark one as default.
- Create a shipment for that vendor.
- Confirm the default pickup address is available during shipment creation.

## Phase 4: Shipping Rules And Quote Engine

Status: `Done`

Create these files:

- `main/java/com/ecommerce/app/module/shipping/model/ShippingRule.java`
- `main/java/com/ecommerce/app/module/shipping/repository/ShippingRuleRepository.java`
- `main/java/com/ecommerce/app/module/shipping/services/ShippingRuleService.java`
- `main/java/com/ecommerce/app/module/shipping/services/ShippingQuoteService.java`
- `main/java/com/ecommerce/app/module/shipping/controller/ShippingRuleController.java`

Update these files:

- `main/java/com/ecommerce/app/module/shipping/services/ShippingOptionService.java`
- `main/java/com/ecommerce/app/module/cart/controller/CartsController.java`
- `main/java/com/ecommerce/app/module/cart/controller/CartController.java`
- `main/resources/templates/admin/shipping/rules/list.html`
- `main/resources/templates/admin/shipping/rules/form.html`

Implementation comments:

- `ShippingQuoteService` should become the main service for checkout shipping choices.
- Keep `ShippingOptionService` as a wrapper or migrate its logic gradually.
- Rules should support common actions first: disable carrier, disable COD, add extra fee, and set delivery priority.

Test documentation:

- Create a rule: weight above `10kg` disables one carrier.
- Add cart above `10kg`.
- Confirm disabled carrier does not appear.
- Create a rule: COD above configured amount disables COD.
- Confirm COD option is blocked or flagged.

## Phase 5: Labels, Manifest, Invoice

Status: `Done`

Create these files:

- `main/java/com/ecommerce/app/module/shipping/model/ShippingLabel.java`
- `main/java/com/ecommerce/app/module/shipping/model/ShippingManifest.java`
- `main/java/com/ecommerce/app/module/shipping/model/ShipmentInvoice.java`
- `main/java/com/ecommerce/app/module/shipping/repository/ShippingLabelRepository.java`
- `main/java/com/ecommerce/app/module/shipping/repository/ShippingManifestRepository.java`
- `main/java/com/ecommerce/app/module/shipping/repository/ShipmentInvoiceRepository.java`
- `main/java/com/ecommerce/app/module/shipping/services/ShippingDocumentService.java`
- `main/java/com/ecommerce/app/module/shipping/controller/ShippingDocumentController.java`

Update these files:

- `main/resources/templates/admin/shipping/documents/labels.html`
- `main/resources/templates/admin/shipping/documents/label_form.html`
- `main/resources/templates/admin/shipping/documents/manifests.html`
- `main/resources/templates/admin/shipping/documents/manifest_form.html`
- `main/resources/templates/admin/shipping/documents/invoices.html`
- `main/resources/templates/admin/shipping/documents/invoice_form.html`
- `main/java/com/ecommerce/app/vendor/controller/VendorShippingDocumentController.java`
- `main/resources/templates/vendor/shipping_documents/labels.html`
- `main/resources/templates/vendor/shipping_documents/manifests.html`
- `main/resources/templates/vendor/shipping_documents/manifest_form.html`
- `main/resources/templates/vendor/shipping_documents/invoices.html`
- `main/resources/templates/vendor-left-menu.html`
- `main/resources/templates/vendor/shipments/list.html`

Implementation comments:

- Labels can start as stored URL/text metadata before full PDF generation.
- Manifest should group multiple shipments for carrier handover.
- Invoice should snapshot shipment financial data.

Test documentation:

- Create two shipments.
- Generate a manifest.
- Confirm both shipments are linked.
- Generate or save label data.
- Confirm shipment invoice values match shipment settlement fields.
- Login as vendor and generate a label from `/vendor/shipping-documents/labels`.
- Create a vendor manifest from `/vendor/shipping-documents/manifests/create` with only active-vendor shipments.
- Generate a vendor shipment invoice from `/vendor/shipping-documents/invoices` and confirm settlement values match the shipment.

## Phase 6: Documentation Update

Status: `In Progress`

Update these files:

- `docs/Shiping_module.docx`
- `docs/functional-operations-guide.md`
- `docs/module-status.md`
- `docs/test-flow.md`

Implementation comments:

- `Shiping_module.docx` currently mixes old missing-feature notes with current architecture notes.
- Update it after each phase so it does not claim already implemented features are missing.
- Keep this workflow file as the implementation tracker.

Test documentation:

- Add manual test cases to `docs/test-flow.md`.
- Record tested date and result in `docs/module-status.md`.
- Keep failed cases with notes until fixed.

## Progress Log

| Date | Phase | Status | Notes |
|---|---|---|---|
| 2026-06-18 | Planning | Done | Created workflow from `Shiping_module.docx` comparison and current code review. |
| 2026-06-18 | Phase 1 | Done | Added shipping zones, carrier-rate slabs, zone admin screens, slab admin screens, zone-aware checkout rate matching, and slab-first rate calculation fallback. Markdown workflow, module status, and test docs were updated. Runtime/browser test and DOCX refresh are still pending. |
| 2026-06-23 | Phase 2 | Done | Tracking event entity, repository, service, status-change recording, and admin/vendor timeline display are present. Manual browser/database verification still pending. |
| 2026-06-23 | Phase 3 | Done | Added pickup address CRUD, one-default-per-vendor service behavior, pickup address shipment reference, and admin/vendor shipment form selection. |
| 2026-06-23 | Phase 4 | Done | Added shipping rules, quote service wrapper, checkout/cart API integration, and admin rule screens for disable carrier, disable COD, extra fee, and priority actions. |
| 2026-06-23 | Phase 5 | Done | Added label, manifest, and invoice entities/repositories/service/controller/screens. Shipment invoice snapshots settlement amounts. |
| 2026-06-23 | Hardening | Done | Vendor COD collection is now vendor-scoped, shipment carrier is optional when delivery person exists, and shipping-module deletes now use POST routes. |
| 2026-06-23 | Location Scale | Done | Added database-driven `ShippingLocation` hierarchy for country, division/state, district/city, and thana/upazila. Shipping zones now support coverage locations plus legacy district compatibility. |
| 2026-06-24 | Vendor Documents | Done | Added vendor panel label generation, manifest creation, shipment-invoice generation, vendor-scoped document queries, and menu shortcuts for the useful shipment handover flow. |

## Open Questions

- Should old district-based carrier rates remain permanently, or only during migration?
- Should vendors manage pickup addresses from the vendor panel as well, or remain admin-managed for the first release?
- Should real courier integrations start with Pathao, Steadfast, or RedX?
- When should checkout/customer address forms move from legacy district enum to shipping location IDs?

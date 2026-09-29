# Documentation Index

This folder is organized by document purpose and business module.

## Start Here

- [Feature Catalog](overview/feature-catalog.md) - code-derived module and sub-module feature inventory.
- [Functional Operations Guide](operations/functional-operations-guide.md) - main operating flows for marketplace, customer, vendor, order, shipping, finance, return, and reward workflows.
- [Module Status](project-management/module-status.md) - module progress and verification notes.
- [Requirement Changes](project-management/requirement-changes.md) - requirement change log.

## Folder Standard

| Folder | Purpose |
|---|---|
| `overview/` | High-level catalog and product overview documents. |
| `operations/` | Operator-facing guides and deployment-mode notes. |
| `project-management/` | Status, change tracking, and planning records. |
| `modules/` | Module-specific workflows and implementation guides. |
| `security/` | Security workflow docs, permission catalogues, endpoint inventories, and helper scripts. |
| `testing/` | Manual and focused test-flow documentation. |
| `source-documents/` | Original business/source `.docx` documents retained for reference. |

## Module Guides

- [Catalog](modules/catalog/category-attribute-admin-guide.md)
- [Checkout](modules/checkout/store-order-mode-guest-checkout-workflow.md)
- [Shipping](modules/shipping/shipping-module-update-workflow.md)
- [Fraud](modules/fraud/fraud-order-detection-workflow.md)
- [Communication](modules/communication/communication-module-workflow.md)
- [Marketing](modules/marketing/marketing-analytics-social-workflow.md)
- [Promotions and Incentives](modules/promotions/incentive-system-migration-plan.md)
- [Sales Dashboard](modules/sales/sales-dashboard-workflow.md)
- [Blog](modules/blog/blog-management-module-workflow.md)

## Security and Testing

- [Security Implementation Workflow](security/implementation-workflow.md)
- [Authentication and Authorization Workflow](security/application-security-authentication-authorization-workflow.md)
- [Application Security Endpoint Inventory](security/application-security-endpoint-inventory.csv)
- [Application Security Permission Catalogue](security/application-security-permission-catalogue.csv)
- [Test Flow](testing/test-flow.md)
- [Enterprise SQA Business Feature Matrix](testing/enterprise-sqa-business-feature-matrix.md)

## Cleanup Notes

- Older root-level `phase1-security-*` CSV files were removed because newer canonical application-security artifacts are retained under `security/`.
- The old empty `project-spec.md` placeholder was removed; use the feature catalog and functional guide as the active documentation entry points.

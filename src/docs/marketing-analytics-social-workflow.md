# Marketing, Analytics, and Social Sharing Workflow

## Assessment

| Area | Existing state | Reuse decision |
| --- | --- | --- |
| Global settings | `GlobalSettings` is the singleton settings entity with section forms, per-section POST handlers, optimistic locking, image upload handling, and cache invalidation. SEO already has meta title, meta description, OG title, OG description, OG image, GA ID, and Pixel ID. | Extend this module and keep the per-section save pattern. |
| Public layout | Shared storefront layouts are `front-layout-home.html`, `front-layout-inner-page.html`, plus mirrored `fronttheme/` variants. The active root layouts load `front-site.css` and `front-site.js`. | Add shared SEO/tracking fragments to the root public layouts first, then keep page-specific metadata as model attributes. |
| SEO/Open Graph | Basic description/title exists in layouts. No reusable OG fragment was present. | Add a reusable `fronttheme/fragments/social-metadata.html` fragment. |
| Existing Open Graph tags | No standard OG/Twitter set found in the active public root layouts. | Generate default and page-specific metadata through the new fragment. |
| Existing analytics scripts | Hard-coded Facebook Pixel noscript image existed in public layouts with a fixed Pixel ID. Old GA/Pixel ID fields existed in SEO settings. | Replace hard-coded snippets with settings-driven, consent-aware tracking fragments. |
| Referral module | `ReferralService`, `ReferralRepository`, and product share referral capture already exist. Product share URLs append `?ref=` only for authenticated customers and ignore self-referral on capture. | Reuse the public referral code and repository lookup. Do not expose IDs, emails, phones, UUIDs, or tokens. |
| Consent module | No complete cookie-consent module was found in the active public layout/assets. | Add a small public consent layer in the shared tracking service with necessary, analytics, marketing, and preferences categories. |
| Queue/message broker | Communication module has message job/retry/scheduler infrastructure, but analytics events are distinct from customer communications. | Add isolated async analytics delivery status table/service for Conversion API; do not block checkout/order flows. |
| Duplicate/conflicting tracking | Hard-coded Facebook Pixel fallback was present in two active layouts. | Remove hard-coded fallback and centralize script loading in one fragment/service. |

## Workflow Tracker

| Task | Status | Notes |
| --- | --- | --- |
| Codebase assessment | Done | Existing settings, layouts, analytics snippets, referral, and DB script style inspected. |
| Extend Global Settings fields | Done | Existing singleton settings entity now owns OG, sharing, Pixel, GA4, GTM, consent, and tracking mode fields. |
| Add validation and secret masking | Done | GA4, GTM, Pixel, Facebook App ID, HTTPS URL, and tracking mode validation added; masked Conversion API token preserves existing secret. |
| Add database migration | Done | `marketing_tracking_init.sql` adds idempotent settings columns and share/conversion delivery tables. |
| Add reusable metadata fragment | Done | `fronttheme/fragments/social-metadata.html` renders title, description, canonical, OG, and Twitter tags. |
| Add reusable social-sharing fragment | Done | Guest/customer sharing supports configured platforms, copy link, native share, accessibility labels, and backend tracking. |
| Add tracking JS service | Done | `window.AppTracking` handles consent checks, GA4, Pixel, GTM dataLayer, deduplication, page view, product view, and share events. |
| Add share tracking endpoint | Done | `/public/share-track` validates/rate limits and persists `SHARE_INITIATED` events. |
| Add admin analytics report | Done | `/admin/marketing/share-analytics` shows totals, platform/page splits, top products, top vendors, and date filters. |
| Add admin menu and diagnostics UI | Done | Configuration menu links directly to Marketing & Tracking; Reports links to Social Share Analytics; Marketing tab includes diagnostics and preview cards. |
| Add product single-page Thymeleaf layout | Done | `front-layout-single-product.html` centralizes single-product metadata/tracking assets. |
| Wire product metadata | Done | Product title, description, image fallback, canonical URL, price, currency, availability, and item tracking are supplied. |
| Add public SEO service and robots support | Done | `PublicSeoService` centralizes canonical URLs, robots directives, Open Graph defaults, and JSON-LD generation. |
| Wire product/category/blog/static metadata | Done | Product, product listing, category, blog list/category/detail, homepage, registration, auth utility, browsing-history, and static policy pages now receive explicit SEO metadata. |
| Add crawler/AI discovery endpoints | Done | Root `/robots.txt`, `/sitemap.xml`, and `/llms.txt` endpoints were added with canonical public URL guidance. |
| Wire vendor/referral/campaign/offer/gift-card metadata | Partial | Product referral-aware sharing is preserved. Dedicated public vendor, campaign, offer, and gift-card landing-page metadata still depends on those public page routes being exposed. |
| Conversion API async delivery | Partial | Delivery table/entity/repository and queueing service exist; order/registration/add-to-cart hooks and sender retry scheduler remain. |
| Tests | Pending | Maven unavailable in this environment; tests still need to be added/run from a machine with Maven. |
| Runtime/build verification | Blocked | `mvn` is not on PATH and no wrapper is present; static duplicate-tracking checks were run instead. |

## Configuration Notes

- Run `main/resources/db/marketing_tracking_init.sql` before saving the new marketing settings on an existing database.
- Configure `Public Website Base URL` as the production HTTPS origin, for example `https://example.com`. Metadata and share tracking reject localhost/private URLs for public use.
- Use `DIRECT` tracking mode to load GA4 and Facebook Pixel directly after consent.
- Use `GOOGLE_TAG_MANAGER` tracking mode to push normalized events to `window.dataLayer` and avoid duplicate direct GA4/Pixel script loading.
- Leave the Conversion API token field masked to keep the existing saved secret; enter a new value only when rotating the token.
- The share endpoint stores `SHARE_INITIATED`; it intentionally does not claim a successful provider-side post.

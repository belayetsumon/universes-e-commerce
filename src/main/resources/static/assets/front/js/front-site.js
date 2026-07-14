/*
 * Section: Front storefront
 * Used by: templates/front-layout-home.html and templates/front-layout-inner-page.html
 * Purpose: storefront-wide tracking, tabs, filters, cart interactions, modal/offcanvas helpers
 */
function initSiteLoadingProgress() {
    const loader = document.querySelector('[data-site-loader]');
    if (!loader) {
        return;
    }

    const progressBar = loader.querySelector('[data-site-loader-bar]');
    const percentText = loader.querySelector('[data-site-loader-percent]');
    const timeText = loader.querySelector('[data-site-loader-time]');
    const usePerformanceClock = window.performance && typeof window.performance.now === 'function';
    const startedAt = usePerformanceClock ? 0 : Date.now();
    let progress = 8;
    let isComplete = false;

    document.body.classList.add('site-is-loading');

    const render = function () {
        const safeProgress = Math.max(0, Math.min(100, Math.round(progress)));
        if (progressBar) {
            progressBar.style.width = safeProgress + '%';
        }
        if (percentText) {
            percentText.textContent = safeProgress + '%';
        }
        if (timeText) {
            const now = usePerformanceClock ? window.performance.now() : Date.now();
            const elapsedSeconds = Math.max(0, (now - startedAt) / 1000);
            timeText.textContent = elapsedSeconds.toFixed(1) + 's';
        }
    };

    const finish = function () {
        if (isComplete) {
            return;
        }
        isComplete = true;
        progress = 100;
        render();
        window.setTimeout(function () {
            loader.classList.add('is-complete');
            document.body.classList.remove('site-is-loading');
        }, 240);
    };

    render();
    const timer = window.setInterval(function () {
        if (isComplete) {
            window.clearInterval(timer);
            return;
        }
        const remaining = 94 - progress;
        progress += Math.max(0.4, remaining * 0.08);
        render();
    }, 120);

    window.addEventListener('load', finish, {once: true});
    window.addEventListener('pageshow', function (event) {
        if (event.persisted) {
            finish();
        }
    });
    window.addEventListener('beforeunload', function () {
        loader.classList.remove('is-complete');
        document.body.classList.add('site-is-loading');
        progress = 8;
        isComplete = false;
        render();
    });

    if (document.readyState === 'complete') {
        window.setTimeout(finish, 180);
    }
}

initSiteLoadingProgress();

function initStorefrontActiveMenu() {
    var currentPath = window.location.pathname || '/';
    var links = document.querySelectorAll('[data-menu-match]');
    if (!links.length) {
        return;
    }

    links.forEach(function (link) {
        var matchPath = link.getAttribute('data-menu-match') || '';
        var isHome = matchPath === '/';
        var isActive = isHome ? currentPath === '/' : currentPath.indexOf(matchPath) === 0;
        link.classList.toggle('active', isActive);
        if (isActive) {
            link.setAttribute('aria-current', 'page');
        } else {
            link.removeAttribute('aria-current');
        }
    });
}

initStorefrontActiveMenu();

// District and thana location picker
document.addEventListener('DOMContentLoaded', function () {
    var path = window.location.pathname;

    var blockedPages = [

        '/public/member-login',
        '/public/front-registration'
    ];

    var shouldBlockLocationModal = blockedPages.some(function (page) {
        return path.startsWith(page);
    });

    if (shouldBlockLocationModal) {
        return;
    }



    document.querySelectorAll('.location-picker-modal').forEach(function (pickerEl) {
        if (pickerEl.dataset.locationPickerReady === 'true') {
            return;
        }
        pickerEl.dataset.locationPickerReady = 'true';
        var modalEl = pickerEl.classList.contains('modal') ? pickerEl : pickerEl.closest('.modal');
        var districtSelect = pickerEl.querySelector('[data-location-district]');
        var thanaSelect = pickerEl.querySelector('[data-location-thana]');
        var saveBtn = pickerEl.querySelector('[data-location-save]');
        if (!modalEl || !districtSelect || !thanaSelect || !saveBtn) {
            return;
        }

        var locationModal = bootstrap.Modal.getOrCreateInstance(modalEl);
        var currentDistrict = pickerEl.dataset.currentDistrict || modalEl.dataset.currentDistrict || '';
        var currentLocation = pickerEl.dataset.currentLocation || modalEl.dataset.currentLocation || '';
        function resetThanas(message) {
            thanaSelect.innerHTML = '';
            var option = document.createElement('option');
            option.value = '';
            option.textContent = message;
            thanaSelect.appendChild(option);
            thanaSelect.disabled = true;
        }

        function loadThanas(districtId, selectedThanaId) {
            resetThanas('Loading thanas...');
            fetch('/district/thanas?districtId=' + encodeURIComponent(districtId))
                    .then(function (res) {
                        if (!res.ok) {
                            throw new Error('Network response was not OK');
                        }
                        return res.json();
                    })
                    .then(function (thanas) {
                        resetThanas(thanas.length ? 'Select Thana' : 'No thana available');
                        thanas.forEach(function (thana) {
                            var option = document.createElement('option');
                            option.value = thana.id;
                            option.textContent = thana.name;
                            thanaSelect.appendChild(option);
                        });
                        thanaSelect.disabled = thanas.length === 0;
                        if (selectedThanaId) {
                            thanaSelect.value = selectedThanaId;
                        }
                    })
                    .catch(function (err) {
                        console.error('Error loading thanas:', err);
                        resetThanas('Unable to load thanas');
                    });
        }

        if (currentDistrict) {
            districtSelect.value = currentDistrict;
            loadThanas(currentDistrict, currentLocation);
        }

        if (modalEl.dataset.show && modalEl.dataset.show.toLowerCase() === 'true') {
            locationModal.show();
        }

        districtSelect.addEventListener('change', function () {
            if (this.value) {
                loadThanas(this.value, '');
            } else {
                resetThanas('Select Thana');
            }
        });
        saveBtn.addEventListener('click', function () {
            var district = districtSelect.value;
            var thana = thanaSelect.value;
            if (!district) {
                alert('Please select a district.');
                return;
            }
            if (!thanaSelect.disabled && !thana) {
                alert('Please select a thana.');
                return;
            }

            fetch('/district/save-district', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'location=' + encodeURIComponent(thana || district)
            })
                    .then(function (res) {
                        if (!res.ok) {
                            throw new Error('Network response was not OK');
                        }
                        return res.text();
                    })
                    .then(function (data) {
                        if (data === 'success') {
                            locationModal.hide();
                            window.location.reload();
                        } else {
                            alert('Invalid location selection');
                        }
                    })
                    .catch(function (err) {
                        console.error('Fetch error:', err);
                        alert('Unable to save location. Please try again.');
                    });
        });
    });
});
// Date: 2026-04-26
// Persist active Bootstrap tabs on pages like single-product.
// The previous code only targeted #myTab buttons, while this page uses
// <a data-bs-toggle="tab"> inside #productTabs, so tab state and activation
// could drift or stop working after reloads.
document.addEventListener('DOMContentLoaded', function () {
    const tabContainers = document.querySelectorAll('.nav-tabs[id]');
    tabContainers.forEach((tabContainer) => {
        const tabTriggers = Array.from(tabContainer.querySelectorAll('[data-bs-toggle="tab"]'));
        if (!tabTriggers.length || typeof bootstrap === 'undefined' || !bootstrap.Tab) {
            return;
        }

        const storageKey = 'activeTab:' + tabContainer.id;
        const getTargetSelector = (trigger) => trigger.getAttribute('data-bs-target') || trigger.getAttribute('href');
        const savedTarget = sessionStorage.getItem(storageKey);
        const matchingSavedTrigger = savedTarget
                ? tabTriggers.find((trigger) => getTargetSelector(trigger) === savedTarget)
                : null;
        const initialTrigger = matchingSavedTrigger || tabTriggers.find((trigger) => trigger.classList.contains('active')) || tabTriggers[0];
        if (initialTrigger) {
            bootstrap.Tab.getOrCreateInstance(initialTrigger).show();
        }

        tabTriggers.forEach((trigger) => {
            trigger.addEventListener('shown.bs.tab', function (event) {
                const targetSelector = getTargetSelector(event.target);
                if (targetSelector) {
                    sessionStorage.setItem(storageKey, targetSelector);
                }
            });
        });
    });
});
$(document).ready(function () {

//alert("Hi");



//    $('.slider-for').slick({
//        slidesToShow: 1,
//        slidesToScroll: 1,
//        arrows: false,
//        fade: true,
//        asNavFor: '.slider-nav'
//    });

//    $('.slider-nav').slick({
//        slidesToShow: 4,
//        slidesToScroll: 1,
//        arrows: true,
//        dots: false,
//        centerMode: false,
//        focusOnSelect: true
//    });
//    $('.slider-nav').slick({
//        slidesToShow: 4,
//        slidesToScroll: 1,
//        asNavFor: '.slider-for',
//        dots: false,
//        centerMode: true,
//        focusOnSelect: true
//    });


    // Date: 2026-04-26
    // Restore product thumbnail click behavior on single-product page.
    $('.slider-nav').on('click', 'img[data-img]', function () {
        const newSrc = $(this).data('img');
        const mainImage = $('#mainImage');
        if (!newSrc || !mainImage.length) {
            return;
        }
        mainImage.attr('src', newSrc);
    });
});
// Date: 2026-04-26
// Make category filters feel instantaneous while keeping price fields explicit.
document.addEventListener('change', function (event) {
    var target = event.target;
    var filterForm = target.closest('.category-filter-form');
    if (!filterForm) {
        return;
    }

    if (target.matches('input[type="checkbox"], input[type="radio"], select')) {
        if (typeof filterForm.requestSubmit === 'function') {
            filterForm.requestSubmit();
        } else {
            filterForm.submit();
        }
    }
});
document.addEventListener('click', function (event) {
    var clearButton = event.target.closest('.filter-clear-link');
    if (!clearButton) {
        return;
    }

    var filterForm = clearButton.closest('form');
    var groupName = clearButton.dataset.clearGroup;
    if (!filterForm || !groupName) {
        return;
    }

    filterForm.querySelectorAll('[name="' + groupName + '"]').forEach(function (input) {
        if (input.type === 'checkbox' || input.type === 'radio') {
            input.checked = false;
        } else {
            input.value = '';
        }
    });
    if (typeof filterForm.requestSubmit === 'function') {
        filterForm.requestSubmit();
    } else {
        filterForm.submit();
    }
});
document.addEventListener('htmx:beforeRequest', function (event) {
    var requestElement = event.detail && event.detail.elt ? event.detail.elt : event.target;
    if (!requestElement || requestElement.id !== 'productFilterForm') {
        return;
    }

    var offcanvasEl = document.getElementById('categoryFilterOffcanvas');
    if (!offcanvasEl || !offcanvasEl.classList.contains('show') || typeof bootstrap === 'undefined') {
        return;
    }

    var offcanvasInstance = bootstrap.Offcanvas.getOrCreateInstance(offcanvasEl);
    offcanvasInstance.hide();
});
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/javascript.js to edit this template
 */


//alert("Hi Cart");
/**
 * Date: 2026-04-20
 * Debug marker:
 * If you open browser DevTools Console, you should see this line on the Cart page.
 * This confirms the active storefront bundle is loading.
 */
console.log("[cart] storefront cart bundle loaded (2026-05-25)");
const debounce = (fn, delay = 100) => {
    let t;
    return (...args) => {
        clearTimeout(t);
        t = setTimeout(() => fn(...args), delay);
    };
};
const formatCurrency = num => `৳ ${parseFloat(num || 0).toFixed(2)}`;
const escapeHtml = value => String(value ?? '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
/**
 * ============================================
 * Cart totals update (Shipping + Packaging)
 * Date: 2026-04-20
 *
 * What changed and why:
 * - Previously the shipping <select> used option.value = price (client-controlled).
 *   That is unsafe and also prevents correct server-side calculation by vendor/location/weight.
 * - Now we send:
 *   - shippingOptionCode (CarrierRate UUID) for shipping
 *   - packagingRateUuid (PackagingRate UUID) for packaging
 * - The server stores vendor-wise costs in session and returns updated cart JSON.
 * - UI label now shows: Company + Speed + ETA (estimatedDelivery).
 * ============================================
 */

// Helper: POST form-urlencoded and parse JSON response.
async function postForm(url, bodyObj) {
    const body = new URLSearchParams();
    Object.entries(bodyObj || {}).forEach(([k, v]) => body.append(k, v ?? ""));
    const res = await fetch(url, {
        method: "POST",
        // Date: 2026-04-20
        // Ensure session cookie (JSESSIONID) is sent so vendor-wise shipping/packaging
        // selections persist in HttpSession.
        credentials: "same-origin",
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body
    });
    if (!res.ok) {
        const msg = await res.text();
        let errorMessage = msg || "Request failed";
        try {
            const json = JSON.parse(msg);
            errorMessage = json.message || errorMessage;
        } catch (ignored) {
            // Keep the plain server response when it is not JSON.
        }
        throw new Error(errorMessage);
    }
    return res.json();
}
// ---------- Load Cart ----------
async function loadCart() {
    try {
        // Date: 2026-04-20
        // `cart.js` is included globally in the layout, so it can run on pages
        // that do NOT have the cart DOM. In that case, exit safely.
        const container = document.getElementById('cartContainer');
        if (!container) {
            return;
        }

        const res = await fetch('/carts/api', {
            // Date: 2026-04-20
            // Ensure cart reads/writes use the same session.
            credentials: "same-origin"
        });
        if (!res.ok)
            throw new Error('Failed to load cart');
        const data = await res.json();
        //console.log(data);

        container.innerHTML = '';
        // ---------- Empty cart ----------
        if (!data?.groupedCart || Object.keys(data.groupedCart).length === 0) {
            container.innerHTML = `
                <div class="empty-cart text-center">
                    <div class="cart-empty-icon"><i class="bi bi-bag"></i></div>
                    <h2>Your cart is empty</h2>
                    <p>Add products to begin checkout.</p>
                    <a href="/public/product" class="btn btn-danger btn-continue">
                        <i class="bi bi-grid"></i>
                        Continue Shopping
                    </a>
                </div>
            `;
            const grandTotalEl = document.getElementById('grandTotal');
            if (grandTotalEl) {
                grandTotalEl.textContent = '';
            }
            return;
        }

        // ---------- Vendors ----------
        for (const [vendorUuid, items] of Object.entries(data.groupedCart)) {

            if (!items || items.length === 0)
                continue;
            const vendorDiv = document.createElement('div');
            vendorDiv.classList.add('vendor');
            vendorDiv.dataset.vendorUuid = vendorUuid;
            const vendorName =
                    items[0]?.product?.vendorprofile?.companyName || 'Vendor';
            const shippingOptions = data.vendorShippingOptions?.[vendorUuid] || [];
            const packagingOptions = data.vendorPackagingOptions?.[vendorUuid] || [];
            const shippingCost = data.vendorShippingCost?.[vendorUuid] || 0;
            const packagingCost = data.vendorPackagingCost?.[vendorUuid] || 0;
            const subtotal = data.vendorSubtotals?.[vendorUuid] || 0;
            const vendorRequiresShipping = data.vendorRequiresShipping?.[vendorUuid] !== false;
            // Persist selections by ID (so selection stays correct after re-render).
            // Date: 2026-04-20
            const selectedShippingOptionCode = data.selectedShippingOption?.[vendorUuid] || '';
            const selectedPackagingRateUuid = data.selectedPackagingRate?.[vendorUuid] || '';
            vendorDiv.innerHTML = `
                <div class="vendor-header">
                    <div>
                        <div class="vendor-label">Seller</div>
                        <h3>${escapeHtml(vendorName)}</h3>
                    </div>
                    <span class="vendor-item-count">${items.length} item${items.length === 1 ? '' : 's'}</span>
                </div>

                <div class="cart-table-wrap">
                    <table class="table cart-items-table align-middle">
                        <thead>
                            <tr>
                                <th>Product</th>
                                <th class="text-end">Price</th>
                                <th class="text-center">Qty</th>
                                <th class="text-end">Discount</th>
                                <th class="text-end">VAT</th>
                                <th class="text-end">Total</th>
                                <th class="text-end">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${items.map((item, idx) => `
                                <tr>
                                    <td data-label="Product">
                                        <div class="cart-product-cell">
                                            <img class="cart-product-image"
                                                 src="${escapeHtml(item.product?.imageUrl || '/images/no-image.png')}"
                                                 alt="${escapeHtml(item.product?.title || 'Product')}"/>
                                            <div class="cart-product-meta">
                                                <div class="cart-product-index">Item ${idx + 1}</div>
                                                <a class="cart-product-title" href="/public/single-product/${encodeURIComponent(item.productUuid || '')}">
                                                    ${escapeHtml(item.product?.title || 'Product')}
                                                </a>
                                                <div class="cart-product-variant">${escapeHtml(item.variantSummary || 'Standard')}</div>
                                                <div class="cart-product-specs">
                                                    <span>${escapeHtml(item.uom?.name || '-')}</span>
                                                    <span>${Number(item.weight || 0).toFixed(2)} kg</span>
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                    <td class="text-end" data-label="Price">${formatCurrency(item.salesPrice || 0)}</td>
                                    <td class="text-center" data-label="Qty">
                                        <input type="number"
                                               class="qty-input form-control form-control-sm"
                                               data-product-uuid="${escapeHtml(item.productUuid || '')}"
                                               data-catalog-variant-uuid="${escapeHtml(item.catalogVariantUuid || '')}"
                                               value="${escapeHtml(item.quantity || 1)}"
                                               min="1"/>
                                    </td>
                                    <td class="text-end" data-label="Discount">${escapeHtml(item.discountRate || 0)}%</td>
                                    <td class="text-end" data-label="VAT">${escapeHtml(item.vatRate || 0)}%</td>
                                    <td class="item-total text-end" data-label="Total">
                                        ${formatCurrency(item.itemTotal || 0)}
                                    </td>
                                    <td class="text-end" data-label="Action">
                                        <button type="button" class="remove-btn btn btn-sm btn-outline-danger"
                                                data-product-uuid="${escapeHtml(item.productUuid || '')}"
                                                data-catalog-variant-uuid="${escapeHtml(item.catalogVariantUuid || '')}"
                                                title="Remove item">
                                            <i class="bi bi-trash"></i>
                                        </button>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>

                <div class="vendor-controls">
                    <div class="vendor-options">
                        <div class="cart-field">
                            <label>Shipping</label>
                            <select class="shipping-select form-select"
                                    data-vendor-uuid="${escapeHtml(vendorUuid)}"
                                    ${vendorRequiresShipping ? '' : 'disabled'}
                                    onchange="window.cartOnShippingChange && window.cartOnShippingChange(this)">
                                <option value="0" ${!selectedShippingOptionCode ? 'selected' : ''}>
                                    ${vendorRequiresShipping ? 'Select shipping company' : 'No shipping required'}
                                </option>
                                ${shippingOptions.length
                    ? shippingOptions.map(opt => `
                                    <option value="${escapeHtml(opt.code)}"
                                        ${(opt.code && opt.code === selectedShippingOptionCode) ? 'selected' : ''}>
                                        ${escapeHtml(opt.title)} - ${formatCurrency(opt.price)} (${escapeHtml(opt.estimatedDelivery || '')})
                                    </option>
                                `).join('')
                    : `<option value="0">No shipping</option>`
                    }
                            </select>
                        </div>

                        <div class="cart-field">
                            <label>Packaging</label>
                            <select class="packaging-select form-select"
                                    data-vendor-uuid="${escapeHtml(vendorUuid)}"
                                    ${vendorRequiresShipping ? '' : 'disabled'}
                                    onchange="window.cartOnPackagingChange && window.cartOnPackagingChange(this)">
                                <option value="0" ${!selectedPackagingRateUuid ? 'selected' : ''}>
                                    ${vendorRequiresShipping ? 'Select packaging type' : 'No packaging required'}
                                </option>
                                ${packagingOptions.length
                    ? packagingOptions.map(pack => {
                        const price = (pack.basePrice || 0) + (pack.additionalPrice || 0);
                        return `
                                    <option value="${escapeHtml(pack.uuid)}"
                                        ${(pack.uuid != null && String(pack.uuid) === String(selectedPackagingRateUuid)) ? 'selected' : ''}>
                                        ${escapeHtml(pack.packagingType)} - ${formatCurrency(price)}
                                    </option>`;
                    }).join('')
                    : `<option value="0">No packaging</option>`
                    }
                            </select>
                        </div>
                        ${vendorRequiresShipping ? '' : '<div class="cart-muted-note">Virtual products do not require shipping or packaging.</div>'}
                    </div>

                    <div class="vendor-summary">
                        <div><span>Subtotal</span><strong>${formatCurrency(subtotal)}</strong></div>
                        <div><span>Shipping</span><strong>${formatCurrency(shippingCost)}</strong></div>
                        <div><span>Packaging</span><strong>${formatCurrency(packagingCost)}</strong></div>
                        <div class="vendor-summary-total">
                            <span>Total</span><strong>${formatCurrency(subtotal + shippingCost + packagingCost)}</strong>
                        </div>
                    </div>
                </div>
            `;
            container.appendChild(vendorDiv);
        }

        const grandTotalEl = document.getElementById('grandTotal');
        if (grandTotalEl) {
            grandTotalEl.innerHTML =
                    `<span>Grand total</span><strong>${formatCurrency(data.grandTotal || 0)}</strong>`;
        }

        /**
         * ============================================
         * Right-side Order Summary live update
         * Date: 2026-04-20
         *
         * The sidebar totals in `templates/cart/index.html` are rendered by Thymeleaf
         * on initial page load, so they won't change when user updates shipping/packaging.
         * We update them here from the fresh `/carts/api` JSON.
         * ============================================
         */
        const orderSubTotalEl = document.getElementById("orderSubTotal");
        const orderTotalEl = document.getElementById("orderTotal");
        // Subtotal = sum of all vendor subtotals (without shipping/packaging)
        const vendorSubtotals = data.vendorSubtotals || {};
        const subTotalAll = Object.values(vendorSubtotals)
                .reduce((acc, v) => acc + parseFloat(v || 0), 0);
        if (orderSubTotalEl) {
            orderSubTotalEl.textContent = parseFloat(subTotalAll || 0).toFixed(2);
        }
        if (orderTotalEl) {
            orderTotalEl.textContent = parseFloat(data.grandTotal || 0).toFixed(2);
        }
        //  attachCartEvents();

    } catch (err) {
        console.error('Cart load failed', err);
    }
}

/**
 * ============================================
 * Fallback onchange handlers (global)
 * Date: 2026-04-20
 *
 * If delegated events don't fire in some browsers/layouts,
 * these functions are called directly from the <select onchange="...">.
 * ============================================
 */
window.cartOnShippingChange = async function (selectEl) {
    try {
        // console.log("hello");
        const vendorUuid = selectEl?.dataset?.vendorUuid;
        const shippingOptionCode = selectEl?.value || "";
        // console.log("[cart] shipping onchange", {vendorUuid, shippingOptionCode});
        if (!vendorUuid)
            return;
        await postForm("/carts/updateShippingOption", {vendorUuid, shippingOptionCode});
        await loadCart();
    } catch (err) {
        /// console.error(err);
        alert(err?.message || "Shipping update failed");
    }
};
window.cartOnPackagingChange = async function (selectEl) {
    try {
        const vendorUuid = selectEl?.dataset?.vendorUuid;
        const packagingRateUuid = selectEl?.value || "";
        // console.log("[cart] packaging onchange", {vendorUuid, packagingRateUuid});
        if (!vendorUuid)
            return;
        await postForm("/carts/updatePackagingRate", {vendorUuid, packagingRateUuid});
        await loadCart();
    } catch (err) {
        // console.error(err);
        alert(err?.message || "Packaging update failed");
    }
};

function initCheckoutAvailabilityGate() {
    const checkoutButtons = document.querySelectorAll('[data-checkout-start]');
    if (!checkoutButtons.length) {
        return;
    }
    const unavailableAlert = document.querySelector('[data-checkout-unavailable-alert]');
    const authModalEl = document.getElementById('checkoutAuthModal');
    const guestOption = document.querySelector('[data-checkout-guest-option]');
    const guestDivider = document.querySelector('[data-checkout-guest-divider]');
    const authCopy = document.querySelector('[data-checkout-auth-copy]');
    const unavailableMessage = 'Checkout is currently unavailable. Purchasing has been temporarily disabled by the store administrator. Please try again later or contact customer support for assistance.';

    function showUnavailable(message) {
        if (unavailableAlert) {
            unavailableAlert.textContent = message || unavailableMessage;
            unavailableAlert.classList.remove('d-none');
            unavailableAlert.scrollIntoView({behavior: 'smooth', block: 'center'});
            return;
        }
        alert(message || unavailableMessage);
    }

    checkoutButtons.forEach(function (checkoutButton) {
        checkoutButton.addEventListener('click', async function () {
        checkoutButton.disabled = true;
        if (unavailableAlert) {
            unavailableAlert.classList.add('d-none');
        }
        try {
            const response = await fetch('/cart/checkout/availability', {
                credentials: 'same-origin',
                headers: {'Accept': 'application/json'}
            });
            if (!response.ok) {
                throw new Error(unavailableMessage);
            }
            const availability = await response.json();
            if (!availability.checkoutAvailable) {
                showUnavailable(availability.message);
                return;
            }
            if (availability.showAuthenticationModal) {
                if (guestOption) {
                    guestOption.classList.toggle('d-none', !availability.guestAllowed);
                }
                if (guestDivider) {
                    guestDivider.classList.toggle('d-none', !availability.guestAllowed);
                }
                if (authCopy) {
                    authCopy.textContent = availability.guestAllowed
                            ? 'Sign in to use your saved account details, or continue as a guest with mobile verification.'
                            : 'Sign in to continue checkout. Guest checkout is currently disabled by the store administrator.';
                }
                if (authModalEl && window.bootstrap && window.bootstrap.Modal) {
                    window.bootstrap.Modal.getOrCreateInstance(authModalEl).show();
                    return;
                }
                window.location.href = availability.guestAllowed ? '/cart/checkout' : '/public/member-login';
                return;
            }
            window.location.href = availability.nextUrl || '/cart/checkout';
        } catch (error) {
            showUnavailable(error && error.message ? error.message : unavailableMessage);
        } finally {
            checkoutButton.disabled = false;
        }
        });
    });
}
/**
 * ============================================
 * Event handling for shipping & packaging selects
 * Date: 2026-04-20
 *
 * We use event delegation because `loadCart()` rebuilds the DOM each time.
 * On change we ask the server to recalculate vendor-wise shipping/packaging,
 * then re-load the cart to update vendor totals + grand total.
 * ============================================
 */
document.addEventListener("change", async (e) => {
    const shipSel = e.target.closest(".shipping-select");
    const packSel = e.target.closest(".packaging-select");
    const qtyInput = e.target.closest(".qty-input");
    if (!shipSel && !packSel && !qtyInput)
        return;
    const vendorUuid = e.target.dataset.vendorUuid;
    const vendorDiv = e.target.closest(".vendor");
    vendorDiv?.classList.add("vendor-loading");
    try {
        if (qtyInput) {
            const productUuid = qtyInput.dataset.productUuid;
            const catalogVariantUuid = qtyInput.dataset.catalogVariantUuid || "";
            const quantity = qtyInput.value || "1";
            await postForm("/carts/updateQuantity", {productUuid, catalogVariantUuid, quantity});
        } else if (!vendorUuid) {
            return;
        } else if (shipSel) {
            const shippingOptionCode = shipSel.value || "";
            // Date: 2026-04-20
            // Debug message to confirm the change handler is firing.
            // alert("hello");
            // console.log("[cart] shipping change", {vendorUuid, shippingOptionCode});
            await postForm("/carts/updateShippingOption", {vendorUuid, shippingOptionCode});
        }
        if (packSel) {
            const packagingRateUuid = packSel.value || "";
            // console.log("[cart] packaging change", {vendorUuid, packagingRateUuid});
            await postForm("/carts/updatePackagingRate", {vendorUuid, packagingRateUuid});
        }
        await loadCart();
    } catch (err) {
        // console.error(err);
        alert(err?.message || "Update failed");
        if (qtyInput) {
            await loadCart();
        }
    } finally {
        vendorDiv?.classList.remove("vendor-loading");
    }
});
document.addEventListener('click', async function (e) {

    const btn = e.target.closest('.remove-btn');
    if (!btn)
        return; // Not a delete button
    e.preventDefault(); // stop form submit
    e.stopPropagation(); // stop bubbling
    const productUuid = btn.dataset.productUuid;
    const catalogVariantUuid = btn.dataset.catalogVariantUuid || '';
    if (!productUuid)
        return;
    if (!productUuid || productUuid === "undefined") {
        alert("Product UUID missing. Cannot remove item.");
        return;
    }
    if (!confirm('Remove this item from cart?'))
        return;
    const vendorDiv = btn.closest('.vendor');
    vendorDiv?.classList.add('vendor-loading');
    try {
        const res = await fetch('/carts/removeitem', {
            method: 'POST',
            // Date: 2026-04-20
            // Ensure remove action uses same session.
            credentials: "same-origin",
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: `productUuid=${encodeURIComponent(productUuid)}&catalogVariantUuid=${encodeURIComponent(catalogVariantUuid)}`
        });
        if (!res.ok)
            throw new Error('Server error while removing');
        alert('Item removed successfully');
        await loadCart();
    } catch (err) {
        alert(err.message);
    } finally {
        vendorDiv?.classList.remove('vendor-loading');
    }
});
document.addEventListener('DOMContentLoaded', loadCart);
document.addEventListener('DOMContentLoaded', initCheckoutAvailabilityGate);

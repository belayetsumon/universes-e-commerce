/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/javascript.js to edit this template
 */


//alert("Hi Cart");
/**
 * Date: 2026-04-20
 * Debug marker:
 * If you open browser DevTools Console, you should see this line on the Cart page.
 * This confirms the correct `/js/cart.js` file is loading.
 */
console.log("[cart] cart.js loaded (2026-04-20)");
const debounce = (fn, delay = 100) => {
    let t;
    return (...args) => {
        clearTimeout(t);
        t = setTimeout(() => fn(...args), delay);
    };
};
const formatCurrency = num => `৳ ${parseFloat(num || 0).toFixed(2)}`;

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
 *   - packagingRateId (PackagingRate ID) for packaging
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
                <div class="empty-cart text-center p-4">
                    <p>Your cart is empty.</p>
                    <a href="/public/product" class="btn btn-success btn-continue">
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
        for (const [vendorId, items] of Object.entries(data.groupedCart)) {

            if (!items || items.length === 0)
                continue;
            const vendorDiv = document.createElement('div');
            vendorDiv.classList.add('vendor');
            vendorDiv.dataset.vendorId = vendorId;
            const vendorName =
                    items[0]?.product?.vendorprofile?.companyName || 'Vendor';
            const shippingOptions = data.vendorShippingOptions?.[vendorId] || [];
            const packagingOptions = data.vendorPackagingOptions?.[vendorId] || [];
            const shippingCost = data.vendorShippingCost?.[vendorId] || 0;
            const packagingCost = data.vendorPackagingCost?.[vendorId] || 0;
            const subtotal = data.vendorSubtotals?.[vendorId] || 0;
            const vendorRequiresShipping = data.vendorRequiresShipping?.[vendorId] !== false;
            // Persist selections by ID (so selection stays correct after re-render).
            // Date: 2026-04-20
            const selectedShippingOptionCode = data.selectedShippingOption?.[vendorId] || '';
            const selectedPackagingRateId = data.selectedPackagingRate?.[vendorId] || '';
            vendorDiv.innerHTML = `
                <h4 class="mt-4 text-primary">${vendorName}</h4>

                <div class="table-responsive">
                    <table class="table table-bordered table-striped align-middle">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Product</th>

                                <th>Photo</th>
            <th>Description</th>
                                <th>Price</th>
                                <th>Quantity</th>
                                <th>Unit</th>
                                <th>Weight</th>
                                <th>Discount %</th>
                                <th>VAT %</th>
                                <th>Item Total</th>
                                <th>Remove</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${items.map((item, idx) => `
                                <tr>
                                    <td>${idx + 1}</td>
                                    <td>${item.product?.title || ''}</td>

                                    <td>
                                        <img
                                            src="${item.product?.imageUrl || '/images/no-image.png'}"
                                            style="width:50px;height:auto"
                                        />
                                    </td>
             <td>${item.variantSummary || '-'}</td>
                                    <td>${formatCurrency(item.salesPrice || 0)}</td>
                                    <td>
                                        <input type="number"
                                               class="qty-input form-control form-control-sm"
                                               data-product-id="${item.productId}"
                                               data-catalog-variant-uuid="${item.catalogVariantUuid || ''}"
                                               value="${item.quantity || 1}"
                                               min="1"/>
                                    </td>
                                    <td>${item.uom?.name || '-'}</td>
                                    <td>${Number(item.weight || 0).toFixed(2)}</td>
                                    <td>${item.discountRate || 0}</td>
                                    <td>${item.vatRate || 0}</td>
                                    <td class="item-total">
                                        ${formatCurrency(item.itemTotal || 0)}
                                    </td>
                                    <td>
                                        <button type="button" class="remove-btn btn btn-sm btn-danger"
                                                data-product-id="${item.productId}"
                                                data-catalog-variant-uuid="${item.catalogVariantUuid || ''}">
                                            Delete
                                        </button>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>

                <div class="d-flex gap-4 mt-3 justify-content-between align-items-start">
                    <div class="d-flex flex-column gap-2">
                        <label>Shipping:</label>
                        <select class="shipping-select form-select form-select-sm"
                                data-vendor-id="${vendorId}"
                                ${vendorRequiresShipping ? '' : 'disabled'}
                                onchange="window.cartOnShippingChange && window.cartOnShippingChange(this)">
                            <option value="0" ${!selectedShippingOptionCode ? 'selected' : ''}>
                                ${vendorRequiresShipping ? 'Select shipping company' : 'No shipping required'}
                            </option>
                            ${shippingOptions.length
                    ? shippingOptions.map(opt => `
                                    <option value="${opt.code}"
                                        ${(opt.code && opt.code === selectedShippingOptionCode) ? 'selected' : ''}>
                                        ${opt.title} - ${formatCurrency(opt.price)} (${opt.estimatedDelivery || ''})
                                    </option>
                                `).join('')
                    : `<option value="0">No shipping</option>`
                    }
                        </select>

                        <label>Packaging:</label>
                        <select class="packaging-select form-select form-select-sm"
                                data-vendor-id="${vendorId}"
                                ${vendorRequiresShipping ? '' : 'disabled'}
                                onchange="window.cartOnPackagingChange && window.cartOnPackagingChange(this)">
                            <option value="0" ${!selectedPackagingRateId ? 'selected' : ''}>
                                ${vendorRequiresShipping ? 'Select packaging type' : 'No packaging required'}
                            </option>
                            ${packagingOptions.length
                    ? packagingOptions.map(pack => {
                        const price = (pack.basePrice || 0) + (pack.additionalPrice || 0);
                        return `
                                        <option value="${pack.id}"
                                            ${(pack.id != null && String(pack.id) === String(selectedPackagingRateId)) ? 'selected' : ''}>
                                            ${pack.packagingType} - ${formatCurrency(price)}
                                        </option>`;
                    }).join('')
                    : `<option value="0">No packaging</option>`
                    }
                        </select>
                        ${vendorRequiresShipping ? '' : '<div class="small text-muted">Virtual products do not require shipping or packaging.</div>'}
                    </div>

                    <div class="text-end vendor-summary">
                        <div>Subtotal: ${formatCurrency(subtotal)}</div>
                        <div>Shipping: ${formatCurrency(shippingCost)}</div>
                        <div>Packaging: ${formatCurrency(packagingCost)}</div>
                        <div class="fw-bold border-top mt-1 pt-1">
                            Total: ${formatCurrency(subtotal + shippingCost + packagingCost)}
                        </div>
                    </div>
                </div>
            `;
            container.appendChild(vendorDiv);
        }

        const grandTotalEl = document.getElementById('grandTotal');
        if (grandTotalEl) {
            grandTotalEl.textContent =
                    `Grand Total: ${formatCurrency(data.grandTotal || 0)}`;
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
        const vendorId = selectEl?.dataset?.vendorId;
        const shippingOptionCode = selectEl?.value || "";
        // console.log("[cart] shipping onchange", {vendorId, shippingOptionCode});
        if (!vendorId)
            return;
        await postForm("/carts/updateShippingOption", {vendorId, shippingOptionCode});
        await loadCart();
    } catch (err) {
        /// console.error(err);
        alert(err?.message || "Shipping update failed");
    }
};

window.cartOnPackagingChange = async function (selectEl) {
    try {
        const vendorId = selectEl?.dataset?.vendorId;
        const packagingRateId = selectEl?.value || "";
        // console.log("[cart] packaging onchange", {vendorId, packagingRateId});
        if (!vendorId)
            return;
        await postForm("/carts/updatePackagingRate", {vendorId, packagingRateId});
        await loadCart();
    } catch (err) {
        // console.error(err);
        alert(err?.message || "Packaging update failed");
    }
};

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

    const vendorId = e.target.dataset.vendorId;
    const vendorDiv = e.target.closest(".vendor");
    vendorDiv?.classList.add("vendor-loading");

    try {
        if (qtyInput) {
            const productId = qtyInput.dataset.productId;
            const catalogVariantUuid = qtyInput.dataset.catalogVariantUuid || "";
            const quantity = qtyInput.value || "1";
            await postForm("/carts/updateQuantity", {productId, catalogVariantUuid, quantity});
        } else if (!vendorId) {
            return;
        } else if (shipSel) {
            const shippingOptionCode = shipSel.value || "";
            // Date: 2026-04-20
            // Debug message to confirm the change handler is firing.
            // alert("hello");
            // console.log("[cart] shipping change", {vendorId, shippingOptionCode});
            await postForm("/carts/updateShippingOption", {vendorId, shippingOptionCode});
        }
        if (packSel) {
            const packagingRateId = packSel.value || "";
            // console.log("[cart] packaging change", {vendorId, packagingRateId});
            await postForm("/carts/updatePackagingRate", {vendorId, packagingRateId});
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
    const productId = btn.dataset.productId;
    const catalogVariantUuid = btn.dataset.catalogVariantUuid || '';
    if (!productId)
        return;
    if (!productId || productId === "undefined") {
        alert("Product ID missing. Cannot remove item.");
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
            body: `productId=${encodeURIComponent(productId)}&catalogVariantUuid=${encodeURIComponent(catalogVariantUuid)}`
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

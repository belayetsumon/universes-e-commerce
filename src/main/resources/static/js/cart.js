/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/javascript.js to edit this template
 */


//alert("Hi Cart");
const debounce = (fn, delay = 100) => {
    let t;
    return (...args) => {
        clearTimeout(t);
        t = setTimeout(() => fn(...args), delay);
    };
};
const formatCurrency = num => `৳ ${parseFloat(num || 0).toFixed(2)}`;
// ---------- Load Cart ----------
async function loadCart() {
    try {
        const res = await fetch('/carts/api');
        if (!res.ok)
            throw new Error('Failed to load cart');
        const data = await res.json();
        //console.log(data);

        const container = document.getElementById('cartContainer');
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
            document.getElementById('grandTotal').textContent = '';
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
             <td>
Color:</br>
Size:
</td>
                                    <td>${formatCurrency(item.salesPrice || 0)}</td>
                                    <td>
                                        <input type="number"
                                               class="qty-input form-control form-control-sm"
                                               data-product-id="${item.productId}"
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
                                                data-product-id="${item.productId}">
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
                                data-vendor-id="${vendorId}">
                            ${shippingOptions.length
                    ? shippingOptions.map(opt => `
                                    <option value="${opt.price}"
                                        ${opt.price === shippingCost ? 'selected' : ''}>
                                        ${opt.title} - ${formatCurrency(opt.price)}
                                    </option>
                                `).join('')
                    : `<option value="0">No shipping</option>`
                    }
                        </select>

                        <label>Packaging:</label>
                        <select class="packaging-select form-select form-select-sm"
                                data-vendor-id="${vendorId}">
                            ${packagingOptions.length
                    ? packagingOptions.map(pack => {
                        const price = (pack.basePrice || 0) + (pack.additionalPrice || 0);
                        return `
                                        <option value="${price}"
                                            ${price === packagingCost ? 'selected' : ''}>
                                            ${pack.packagingType} - ${formatCurrency(price)}
                                        </option>`;
                    }).join('')
                    : `<option value="0">No packaging</option>`
                    }
                        </select>
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

        document.getElementById('grandTotal').textContent =
                `Grand Total: ${formatCurrency(data.grandTotal || 0)}`;
        //  attachCartEvents();

    } catch (err) {
        console.error('Cart load failed', err);
    }
}

document.addEventListener('click', async function (e) {

    const btn = e.target.closest('.remove-btn');
    if (!btn)
        return; // Not a delete button
    e.preventDefault(); // stop form submit
    e.stopPropagation(); // stop bubbling
    const productId = btn.dataset.productId;
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
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: `productId=${productId}`
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
//// ---------- Attach Events ----------
//function attachCartEvents() {
//    // Quantity change (debounced)
//    document.querySelectorAll('.qty-input').forEach(input => {
//        const debounced = debounce(async () => {
//            const productId = input.dataset.productId;
//            const qty = input.value;
//            const vendorDiv = input.closest('.vendor');
//            vendorDiv.classList.add('vendor-loading');
//            vendorDiv.querySelector('.vendor-error').textContent = '';
//            try {
//                const res = await fetch('/carts/updateQuantity', {
//                    method: 'POST',
//                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
//                    body: `productId=${productId}&quantity=${qty}`
//                });
//                if (!res.ok)
//                    throw new Error('Update failed');
//                await loadCart();
//            } catch (err) {
//                vendorDiv.querySelector('.vendor-error').textContent = err.message;
//            } finally {
//                vendorDiv.classList.remove('vendor-loading');
//            }
//        }, 400);
//        input.addEventListener('input', debounced);
//
//        // ✅ Show alert when user leaves the field
//    });
//
//    // Remove item
////    document.querySelectorAll('.remove-btn').forEach(btn => {
////        btn.addEventListener('click', async () => {
////            const productId = btn.dataset.productId;
////
////            // ✅ Confirm before deleting
////            if (!confirm('Are you sure you want to remove this item from cart?')) {
////                return;
////            }
////            const vendorDiv = btn.closest('.vendor');
////            vendorDiv.classList.add('vendor-loading');
////            vendorDiv.querySelector('.vendor-error').textContent = '';
////            try {
////                const res = await fetch('/carts/removeItem', {
////                    method: 'POST',
////                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
////                    body: `productId=${productId}`
////                });
////                if (!res.ok)
////                    throw new Error('Remove failed');
////                await loadCart();
////            } catch (err) {
////                vendorDiv.querySelector('.vendor-error').textContent = err.message;
////            } finally {
////                vendorDiv.classList.remove('vendor-loading');
////            }
////        });
////    });
//
//
//
//
//
//
//
//
//    // Shipping change
//    document.querySelectorAll('.shipping-select').forEach(sel => {
//        sel.addEventListener('change', async () => {
//            const vendorId = sel.dataset.vendorId;
//            const cost = sel.value;
//            const vendorDiv = sel.closest('.vendor');
//            vendorDiv.classList.add('vendor-loading');
//            vendorDiv.querySelector('.vendor-error').textContent = '';
//            try {
//                const res = await fetch('/carts/updateShipping', {
//                    method: 'POST',
//                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
//                    body: `vendorId=${vendorId}&shippingCost=${cost}`
//                });
//                if (!res.ok)
//                    throw new Error('Shipping update failed');
//                await loadCart();
//            } catch (err) {
//                vendorDiv.querySelector('.vendor-error').textContent = err.message;
//            } finally {
//                vendorDiv.classList.remove('vendor-loading');
//            }
//        });
//    });
//
//    // Packaging change
//    document.querySelectorAll('.packaging-select').forEach(sel => {
//        sel.addEventListener('change', async () => {
//            const vendorId = sel.dataset.vendorId;
//            const cost = sel.value;
//            const vendorDiv = sel.closest('.vendor');
//            vendorDiv.classList.add('vendor-loading');
//            vendorDiv.querySelector('.vendor-error').textContent = '';
//            try {
//                const res = await fetch('/carts/updatePackaging', {
//                    method: 'POST',
//                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
//                    body: `vendorId=${vendorId}&packagingCost=${cost}`
//                });
//                if (!res.ok)
//                    throw new Error('Packaging update failed');
//                await loadCart();
//            } catch (err) {
//                vendorDiv.querySelector('.vendor-error').textContent = err.message;
//            } finally {
//                vendorDiv.classList.remove('vendor-loading');
//            }
//        });
//    });
//}

document.addEventListener('DOMContentLoaded', loadCart);

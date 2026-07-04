/*
 * Section: Customer panel
 * Used by: templates/front-layout-inner-customar-page.html
 * Purpose: customer dashboard tracking, shared storefront helpers, and Bootstrap tab persistence
 */
function initFacebookPixel() {
    if (window.fbq) {
        return;
    }

    !function (f, b, e, v, n, t, s) {
        if (f.fbq) {
            return;
        }
        n = f.fbq = function () {
            n.callMethod ? n.callMethod.apply(n, arguments) : n.queue.push(arguments);
        };
        if (!f._fbq) {
            f._fbq = n;
        }
        n.push = n;
        n.loaded = true;
        n.version = '2.0';
        n.queue = [];
        t = b.createElement(e);
        t.async = true;
        t.src = v;
        s = b.getElementsByTagName(e)[0];
        s.parentNode.insertBefore(t, s);
    }(window, document, 'script', 'https://connect.facebook.net/en_US/fbevents.js');

    fbq('init', '1365852474985195');
    fbq('track', 'PageView');
}

document.addEventListener('DOMContentLoaded', initFacebookPixel);

// Date: 2026-05-26
// Keep customer sidebar submenus collapsed by default and reopen the
// relevant section for the current page so referral-reward pages behave
// like an accordion instead of a permanently expanded nested list.
document.addEventListener('DOMContentLoaded', function () {
    var normalizePath = function (path) {
        if (!path) {
            return '/';
        }

        var trimmedPath = path.replace(/\/+$/, '');
        return trimmedPath || '/';
    };

    var matchesCurrentPath = function (rawMatchValue, currentPath) {
        if (!rawMatchValue) {
            return false;
        }

        return rawMatchValue.split(',').some(function (matchValue) {
            var matchPath = normalizePath(matchValue.trim());
            return currentPath === matchPath || currentPath.indexOf(matchPath + '/') === 0;
        });
    };

    var currentPath = normalizePath(window.location.pathname);

    document.querySelectorAll('[data-path-match]').forEach(function (link) {
        if (!matchesCurrentPath(link.getAttribute('data-path-match'), currentPath)) {
            return;
        }

        link.classList.add('active');
    });

    var sidebar = document.querySelector('.customer-sidebar-menu');

    if (!sidebar || typeof bootstrap === 'undefined' || !bootstrap.Collapse) {
        return;
    }

    sidebar.querySelectorAll('.collapse .nav-link.active').forEach(function (link) {
        var submenu = link.closest('.collapse');
        if (submenu) {
            bootstrap.Collapse.getOrCreateInstance(submenu, {toggle: false}).show();
        }
    });

    sidebar.querySelectorAll('.customer-submenu-toggle').forEach(function (toggle) {
        var targetSelector = toggle.getAttribute('data-bs-target');
        if (!targetSelector) {
            return;
        }

        var submenu = sidebar.querySelector(targetSelector);
        if (!submenu) {
            return;
        }

        var syncExpandedState = function () {
            var isOpen = submenu.classList.contains('show');
            var hasActiveChild = !!submenu.querySelector('.nav-link.active');

            toggle.classList.toggle('is-active', isOpen || hasActiveChild);
            toggle.classList.toggle('collapsed', !isOpen);
            toggle.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
        };

        submenu.addEventListener('shown.bs.collapse', syncExpandedState);
        submenu.addEventListener('hidden.bs.collapse', syncExpandedState);
        syncExpandedState();
    });
});

// District and thana location picker
document.addEventListener('DOMContentLoaded', function () {
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


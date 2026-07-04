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



//// district and thana location popup  start //////////////////////////////
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

        var districtModal = bootstrap.Modal.getOrCreateInstance(modalEl);
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
            districtModal.show();
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

            var location = thana || district;
            fetch('/district/save-district', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'location=' + encodeURIComponent(location)
            })
                    .then(function (res) {
                        if (!res.ok) {
                            throw new Error('Network response was not OK');
                        }
                        return res.text();
                    })
                    .then(function (data) {
                        if (data === 'success') {
                            districtModal.hide();
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

    document.querySelectorAll('.location-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var location = btn.dataset.location;

            if (!location) {
                alert('Invalid location!');
                return;
            }

            fetch('/district/save-district', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'location=' + encodeURIComponent(location)
            })
                    .then(res => {
                        if (!res.ok)
                            throw new Error('Network response was not OK');
                        return res.text();
                    })
                    .then(data => {
                        if (data === 'success') {
                            console.log('Location saved:', location);
                            window.location.reload(); // refresh page after saving
                        } else {
                            alert('Failed to save location');
                        }
                    })
                    .catch(err => console.error('Error saving location:', err));
        });
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

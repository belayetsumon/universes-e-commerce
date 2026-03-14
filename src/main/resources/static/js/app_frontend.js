$(document).ready(function () {

//alert("Hi");



//    $('.slider-for').slick({
//        slidesToShow: 1,
//        slidesToScroll: 1,
//        arrows: false,
//        fade: true,
//        asNavFor: '.slider-nav'
//    });

    $('.slider-nav').slick({
        slidesToShow: 4,
        slidesToScroll: 1,
        arrows: true,
        dots: false,
        centerMode: false,
        focusOnSelect: true
    });
//    $('.slider-nav').slick({
//        slidesToShow: 4,
//        slidesToScroll: 1,
//        asNavFor: '.slider-for',
//        dots: false,
//        centerMode: true,
//        focusOnSelect: true
//    });


    // Click to replace main image
    $('.slider-nav').on('click', 'img', function () {
        const newSrc = $(this).data('img');
        $('#mainImage').attr('src', newSrc);
    });
});



//// cart district popup  start //////////////////////////////
document.addEventListener('DOMContentLoaded', function () {
    var modalEl = document.getElementById('districtModal');

    if (!modalEl)
        return; // Safety check
    var districtModal = new bootstrap.Modal(modalEl);

    // Auto-show modal if data-show attribute is true
    if (modalEl.dataset.show && modalEl.dataset.show.toLowerCase() === 'true') {
        districtModal.show();
    }

    // Save selected district
    var saveBtn = document.getElementById('saveDistrict');
    if (saveBtn) {
        saveBtn.addEventListener('click', function () {
            var districtSelect = document.getElementById('districtSelect');
            if (!districtSelect)
                return;

            var district = districtSelect.value;
            if (!district) {
                alert('Please select a district!');
                return;
            }

            fetch('/district/save-district', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'districtName=' + encodeURIComponent(district)
            })
                    .then(res => {
                        if (!res.ok)
                            throw new Error('Network response was not OK');
                        return res.text();
                    })
                    .then(data => {
                        if (data === 'success') {
                            districtModal.hide(); // Close modal
                        } else {
                            alert('Invalid district selection');
                            console.log('Invalid district selection');
                        }
                    })
                    .catch(err => console.error('Fetch error:', err));
        });
    }

    // Refresh page after modal closes
    modalEl.addEventListener('hidden.bs.modal', function () {
        window.location.reload();
    });
});

//// header district select 


document.addEventListener('DOMContentLoaded', function () {
    // Get all district buttons
    var districtButtons = document.querySelectorAll('.district-btn');

    districtButtons.forEach(function (btn) {
        btn.addEventListener('click', function () {
            var district = btn.dataset.district;

            if (!district) {
                alert('Invalid district!');
                return;
            }

            fetch('/district/save-district', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: 'districtName=' + encodeURIComponent(district)
            })
                    .then(res => {
                        if (!res.ok)
                            throw new Error('Network response was not OK');
                        return res.text();
                    })
                    .then(data => {
                        if (data === 'success') {
                            console.log('District saved:', district);
                            window.location.reload(); // refresh page after saving
                        } else {
                            alert('Failed to save district');
                        }
                    })
                    .catch(err => console.error('Error saving district:', err));
        });
    });
});

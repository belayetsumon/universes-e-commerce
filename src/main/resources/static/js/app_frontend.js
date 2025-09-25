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


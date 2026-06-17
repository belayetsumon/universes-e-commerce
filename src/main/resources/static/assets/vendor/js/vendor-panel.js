/*
 * Section: Vendor panel
 * Used by: templates/front-layout-inner-vendor-page.html
 * Purpose: vendor rich text editor setup, discount date controls, and shared datatable behavior
 */

//tiny mce start
tinymce.init({
    selector: 'textarea.rich-text', // Apply only to textareas with class "rich-text"
    plugins: 'lists link image table ',
    toolbar: 'undo redo | bold italic underline | bullist numlist ',
    menubar: true,
    branding: false,
    height: 300
});

//tiny mce end


/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/javascript.js to edit this template
 */


$(document).ready(function () {
//    alert("hi");
    // Initialize the datepicker
    $('#productDiscountStart').datepicker({
        format: 'dd-mm-yyyy', // Bootstrap Datepicker format
        autoclose: true, // Close the datepicker once a date is selected
        todayHighlight: true, // Highlight today's date
    });
    
    
    
    
    
     $('#productDiscountEnd').datepicker({
        format: 'dd-mm-yyyy', // Bootstrap Datepicker format
        autoclose: true, // Close the datepicker once a date is selected
        todayHighlight: true, // Highlight today's date
    });
    
    
    $('#categoryDiscountStartDate').datepicker({
        format: 'dd-mm-yyyy', // Bootstrap Datepicker format
        autoclose: true, // Close the datepicker once a date is selected
        todayHighlight: true, // Highlight today's date
    });
    
    
    
    
    
     $('#categoryDiscountEndDate').datepicker({
        format: 'dd-mm-yyyy', // Bootstrap Datepicker format
        autoclose: true, // Close the datepicker once a date is selected
        todayHighlight: true, // Highlight today's date
    });
    
    
    
    
    
    
});
$(document).ready(function () {

    const table = $("#tablesdata").DataTable({
        "ordering": true, // Enable column ordering
//        "responsive": true, // Make table responsive
        "lengthChange": true, // Allow changing the number of records per page
        "autoWidth": false, // Disable auto width (use fixed width for the columns)
        "scrollX": true, // Enable horizontal scrolling
        "buttons": ["copy", "csv", "excel", "pdf", "print"], // Enable export buttons
        "lengthMenu": [
            [10, 25, 50, -1], // Options for records per page
            [10, 25, 50, 'All']           // Displayed options
        ],
        "columnDefs": [
            {targets: '_all', visible: true} // Ensure all columns are visible
        ]
    });

    // 2026-04-22: Guard against missing DataTables Buttons plugin so unrelated JS errors do not obscure form issues.
    if (table && typeof table.buttons === "function") {
        table.buttons().container().appendTo('#tablesdata_wrapper  .col-md-6:eq(1)');
    }

});


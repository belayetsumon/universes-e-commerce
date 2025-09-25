$(document).ready(function () {

    $("#tablesdata").DataTable({
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
    }).buttons().container().appendTo('#tablesdata_wrapper  .col-md-6:eq(1)');

});
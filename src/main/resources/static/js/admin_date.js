/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/javascript.js to edit this template
 */


$(document).ready(function () {
//    alert("hi");
    // Initialize the datepicker
    $('#productDiscountStart').datepicker({
        dateFormat: 'dd-mm-yy', // Specify the format you need
        autoclose: true, // Close the datepicker once a date is selected
        todayHighlight: true, // Highlight today's date
    });
    
    
    
    
    
     $('#productDiscountEnd').datepicker({
        dateFormat: 'dd-mm-yy', // Specify the format you need
        autoclose: true, // Close the datepicker once a date is selected
        todayHighlight: true, // Highlight today's date
    });
    
    
    $('#categoryDiscountStartDate').datepicker({
        dateFormat: 'dd-mm-yy', // Specify the format you need
        autoclose: true, // Close the datepicker once a date is selected
        todayHighlight: true, // Highlight today's date
    });
    
    
    
    
    
     $('#categoryDiscountEndDate').datepicker({
        dateFormat: 'dd-mm-yy', // Specify the format you need
        autoclose: true, // Close the datepicker once a date is selected
        todayHighlight: true, // Highlight today's date
    });
    
    
    
    
    
    
});
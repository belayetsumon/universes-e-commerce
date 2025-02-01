/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/// sidebar menu 
$(document).ready(function () {
    $('#sidebarCollapse').on('click', function () {
        $('#sidebar').toggleClass('active');
    });
});

// product image size limit and image formate 


$(document).ready(function () {
    // Reusable function to handle image file validation with dynamic form IDs
    function validateImageUpload(inputId, errorMessageId, previewId, submitButtonId, deleteButtonId, infoId, formId, minSizeInKB = 30, maxSizeInMB = 1) {
        // Allowed file formats
        const allowedFormats = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];

        // Convert size limits from KB and MB to Bytes
        const minSizeInBytes = minSizeInKB * 1024; // Convert KB to Bytes
        const maxSizeInBytes = maxSizeInMB * 1024 * 1024; // Convert MB to Bytes

        let isValidFile = false; // Track if the selected file is valid

        // Function to validate file size (min and max as parameters)
        function validateFileSize(file) {
            // Check if file size is below minimum size
            if (file.size < minSizeInBytes) {
                return `File size is too small. Minimum size is ${minSizeInKB} KB.`;
            }

            // Check if file size exceeds maximum size
            if (file.size > maxSizeInBytes) {
                return `File size exceeds ${maxSizeInMB} MB.`;
            }

            return null; // Return null if no error
        }

        // Function to validate file format
        function validateFileFormat(file) {
            // Check the file format
            if (!allowedFormats.includes(file.type)) {
                return `Invalid file format. Allowed formats are: ${allowedFormats.join(', ')}`;
            }

            return null; // Return null if no error
        }

        // File input change handler
        $(inputId).on('change', function (event) {
            const file = event.target.files[0]; // Get the selected file
            const errorMessage = $(errorMessageId);
            const submitButton = $(submitButtonId);
            const imagePreview = $(previewId);
            const deleteButton = $(deleteButtonId);
            const infoBox = $(infoId); // Info display element
            errorMessage.text(''); // Clear previous error message
            isValidFile = false; // Reset validation status on each change
            submitButton.prop('disabled', true); // Disable the submit button by default

            // Reset image preview when no file is selected or an invalid file is selected
            imagePreview.attr('src', ''); // Reset preview image
            imagePreview.hide(); // Hide preview image
            deleteButton.hide(); // Hide delete button initially
            infoBox.text(''); // Clear image info

            // Check if a file is selected
            if (file) {
                console.log("File selected: ", file.name);

                // Validate file size
                const sizeError = validateFileSize(file);
                if (sizeError) {
                    errorMessage.text(sizeError);
                    console.log("Error: " + sizeError);
                    return; // Exit the function if there's an error
                }

                // Validate file format
                const formatError = validateFileFormat(file);
                if (formatError) {
                    errorMessage.text(formatError);
                    console.log("Error: " + formatError);
                    return; // Exit the function if there's an error
                }

                // If the file is valid
                errorMessage.text(''); // Clear any error messages
                isValidFile = true; // Mark the file as valid
                submitButton.prop('disabled', false); // Enable the submit button
                console.log("File is valid");

                // Show the image preview (if the file is an image)
                if (file && file.type.startsWith('image/')) {
                    const reader = new FileReader();
                    reader.onload = function (e) {
                        console.log("Image loaded successfully");
                        imagePreview.attr('src', e.target.result); // Set preview image src
                        imagePreview.show(); // Show the preview image
                        deleteButton.show(); // Show the delete button

                        // Create an Image object to get dimensions
                        const img = new Image();
                        img.onload = function () {
                            console.log("Image dimensions loaded successfully");
                            const width = img.width;
                            const height = img.height;
                            const sizeInKB = (file.size / 1024).toFixed(2); // Convert to KB
                            const sizeInMB = (file.size / (1024 * 1024)).toFixed(2); // Convert to MB

                            // Display file info
                            infoBox.html(`
            <strong>File Name:</strong> ${file.name}<br>
            <strong>File Size:</strong> ${sizeInKB} KB (${sizeInMB} MB)<br>
            <strong>Dimensions:</strong> ${width}x${height} px
        `);
                        };
                        img.src = e.target.result; // Load the image to get its dimensions
                    };
                    reader.readAsDataURL(file);
                }
            } else {
                errorMessage.text('Please select a file.');
                console.log("Error: No file selected");
            }
        });

        // Event handler for delete button
        $(deleteButtonId).on('click', function () {
            $(inputId).val(''); // Reset the file input
            $(errorMessageId).text(''); // Clear error message
            $(previewId).attr('src', '').hide(); // Reset preview image
            $(deleteButtonId).hide(); // Hide delete button
            $(infoId).text(''); // Clear image info
            $(submitButtonId).prop('disabled', true); // Disable submit button
            console.log("Preview deleted and fields reset");
        });

        // Prevent form submission if the file is invalid
        $(formId).on('submit', function (event) {
            if (!isValidFile) {
                event.preventDefault(); // Prevent form submission
                $(errorMessageId).text('Please upload a valid file before submitting.');
                console.log("Form submission prevented due to invalid file.");
            }
        });
    }

    // Call the function on page load for product image upload
<<<<<<< HEAD
   /* validateImageUpload(
=======
    validateImageUpload(
>>>>>>> 8be69ac5b0b4aff187039abad5bb6d2f07da813f
            '#imagefile', // Input file selector
            '#image_error_message', // Error message selector
            '#imagePreview', // Image preview selector
            '#submitBtn', // Submit button selector
            '#deleteImageBtn', // Delete button selector
            '#imageInfo', // Info display element
            '#uploadForm', // Form ID
            30, // Minimum file size (KB)
            1                          // Maximum file size (MB)
<<<<<<< HEAD
            );*/

    // Call the function on page load for category image upload
 /*   validateImageUpload(
=======
            );

    // Call the function on page load for category image upload
    validateImageUpload(
>>>>>>> 8be69ac5b0b4aff187039abad5bb6d2f07da813f
            '#catimagefile', // Input file selector
            '#catimage_error_message', // Error message selector
            '#catimagePreview', // Image preview selector
            '#catSubmitBtn', // Submit button selector
            '#catDeleteImageBtn', // Delete button selector
            '#catimageInfo', // Info display element
            '#catUploadForm', // Form ID
            30, // Minimum file size (KB)
            1                          // Maximum file size (MB)
<<<<<<< HEAD
            ); */
=======
            );
>>>>>>> 8be69ac5b0b4aff187039abad5bb6d2f07da813f
});





/// end image size and formate


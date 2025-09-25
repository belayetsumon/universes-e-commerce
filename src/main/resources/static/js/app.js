// product details active tab
$(document).ready(function () {
        // Function to reset and remove active state from all tabs
        function resetTabs() {
            $('#myTab button').removeClass('active');  // Remove active class from all tabs
            $('.tab-content .tab-pane').removeClass('show active');  // Remove active content
        }

        // Retrieve the last active tab from sessionStorage
        var activeTab = sessionStorage.getItem("activeTab");

        // If an active tab exists, show it
        if (activeTab) {
            // Reset previous active tabs
            resetTabs();
            // Activate the last active tab and show the corresponding content
            $('#myTab button[data-bs-target="' + activeTab + '"]').addClass('active');
            $(activeTab).addClass('show active');
        } else {
            // Default to the first tab if no active tab is stored
            $('#myTab button:first').addClass('active');
            $('.tab-content .tab-pane:first').addClass('show active');
        }

        // Store the active tab in sessionStorage when tab changes
        $('#myTab button').on('shown.bs.tab', function (e) {
            var tabId = $(e.target).attr('data-bs-target');
            sessionStorage.setItem('activeTab', tabId);

            // Reset the tabs before adding the active class to the new tab
            resetTabs();
            // Add active class to the selected tab and content
            $(e.target).addClass('active');
            $(tabId).addClass('show active');
        });
    });
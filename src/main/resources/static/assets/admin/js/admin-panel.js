/*
 * Section: Admin panel
 * Used by: templates/admin-layout.html
 * Purpose: admin sidebar behavior, submenu auto-scroll, navbar dropdowns, date controls, and datatables
 */
document.addEventListener('DOMContentLoaded', () => {
    const toggleBtn = document.getElementById('sidebar-toggle-btn');
    const body = document.body;
    const sidebarNav = document.querySelector('.sidebar-nav');
    const sidebarContent = document.querySelector('.sidebar-content');

    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            body.classList.toggle('sidebar-collapsed');
        });
    }

    if (sidebarNav) {
        const scrollSidebarTo = (top) => {
            if (!sidebarContent) {
                return;
            }

            const maxScrollTop = Math.max(0, sidebarContent.scrollHeight - sidebarContent.clientHeight);
            const nextTop = Math.max(0, Math.min(top, maxScrollTop));
            sidebarContent.scrollTo({
                top: nextTop,
                behavior: 'smooth'
            });
        };

        const keepSidebarRegionVisible = (startElement, endElement = startElement) => {
            if (!sidebarContent || !startElement) {
                return;
            }

            const sidebarRect = sidebarContent.getBoundingClientRect();
            const startRect = startElement.getBoundingClientRect();
            const endRect = (endElement || startElement).getBoundingClientRect();
            const padding = 20;
            const viewportTop = sidebarRect.top + padding;
            const viewportBottom = sidebarRect.bottom - padding;
            const regionTop = Math.min(startRect.top, endRect.top);
            const regionBottom = Math.max(startRect.bottom, endRect.bottom);
            const regionHeight = regionBottom - regionTop;
            const viewportHeight = Math.max(0, viewportBottom - viewportTop);

            let nextTop = sidebarContent.scrollTop;

            if (regionHeight > viewportHeight) {
                nextTop += startRect.top - viewportTop;
            } else if (regionBottom > viewportBottom) {
                nextTop += regionBottom - viewportBottom;
            } else if (regionTop < viewportTop) {
                nextTop += regionTop - viewportTop;
            }

            scrollSidebarTo(nextTop);
        };

        const normalizePath = (value) => {
            if (!value) {
                return '/';
            }

            let path = value;
            try {
                path = new URL(value, window.location.origin).pathname;
            } catch (error) {
                path = value;
            }

            path = path.replace(/\/+$/, '');
            return path === '' ? '/' : path.toLowerCase();
        };

        const currentPath = normalizePath(window.location.pathname);
        const sidebarLinks = Array.from(
                sidebarNav.querySelectorAll('.nav-link:not([data-bs-toggle="collapse"])')
                ).filter(link => {
            const href = link.getAttribute('href');
            return href && href !== '#';
        });

        const clearSidebarState = () => {
            sidebarNav.querySelectorAll('.nav-link.active').forEach(link => {
                link.classList.remove('active');
                link.removeAttribute('aria-current');
            });
        };

        const getMatchScore = (linkPath) => {
            if (!linkPath || linkPath === '#') {
                return -1;
            }

            if (linkPath === currentPath) {
                return 10000 + linkPath.length;
            }

            const listOrIndexBase = linkPath.replace(/\/(index|list)$/i, '');
            if (listOrIndexBase !== linkPath && currentPath === listOrIndexBase) {
                return 9000 + listOrIndexBase.length;
            }

            if (listOrIndexBase
                    && listOrIndexBase !== '/'
                    && listOrIndexBase !== '/admin'
                    && currentPath.startsWith(`${listOrIndexBase}/`)) {
                return 5000 + listOrIndexBase.length;
            }

            if (linkPath !== '/' && currentPath.startsWith(`${linkPath}/`)) {
                return 4000 + linkPath.length;
            }

            return -1;
        };

        clearSidebarState();

        let activeLink = null;
        let bestScore = -1;

        sidebarLinks.forEach(link => {
            const linkPath = normalizePath(link.getAttribute('href') || link.href);
            const score = getMatchScore(linkPath);
            if (score > bestScore) {
                bestScore = score;
                activeLink = link;
            }
        });

        if (activeLink) {
            activeLink.classList.add('active');
            activeLink.setAttribute('aria-current', 'page');

            let activeRegionStart = activeLink;
            const parentCollapse = activeLink.closest('.collapse');
            if (parentCollapse) {
                parentCollapse.classList.add('show');

                const parentToggle = sidebarNav.querySelector(
                        `[data-bs-toggle="collapse"][href="#${parentCollapse.id}"]`
                        );

                if (parentToggle) {
                    parentToggle.classList.add('active');
                    parentToggle.setAttribute('aria-expanded', 'true');
                    activeRegionStart = parentToggle;
                }
            }

            keepSidebarRegionVisible(activeRegionStart, activeLink);
        }

        const collapseTriggers = sidebarNav.querySelectorAll('[data-bs-toggle="collapse"]');
        collapseTriggers.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();

                const targetId = e.currentTarget.getAttribute('href');
                if (!targetId || !targetId.startsWith('#')) {
                    return;
                }

                keepSidebarRegionVisible(e.currentTarget);

                sidebarNav.querySelectorAll('.collapse.show').forEach(openCollapse => {
                    if (`#${openCollapse.id}` !== targetId) {
                        bootstrap.Collapse.getOrCreateInstance(openCollapse, {toggle: false}).hide();
                    }
                });
            });
        });

        sidebarNav.querySelectorAll('.collapse').forEach(collapseEl => {
            const relatedToggle = sidebarNav.querySelector(
                    `[data-bs-toggle="collapse"][href="#${collapseEl.id}"]`
                    );

            collapseEl.addEventListener('show.bs.collapse', () => {
                if (relatedToggle) {
                    relatedToggle.classList.add('active');
                    relatedToggle.setAttribute('aria-expanded', 'true');
                }
            });

            collapseEl.addEventListener('shown.bs.collapse', () => {
                const submenuLinks = collapseEl.querySelectorAll('.nav-link');
                const firstVisibleItem = relatedToggle || collapseEl;
                const lastVisibleItem = submenuLinks.length > 0
                        ? submenuLinks[submenuLinks.length - 1]
                        : collapseEl;

                window.requestAnimationFrame(() => {
                    keepSidebarRegionVisible(firstVisibleItem, lastVisibleItem);
                });
            });

            collapseEl.addEventListener('hide.bs.collapse', () => {
                if (relatedToggle) {
                    relatedToggle.setAttribute('aria-expanded', 'false');
                    if (!collapseEl.querySelector('.nav-link.active')) {
                        relatedToggle.classList.remove('active');
                    }
                }
            });
        });
    }

    function updateDateTime() {
        const now = new Date();
        const dateOptions = {weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'};
        const timeOptions = {hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false};
        const formattedDate = now.toLocaleDateString('en-US', dateOptions);
        const formattedTime = now.toLocaleTimeString('en-US', timeOptions);
        const dateTimeElement = document.getElementById('current-date-time');
        if (dateTimeElement) {
            dateTimeElement.textContent = `${formattedDate} then ${formattedTime}`;
        }
    }

    setInterval(updateDateTime, 1000);

    updateDateTime();

    const userDropdown = document.getElementById('user-dropdown');
    if (userDropdown) {
        let timeout;
        userDropdown.addEventListener('mouseenter', () => {
            clearTimeout(timeout);
            const dropdown = new bootstrap.Dropdown(userDropdown.querySelector('.dropdown-toggle'));
            dropdown.show();
        });
        userDropdown.addEventListener('mouseleave', () => {
            timeout = setTimeout(() => {
                const dropdown = new bootstrap.Dropdown(userDropdown.querySelector('.dropdown-toggle'));
                dropdown.hide();
            }, 200);
        });
    }

});



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

    const adminTables = $('#tablesdata, #example').filter(function () {
        return !$.fn.DataTable.isDataTable(this);
    });

    adminTables.each(function () {
        const emptyRow = $(this).find('tbody tr').filter(function () {
            const cells = $(this).children('td');
            return cells.length === 1 && Number(cells.attr('colspan') || 1) > 1;
        }).first();
        const emptyTableMessage = emptyRow.length ? $.trim(emptyRow.text()).replace(/\s+/g, ' ') : "No records found.";
        if (emptyRow.length) {
            emptyRow.remove();
        }

        const table = $(this).DataTable({
            ordering: true,
            lengthChange: true,
            autoWidth: false,
            scrollX: true,
            buttons: ["copy", "csv", "excel", "pdf", "print"],
            lengthMenu: [
                [10, 25, 50, -1],
                [10, 25, 50, 'All']
            ],
            columnDefs: [
                {targets: '_all', visible: true}
            ],
            language: {
                emptyTable: emptyTableMessage || "No records found.",
                zeroRecords: "No matching records found.",
                search: "Search",
                lengthMenu: "Rows _MENU_",
                info: "_START_ to _END_ of _TOTAL_ records"
            }
        });

        if (table && typeof table.buttons === "function") {
            table.buttons().container().appendTo(`#${this.id}_wrapper .col-md-6:eq(1)`);
        }
    });

});


/* Legacy admin page helpers retained from /js/admin-app.js */
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

/// end image size and formate


$(document).ready(function () {
    var $productEditors = $('#description,#shortDescription');
    if (!$productEditors.length || !$.fn || typeof $.fn.summernote !== 'function') {
        return;
    }

    $productEditors.summernote({

        height: 250,
        placeholder: 'Write content here...'
//        toolbar: [
//            ['style', ['bold', 'italic', 'underline', 'clear']],
//            ['font', ['strikethrough', 'superscript', 'subscript']],
//            ['para', ['ul', 'ol', 'paragraph']],
//            ['height', ['height']],
//            ['insert', ['link', 'picture', 'video']],
//            ['view', ['codeview', 'help']]
//        ],
//        callbacks: {
//            onInit: function () {
//                console.log('Summernote initialized');
//            }
//        }
    });
});

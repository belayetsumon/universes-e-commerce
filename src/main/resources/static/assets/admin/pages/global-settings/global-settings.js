/*
 * Page: Admin global settings
 * Used by: templates/admin/settings/global-settings.html
 * Purpose: initialize Summernote, preserve the active settings tab,
 * and keep page-specific validation isolated from the shared admin bundle
 */
(function ($) {
    if (!$ || !$.fn || !$.fn.summernote) {
        return;
    }

    $(function () {
        $('.editor').summernote({
            height: 250,
            placeholder: 'Write content here...',
            toolbar: [
                ['style', ['bold', 'italic', 'underline', 'clear']],
                ['font', ['strikethrough', 'superscript', 'subscript']],
                ['para', ['ul', 'ol', 'paragraph']],
                ['height', ['height']],
                ['insert', ['link', 'picture', 'video']],
                ['view', ['codeview', 'help']]
            ],
            callbacks: {
                onInit: function () {
                    console.log('Summernote initialized');
                }
            }
        });

        $('#settingsTabs button').on('shown.bs.tab', function () {
            localStorage.setItem('activeSettingsTab', $(this).attr('data-bs-target'));
        });

        var activeTab = localStorage.getItem('activeSettingsTab');
        if (activeTab) {
            $('#settingsTabs button[data-bs-target="' + activeTab + '"]').tab('show');
        }

        $('#settingsForm').on('submit', function (event) {
            var isValid = true;

            $(this).find('[required]').each(function () {
                if (!$(this).val()) {
                    $(this).addClass('is-invalid');
                    isValid = false;
                } else {
                    $(this).removeClass('is-invalid');
                }
            });

            if (!isValid) {
                event.preventDefault();
                alert('Please fill in all required fields.');
            }
        });

        $('input, select, textarea').on('input', function () {
            $(this).removeClass('is-invalid');
        });

        $('#settingsForm').on('click', '[data-confirm-message]', function (event) {
            var message = $(this).attr('data-confirm-message');
            if (message && !window.confirm(message)) {
                event.preventDefault();
            }
        });
    });
})(window.jQuery);

(function ($) {
    var storageKey = 'activeGlobalSettingsTab';

    function activateStoredTab() {
        var workspace = document.querySelector('.settings-workspace');
        var requestedSection = window.location.hash || null;
        if (!requestedSection && workspace && workspace.dataset.activeSection) {
            requestedSection = '#' + workspace.dataset.activeSection;
        }
        var activeTab = requestedSection || (window.localStorage ? window.localStorage.getItem(storageKey) : null);
        if (!activeTab) {
            return;
        }
        var trigger = document.querySelector('[data-bs-target="' + activeTab + '"]');
        if (trigger && window.bootstrap && window.bootstrap.Tab) {
            window.bootstrap.Tab.getOrCreateInstance(trigger).show();
        }
    }

    function bindTabs() {
        document.querySelectorAll('.settings-nav [data-bs-toggle="tab"]').forEach(function (trigger) {
            trigger.addEventListener('shown.bs.tab', function (event) {
                document.querySelectorAll('.settings-nav button').forEach(function (button) {
                    button.classList.remove('active');
                });
                event.target.classList.add('active');
                if (window.localStorage) {
                    window.localStorage.setItem(storageKey, event.target.getAttribute('data-bs-target'));
                }
            });
        });
        activateStoredTab();
    }

    function bindValidation() {
        var forms = document.querySelectorAll('.settings-section-form');
        if (!forms.length) {
            return;
        }

        forms.forEach(function (form) {
            form.addEventListener('submit', function (event) {
                var submitter = event.submitter;
                if (submitter && submitter.hasAttribute('formnovalidate')) {
                    return;
                }

                var firstInvalid = null;
                form.querySelectorAll('[required]').forEach(function (field) {
                    var invalid = !field.value || !field.value.trim();
                    field.classList.toggle('is-invalid', invalid);
                    if (invalid && !firstInvalid) {
                        firstInvalid = field;
                    }
                });

                if (firstInvalid) {
                    event.preventDefault();
                    var tabPane = firstInvalid.closest('.tab-pane');
                    if (tabPane) {
                        var tabTrigger = document.querySelector('[data-bs-target="#' + tabPane.id + '"]');
                        if (tabTrigger && window.bootstrap && window.bootstrap.Tab) {
                            window.bootstrap.Tab.getOrCreateInstance(tabTrigger).show();
                        }
                    }
                    firstInvalid.focus({preventScroll: true});
                    firstInvalid.scrollIntoView({behavior: 'smooth', block: 'center'});
                }
            });

            form.addEventListener('input', function (event) {
                if (event.target.matches('input, select, textarea')) {
                    event.target.classList.remove('is-invalid');
                }
            });
        });

        document.addEventListener('click', function (event) {
            var action = event.target.closest('[data-confirm-message]');
            if (!action) {
                return;
            }
            var message = action.getAttribute('data-confirm-message');
            if (message && !window.confirm(message)) {
                event.preventDefault();
            }
        });
    }

    function initEditors() {
        if (!$ || !$.fn || !$.fn.summernote) {
            return;
        }
        $('.editor').summernote({
            height: 220,
            placeholder: 'Write content here...',
            toolbar: [
                ['style', ['bold', 'italic', 'underline', 'clear']],
                ['para', ['ul', 'ol', 'paragraph']],
                ['insert', ['link']],
                ['view', ['codeview']]
            ]
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        bindTabs();
        bindValidation();
        initEditors();
    });
})(window.jQuery);

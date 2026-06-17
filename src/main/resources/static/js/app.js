// Date: 2026-04-26
// Persist active Bootstrap tabs on pages like single-product.
// The previous code only targeted #myTab buttons, while this page uses
// <a data-bs-toggle="tab"> inside #productTabs, so tab state and activation
// could drift or stop working after reloads.
document.addEventListener('DOMContentLoaded', function () {
    const tabContainers = document.querySelectorAll('.nav-tabs[id]');

    tabContainers.forEach((tabContainer) => {
        const tabTriggers = Array.from(tabContainer.querySelectorAll('[data-bs-toggle="tab"]'));
        if (!tabTriggers.length || typeof bootstrap === 'undefined' || !bootstrap.Tab) {
            return;
        }

        const storageKey = 'activeTab:' + tabContainer.id;
        const getTargetSelector = (trigger) => trigger.getAttribute('data-bs-target') || trigger.getAttribute('href');
        const savedTarget = sessionStorage.getItem(storageKey);

        const matchingSavedTrigger = savedTarget
                ? tabTriggers.find((trigger) => getTargetSelector(trigger) === savedTarget)
                : null;

        const initialTrigger = matchingSavedTrigger || tabTriggers.find((trigger) => trigger.classList.contains('active')) || tabTriggers[0];
        if (initialTrigger) {
            bootstrap.Tab.getOrCreateInstance(initialTrigger).show();
        }

        tabTriggers.forEach((trigger) => {
            trigger.addEventListener('shown.bs.tab', function (event) {
                const targetSelector = getTargetSelector(event.target);
                if (targetSelector) {
                    sessionStorage.setItem(storageKey, targetSelector);
                }
            });
        });
    });
});


    

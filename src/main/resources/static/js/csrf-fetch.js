(function installSameOriginCsrfFetch() {
    if (window.__sameOriginCsrfFetchInstalled || typeof window.fetch !== 'function') {
        return;
    }
    window.__sameOriginCsrfFetchInstalled = true;
    const originalFetch = window.fetch.bind(window);

    window.fetch = function csrfProtectedFetch(input, init) {
        const options = Object.assign({}, init || {});
        const requestMethod = String(
                options.method || (input instanceof Request ? input.method : 'GET')
        ).toUpperCase();
        const unsafeMethod = ['POST', 'PUT', 'PATCH', 'DELETE'].includes(requestMethod);
        const rawUrl = input instanceof Request ? input.url : String(input);
        const targetUrl = new URL(rawUrl, window.location.href);

        if (unsafeMethod && targetUrl.origin === window.location.origin) {
            const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const headerName = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
            if (token && headerName) {
                const headers = new Headers(options.headers || (input instanceof Request ? input.headers : undefined));
                headers.set(headerName, token);
                options.headers = headers;
            }
        }
        return originalFetch(input, options);
    };
})();

(function () {
    'use strict';

    const form = document.querySelector('[data-customer-cod-otp-form]');
    if (!form) {
        return;
    }

    const otpInput = form.querySelector('[name="otp"]');
    const tokenInput = form.querySelector('[data-customer-cod-otp-token]');
    const deviceInput = form.querySelector('[data-customer-cod-device]');
    const sendButton = form.querySelector('[data-customer-cod-send-otp]');
    const verifyButton = form.querySelector('[data-customer-cod-verify-otp]');
    const resendButton = form.querySelector('[data-customer-cod-resend-otp]');
    const otpArea = form.querySelector('[data-customer-cod-otp-area]');
    const message = form.querySelector('[data-customer-cod-otp-message]');
    let countdownTimer;

    function deviceId() {
        const fallback = 'checkout-' + Date.now() + '-' + Math.random().toString(36).slice(2);
        try {
            const existing = window.localStorage.getItem('checkoutDeviceId');
            const generated = existing || (window.crypto && window.crypto.randomUUID
                    ? window.crypto.randomUUID()
                    : fallback);
            window.localStorage.setItem('checkoutDeviceId', generated);
            return generated;
        } catch (error) {
            return fallback;
        }
    }

    deviceInput.value = deviceId();

    function setMessage(text, ok) {
        message.textContent = text || '';
        message.className = 'small mt-2 ' + (ok ? 'text-success' : 'text-danger');
    }

    function csrf(params) {
        const csrfInput = form.querySelector('input[type="hidden"][name^="_csrf"], input[type="hidden"][name="_csrf"]');
        if (csrfInput && csrfInput.name && csrfInput.value) {
            params.set(csrfInput.name, csrfInput.value);
        }
        return params;
    }

    async function post(url, params) {
        const response = await fetch(url, {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Accept': 'application/json'
            },
            body: csrf(params).toString()
        });
        let data;
        try {
            data = await response.json();
        } catch (error) {
            throw new Error(response.status === 401 || response.status === 403
                    ? 'Your session expired. Please sign in again.'
                    : 'Verification is temporarily unavailable. Please try again.');
        }
        if (!response.ok || !data.success) {
            throw new Error(data.message || 'Verification failed. Please try again.');
        }
        return data;
    }

    function startCountdown(seconds) {
        window.clearInterval(countdownTimer);
        let remaining = Math.max(Number(seconds) || 60, 1);
        resendButton.disabled = true;
        const render = function () {
            const minutes = String(Math.floor(remaining / 60)).padStart(2, '0');
            const secondsPart = String(remaining % 60).padStart(2, '0');
            resendButton.textContent = 'Resend code in ' + minutes + ':' + secondsPart;
        };
        render();
        countdownTimer = window.setInterval(function () {
            remaining -= 1;
            if (remaining <= 0) {
                window.clearInterval(countdownTimer);
                resendButton.disabled = false;
                resendButton.textContent = 'Resend code';
                return;
            }
            render();
        }, 1000);
    }

    async function sendOtp(endpoint) {
        const params = new URLSearchParams();
        params.set('deviceFingerprint', deviceInput.value);
        const data = await post(endpoint, params);
        if (data.verified || data.verificationRequired === false) {
            window.location.reload();
            return;
        }
        tokenInput.value = data.sessionToken || '';
        otpArea.classList.remove('d-none');
        otpInput.focus();
        setMessage(data.message || 'Verification code sent.', true);
        startCountdown(data.resendAvailableInSeconds || 60);
    }

    sendButton.addEventListener('click', async function () {
        sendButton.disabled = true;
        try {
            await sendOtp('/checkout/customer/mobile/send-otp');
        } catch (error) {
            setMessage(error.message, false);
        } finally {
            sendButton.disabled = false;
        }
    });

    resendButton.addEventListener('click', async function () {
        resendButton.disabled = true;
        try {
            await sendOtp('/checkout/customer/mobile/resend-otp');
        } catch (error) {
            setMessage(error.message, false);
            resendButton.disabled = false;
        }
    });

    verifyButton.addEventListener('click', async function () {
        const params = new URLSearchParams();
        params.set('sessionToken', tokenInput.value);
        params.set('otp', otpInput.value.trim());
        params.set('deviceFingerprint', deviceInput.value);
        verifyButton.disabled = true;
        try {
            await post('/checkout/customer/mobile/verify-otp', params);
            window.location.reload();
        } catch (error) {
            setMessage(error.message, false);
            otpInput.select();
        } finally {
            verifyButton.disabled = false;
        }
    });
}());

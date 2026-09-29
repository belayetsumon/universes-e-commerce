package com.ecommerce.app.module.checkout.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

class CheckoutSessionSerializationFilterTest {

    private final CheckoutSessionSerializationFilter filter = new CheckoutSessionSerializationFilter();

    @ParameterizedTest
    @ValueSource(strings = {
        "/cart/add",
        "/carts/updateQuantity",
        "/district/save-district",
        "/cart_address/add_shipping_address",
        "/checkout/guest/mobile/verify-otp",
        "/order/savebyvendor"
    })
    void unsafeCheckoutStatePathsAreSerialized(String path) {
        MockHttpServletRequest request = request("POST", path, new MockHttpSession());

        assertTrue(filter.isSerializedMutation(request));
    }

    @Test
    void safeReadsAreNotSerialized() {
        MockHttpServletRequest request = request("GET", "/cart/checkout", new MockHttpSession());

        assertFalse(filter.isSerializedMutation(request));
    }

    @Test
    void sameSessionMutationsCannotEnterConcurrently() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest firstRequest = request("POST", "/carts/updateQuantity", session);
        MockHttpServletRequest secondRequest = request("POST", "/order/savebyvendor", session);
        CountDownLatch firstEntered = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        CountDownLatch secondEntered = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> first = executor.submit(() -> {
                filter.doFilter(firstRequest, new MockHttpServletResponse(), (request, response) -> {
                    firstEntered.countDown();
                    try {
                        releaseFirst.await();
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(interrupted);
                    }
                });
                return null;
            });
            assertTrue(firstEntered.await(2, TimeUnit.SECONDS));

            Future<?> second = executor.submit(() -> {
                secondStarted.countDown();
                filter.doFilter(secondRequest, new MockHttpServletResponse(), (request, response) -> secondEntered.countDown());
                return null;
            });
            assertTrue(secondStarted.await(2, TimeUnit.SECONDS));
            assertFalse(secondEntered.await(250, TimeUnit.MILLISECONDS));

            releaseFirst.countDown();
            assertTrue(secondEntered.await(2, TimeUnit.SECONDS));
            first.get(2, TimeUnit.SECONDS);
            second.get(2, TimeUnit.SECONDS);
        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
        }
    }

    private MockHttpServletRequest request(String method, String path, MockHttpSession session) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        request.setSession(session);
        return request;
    }
}

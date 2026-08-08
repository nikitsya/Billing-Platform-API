package com.nikitsya.billing.ping;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PingControllerTest {

    @Test
    void ping() {
        PingController controller = new PingController();
        String result = controller.ping();
        assertEquals("ok", result);
    }
}
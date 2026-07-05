package com.agrofinance;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Sanity check: verifies the full Spring application context
 * loads successfully with no misconfigured beans.
 */
@SpringBootTest
class AgroFinanceApplicationTests {

    @Test
    void contextLoads() {
        // Intentionally empty.
        // If the Spring context fails to start, this test fails automatically.
    }

}

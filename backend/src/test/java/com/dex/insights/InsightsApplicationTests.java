package com.dex.insights;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** Smoke test: the context starts and the dataset loads without error. */
@SpringBootTest
class InsightsApplicationTests {

    @Test
    void contextLoads() {
        // Assertion-free by design: a failure to wire beans or load the dataset fails the test.
    }
}

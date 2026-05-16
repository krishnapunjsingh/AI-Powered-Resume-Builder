package com.resumeai.registry;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ServiceRegistryApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Eureka Server starts successfully
        // For Service Registry, we mainly test that the Eureka server
        // configuration is properly loaded and the application starts
    }
}

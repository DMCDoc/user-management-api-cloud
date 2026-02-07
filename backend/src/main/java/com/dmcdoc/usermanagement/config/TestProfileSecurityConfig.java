// Deprecated: replaced by TestSecurityConfig in test sources.
// This file intentionally left blank to avoid bean definition conflicts during tests.
package com.dmcdoc.usermanagement.config;

/**
 * Removed implementation to avoid duplicate 'authenticationManager' bean with
 * test TestSecurityConfig. If you need custom test beans, prefer adding
 * a @TestConfiguration class under src/test/java.
 */
public class TestProfileSecurityConfig {
}

package com.example.mongocrud;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

public final class CaffeineRuntimeHints implements RuntimeHintsRegistrar {

    private static final String BOUNDED_CACHE_IMPLEMENTATION =
            "com.github.benmanes.caffeine.cache.SSMSA";
    private static final String NODE_FACTORY =
            "com.github.benmanes.caffeine.cache.PSAMS";

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Caffeine derives this optimized cache class name from the configured eviction and expiry policy.
        hints.reflection().registerType(
                TypeReference.of(BOUNDED_CACHE_IMPLEMENTATION),
                MemberCategory.ACCESS_DECLARED_FIELDS,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS
        );
        hints.reflection().registerType(
                TypeReference.of(NODE_FACTORY),
                MemberCategory.ACCESS_DECLARED_FIELDS,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS
        );
    }
}

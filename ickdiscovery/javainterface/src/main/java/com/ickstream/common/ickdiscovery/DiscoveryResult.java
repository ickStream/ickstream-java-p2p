/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickdiscovery;

import java.util.Arrays;
import java.util.List;

/**
 * The result of an {@link IckDiscovery} operation
 */
public class DiscoveryResult {
    /**
     * Successful
     */
    public static final DiscoveryResult SUCCESS = new DiscoveryResult(0, "SUCCESS");
    /**
     * Running
     */
    public static final DiscoveryResult RUNNING = new DiscoveryResult(1, "RUNNING");
    /**
     * A socket error occurred
     */
    public static final DiscoveryResult SOCKET_ERROR = new DiscoveryResult(2, "SOCKET_ERROR");
    /**
     * A threading error occurred
     */
    public static final DiscoveryResult THREAD_ERROR = new DiscoveryResult(3, "THREAD_ERROR");
    /**
     * A memory error occurred
     */
    public static final DiscoveryResult MEMORY_ERROR = new DiscoveryResult(4, "MEMORY_ERROR");

    private static final List<DiscoveryResult> availableResults = Arrays.asList(
            SUCCESS, RUNNING, SOCKET_ERROR, THREAD_ERROR, MEMORY_ERROR
    );

    private int value;
    private String description;

    private DiscoveryResult(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * Convert a raw integer error code to a DiscoveryResult object
     *
     * @param value The raw integer error code
     * @return A DiscoveryResult object
     */
    public static DiscoveryResult valueOf(int value) {
        for (DiscoveryResult result : availableResults) {
            if (result.value == value) {
                return result;
            }
        }
        return new DiscoveryResult(value, "UNKNOWN(" + value + ")");
    }

    /**
     * Get the raw integer error code
     *
     * @return The raw integer error code
     */
    public int value() {
        return this.value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DiscoveryResult && ((DiscoveryResult) o).value == this.value;
    }

    /**
     * @return Textual description of the error
     */
    @Override
    public String toString() {
        return description;
    }
}

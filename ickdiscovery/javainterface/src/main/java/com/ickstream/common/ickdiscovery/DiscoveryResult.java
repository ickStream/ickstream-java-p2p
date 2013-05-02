/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickdiscovery;

import java.util.Arrays;
import java.util.List;

public class DiscoveryResult {
    public static final DiscoveryResult SUCCESS = new DiscoveryResult(0, "SUCCESS");
    public static final DiscoveryResult RUNNING = new DiscoveryResult(1, "RUNNING");
    public static final DiscoveryResult SOCKET_ERROR = new DiscoveryResult(2, "SOCKET_ERROR");
    public static final DiscoveryResult THREAD_ERROR = new DiscoveryResult(3, "THREAD_ERROR");
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

    public static DiscoveryResult valueOf(int value) {
        for (DiscoveryResult result : availableResults) {
            if (result.value == value) {
                return result;
            }
        }
        return new DiscoveryResult(value, "UNKNOWN(" + value + ")");
    }

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

    @Override
    public String toString() {
        return description;
    }
}

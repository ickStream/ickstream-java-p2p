/*
 * Copyright (c) 2013, ickStream GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of ickStream nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.ickstream.common.ickp2p;

import java.util.Arrays;
import java.util.List;

/**
 * The result of an {@link com.ickstream.common.ickp2p.IckP2p} operation
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

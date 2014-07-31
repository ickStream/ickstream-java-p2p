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
 * Type of service, this class is represented as a bitmask so it's important to use the {@link #isType(ServiceType)} method
 * to check if it matches a specific service type.
 */
public class ServiceType {
    static final ServiceType GENERIC = new ServiceType(0, "GENERIC");

    /**
     * A player
     */
    public static final ServiceType PLAYER = new ServiceType(1, "PLAYER");

    /**
     * A controller
     */
    public static final ServiceType CONTROLLER = new ServiceType(2, "CONTROLLER");

    /**
     * A service which is neither player nor controller
     */
    public static final ServiceType SERVICE = new ServiceType(4, "SERVICE");

    /**
     * A debug device
     */
    public static final ServiceType DEBUG = new ServiceType(8, "DEBUG");

    /**
     * Any kind of service
     */
    public static final ServiceType ANY = new ServiceType(15, "ANY");

    private static final List<ServiceType> availableTypes = Arrays.asList(
            PLAYER, CONTROLLER, SERVICE, DEBUG
    );

    private int value;
    private String name;

    private ServiceType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    /**
     * @return The raw bitmask value
     */
    public int value() {
        return value;
    }

    /**
     * Check if the device is implementing the specified service
     *
     * @param serviceType The service to check for
     * @return true if the service is implemented
     */
    public boolean isType(ServiceType serviceType) {
        return GENERIC.value == serviceType.value || ANY.value == serviceType.value || (this.value & serviceType.value) != 0;
    }

    /**
     * Convert a raw bitmask value to a {@link ServiceType} instance
     *
     * @param value The raw bitmask value
     * @return The service type object
     */
    public static ServiceType valueOf(int value) {
        if (value == GENERIC.value) {
            return GENERIC;
        }
        if (value == ANY.value) {
            return ANY;
        }
        for (ServiceType type : availableTypes) {
            if (type.value == value) {
                return type;
            }
        }
        return new ServiceType(value, null);
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ServiceType && ((ServiceType) o).value == this.value;
    }

    /**
     * @return Textual description of all services represented by the object
     */
    @Override
    public String toString() {
        if (value == ANY.value) {
            return ANY.name;
        }
        if (value != 0) {
            StringBuilder sb = new StringBuilder();
            for (ServiceType serviceType : availableTypes) {
                if (isType(serviceType)) {
                    sb.append(sb.length() > 0 ? "," : "").append(serviceType.name);
                }
            }
            if (sb.length() == 0) {
                sb.append("UNKNOWN(").append(value).append(")");
            }
            return sb.toString();
        } else {
            return GENERIC.name;
        }
    }
}

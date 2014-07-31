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

/**
 * Device listener which is called when devices are added, updated or removed.
 * A typical application is only interested in connected device and for this purpose it makes most sense to
 * use the {@link DiscoveryAdapter} and override the {@link #onConnectedDevice(DiscoveryEvent)} and
 * {@link #onDisconnectedDevice(String)} methods
 */
public interface DiscoveryListener {
    /**
     * A new device has been discovered and meta data about it has been loaded
     *
     * @param event The identity of the device
     */
    void onInitializedDevice(DiscoveryEvent event);

    /**
     * A connection has been established with a new device
     *
     * @param event Information about the device
     */
    void onConnectedDevice(DiscoveryEvent event);

    /**
     * The connection has been lost with a previously connected device
     *
     * @param deviceId Identity of the device
     */
    void onDisconnectedDevice(String deviceId);

    /**
     * A device have been discovered but connection has not yet been established with it
     *
     * @param event The identity of the device
     */
    void onDiscoveredDevice(DiscoveryEvent event);

    /**
     * A device have been removed on its own request
     *
     * @param deviceId The identity of the device
     */
    void onByeByeDevice(String deviceId);

    /**
     * A device has been removed because it is no longer answering on the heartbeat messages
     *
     * @param deviceId The identity of the device
     */
    void onExpiredDevice(String deviceId);

    /**
     * The device has been removed because the discovery has been switch off
     *
     * @param deviceId The identity of the device
     */
    void onTerminatedDevice(String deviceId);
}

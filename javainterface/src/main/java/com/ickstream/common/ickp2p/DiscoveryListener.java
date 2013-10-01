/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
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
     * @param event The identity of the device
     */
    void onInitializedDevice(DiscoveryEvent event);

    /**
     * A connection has been established with a new device
     *
     * @param event   Information about the device
     */
    void onConnectedDevice(DiscoveryEvent event);

    /**
     * The connection has been lost with a previously connected device
     *
     * @param deviceId   Identity of the device
     */
    void onDisconnectedDevice(String deviceId);

    /**
     * A device have been discovered but connection has not yet been established with it
     * @param event The identity of the device
     */
    void onDiscoveredDevice(DiscoveryEvent event);

    /**
     * A device have been removed on its own request
     * @param deviceId The identity of the device
     */
    void onByeByeDevice(String deviceId);

    /**
     * A device has been removed because it is no longer answering on the heartbeat messages
     * @param deviceId The identity of the device
     */
    void onExpiredDevice(String deviceId);

    /**
     * The device has been removed because the discovery has been switch off
     * @param deviceId The identity of the device
     */
    void onTerminatedDevice(String deviceId);
}

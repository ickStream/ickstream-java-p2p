/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickdiscovery;

/**
 * Device listener which is called when devices are added, updated or removed
 */
public interface DeviceListener {
    /**
     * A new device which have been discovered
     *
     * @param deviceId   The device identity
     * @param deviceName The name or null if the device doesn't have a name
     * @param services   The services the device offers, use {@link ServiceType#isType} to verify if the device offers a specific service
     */
    void onDeviceAdded(String deviceId, String deviceName, ServiceType services);

    /**
     * A previously discovered device have been updated with new information or services
     *
     * @param deviceId   The device identity
     * @param deviceName The name or null if the device doesn't have a name
     * @param services   The services the device offers, use {@link ServiceType#isType} to verify if the device offers a specific service
     */
    void onDeviceUpdated(String deviceId, String deviceName, ServiceType services);

    /**
     * A previously discovered device have been removed from the network
     *
     * @param deviceId The device identity
     */
    void onDeviceRemoved(String deviceId);
}

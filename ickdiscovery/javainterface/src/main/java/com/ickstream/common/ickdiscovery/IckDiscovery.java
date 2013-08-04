/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickdiscovery;

/**
 * An interface for a discovery module that discovers devices on the local network
 */
public interface IckDiscovery extends MessageSender {
    /**
     * Initialize discovery of devices on the local network
     *
     * @param deviceId         The device identity to use, typically generated using {@link java.util.UUID#randomUUID} and converting it to upper case
     * @param networkInterface The network interface or IP-address to use
     * @param deviceName       The device name to use
     * @param dataFolder       The folder where data should be stored, only set it if you want to provide additional UPnP configuration
     * @return {@link DiscoveryResult#SUCCESS} if discovery started successfully
     */
    DiscoveryResult initDiscovery(String deviceId, String networkInterface, String deviceName, String dataFolder);

    /**
     * Shutdown discovery of devices to the local network
     */
    void endDiscovery();

    /**
     * Add a service which the current device offers
     *
     * @param serviceType The service to add
     * @return {@link DiscoveryResult#SUCCESS} if service was added successfully
     */
    DiscoveryResult addService(ServiceType serviceType);

    /**
     * Remove a service which the current device no longer offers
     *
     * @param serviceType The service to remove
     * @return {@link DiscoveryResult#SUCCESS} if service was successfully removed
     */
    DiscoveryResult removeService(ServiceType serviceType);

    /**
     * Get the name of a device
     *
     * @param deviceId The identity of the device
     * @return The name of the device or null if it doesn't have a name
     */
    String getDeviceName(String deviceId);

    /**
     * Add a device listener which wants to be informed about new, updated and removed devices
     *
     * @param listener A device listener
     */
    void addDeviceListener(DeviceListener listener);

    /**
     * Add a message listener which wants to be informed about new messages from devices
     *
     * @param listener A message listener
     */
    void addMessageListener(MessageListener listener);
}

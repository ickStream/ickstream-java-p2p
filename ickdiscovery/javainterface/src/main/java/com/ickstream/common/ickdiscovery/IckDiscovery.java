/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickdiscovery;

public interface IckDiscovery extends MessageSender {
    DiscoveryResult initDiscovery(String deviceId, String networkInterface, String deviceName, String dataFolder);

    void endDiscovery();

    DiscoveryResult addService(ServiceType serviceType);

    DiscoveryResult removeService(ServiceType serviceType);

    String getDeviceName(String deviceId);

    void addDeviceListener(DeviceListener listener);

    void addMessageListener(MessageListener listener);
}

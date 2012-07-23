/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickdiscovery;

public interface IckDiscovery extends MessageSender {
    void initDiscovery(String deviceId, String networkInterface, String deviceName, String dataFolder);

    void endDiscovery();

    void addService(ServiceType serviceType);

    void removeService(ServiceType serviceType);

    String getDeviceName(String deviceId);

    void addDeviceListener(DeviceListener listener);

    void addMessageListener(MessageListener listener);
}

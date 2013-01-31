/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickdiscovery;

public interface DeviceListener {
    void onDeviceAdded(String deviceId, String deviceName, ServiceType services);
    void onDeviceUpdated(String deviceId, String deviceName, ServiceType services);
    void onDeviceRemoved(String deviceId);
}

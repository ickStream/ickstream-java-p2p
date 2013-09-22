/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickp2p;

import java.net.URL;

public class DiscoveryEvent {
    private String deviceId;
    private String deviceName;
    private URL deviceLocation;
    private String deviceAddress;
    private int devicePort;
    private ServiceType services;

    public DiscoveryEvent() {
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public URL getDeviceLocation() {
        return deviceLocation;
    }

    public void setDeviceLocation(URL deviceLocation) {
        this.deviceLocation = deviceLocation;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public int getDevicePort() {
        return devicePort;
    }

    public void setDevicePort(int devicePort) {
        this.devicePort = devicePort;
    }

    public ServiceType getServices() {
        return services;
    }

    public void setServices(ServiceType services) {
        this.services = services;
    }
}

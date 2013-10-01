/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickp2p;

/**
 * Adapter class which should be overridden instead of {@link DiscoveryListener} interface if you are only
 * interested in subscribing to some time of discovery notifications
 */
public class DiscoveryAdapter implements DiscoveryListener {

    @Override
    public void onInitializedDevice(DiscoveryEvent event) {
    }

    @Override
    public void onConnectedDevice(DiscoveryEvent event) {
    }

    @Override
    public void onDisconnectedDevice(String deviceId) {
    }

    @Override
    public void onDiscoveredDevice(DiscoveryEvent event) {
    }

    @Override
    public void onByeByeDevice(String deviceId) {
    }

    @Override
    public void onExpiredDevice(String deviceId) {
    }

    @Override
    public void onTerminatedDevice(String deviceId) {
    }
}

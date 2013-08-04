/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickdiscovery;

/**
 * A message listener which is called when new messages are received from other devices
 */
public interface MessageListener {
    /**
     * Called when a new message have been received from another device
     *
     * @param sourceDeviceId    The device which the message is originated from
     * @param targetDeviceId    The device which the message is sent to
     * @param targetServiceType The type of service which the device is sent to, note that this can also be {@link ServiceType#ANY} if it's intented for all services
     * @param message           The message received
     */
    void onMessage(String sourceDeviceId, String targetDeviceId, ServiceType targetServiceType, byte[] message);
}

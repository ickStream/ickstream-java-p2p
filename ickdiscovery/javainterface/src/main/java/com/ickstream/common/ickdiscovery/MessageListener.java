/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickdiscovery;

public interface MessageListener {
    void onMessage(String sourceDeviceId, String targetDeviceId, ServiceType targetServiceType, byte[] message);
}

/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickdiscovery;

public interface MessageSender {
    Boolean sendMessage(String targetDeviceId, ServiceType targetServiceType, byte[] message);

    Boolean sendMessage(String targetDeviceId, byte[] message);

    Boolean sendMessage(byte[] message);
}

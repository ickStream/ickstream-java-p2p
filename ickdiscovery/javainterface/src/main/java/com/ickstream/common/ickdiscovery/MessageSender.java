/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickdiscovery;

public interface MessageSender {
    void sendMessage(String deviceId, byte[] message);
    void sendMessage(byte[] message);
}

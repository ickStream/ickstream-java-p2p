/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickdiscovery;

/**
 * A message sender which is able to send messages to other devices
 */
public interface MessageSender {
    /**
     * Send a message to the specified service and device
     *
     * @param targetDeviceId    The identity of the device to send the message to
     * @param targetServiceType The type of service to send the message to, can be {@link ServiceType#ANY} if the message should be sent to all services
     * @param message           The message to send
     * @return true if the message was successfully sent, else false
     */
    Boolean sendMessage(String targetDeviceId, ServiceType targetServiceType, byte[] message);

    /**
     * Send a message to all services on the specified device
     *
     * @param targetDeviceId The identity of the device to send the message to
     * @param message        The message to send
     * @return true if the message was successfully sent, else false
     */
    Boolean sendMessage(String targetDeviceId, byte[] message);

    /**
     * Broadcast a message to all devices
     *
     * @param message The mesasge to send
     * @return true if the message was successfully sent, else false
     */
    Boolean sendMessage(byte[] message);
}

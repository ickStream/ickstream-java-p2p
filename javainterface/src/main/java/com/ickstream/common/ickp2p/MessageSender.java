/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickp2p;

/**
 * A message sender which is able to send messages to other devices
 */
public interface MessageSender {
    /**
     * Send a message to the specified service and device
     *
     * @param targetDeviceId The identity of the device to send the message to
     * @param targetService  The type of service to send the message to, can be {@link ServiceType#ANY} if the message should be sent to all services
     * @param sourceService  The type of service which the message is sent from
     * @param message        The message to send
     * @return true if the message was successfully sent, else false
     */
    void sendMsg(String targetDeviceId, ServiceType targetService, ServiceType sourceService, byte[] message) throws IckP2pException;

    /**
     * Broadcast a message to all devices
     *
     * @param message       The mesasge to send
     * @param sourceService The type of service which the message is sent from
     * @return true if the message was successfully sent, else false
     */
    void sendMsg(ServiceType sourceService, byte[] message) throws IckP2pException;
}

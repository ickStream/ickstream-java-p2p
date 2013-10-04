/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickp2p;

/**
 * An interface for a discovery module that discovers devices on the local network
 */
public interface IckP2p extends MessageSender {
    /**
     * Initialize discovery of devices on the local network
     *
     * @param deviceUuid The device identity to use, typically generated using {@link java.util.UUID#randomUUID} and converting it to upper case
     * @param lifetime
     * @param service
     * @param deviceName The device name to use
     * @param dataFolder The folder where data should be stored, only set it if you want to provide additional UPnP configuration
     * @return {@link DiscoveryResult#SUCCESS} if discovery started successfully
     */
    void create(String deviceName, String deviceUuid, String dataFolder, Integer lifetime, Integer port, ServiceType service) throws IckP2pException;

    /**
     * Resume/start discovery of devices to the local network
     */
    void resume() throws IckP2pException;

    /**
     * Suspend discovery of devices to the local network
     */
    void suspend() throws IckP2pException;

    /**
     * Shutdown discovery of devices to the local network
     */
    void end() throws IckP2pException;

    /**
     * Add an additional interface
     *
     * @param ifName The network interface or IP-address to use
     * @throws IckP2pException
     */
    void addInterface(String ifName, String hostname) throws IckP2pException;

    /**
     * Enable or disable UPnP loopback functionality that makes the local device be discovered locally in
     * same way as any other devices
     *
     * @param enable Enable loopback
     * @throws IckP2pException
     */
    void upnpLoopback(boolean enable) throws IckP2pException;

    /**
     * Add a discovery listener which wants to be informed about new and removed devices
     *
     * @param listener A device listener
     */
    void addDiscoveryListener(DiscoveryListener listener);

    /**
     * Add a message listener which wants to be informed about new messages from devices
     *
     * @param listener A message listener
     */
    void addMessageListener(MessageListener listener);
}

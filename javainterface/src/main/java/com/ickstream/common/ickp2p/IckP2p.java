/*
 * Copyright (c) 2013, ickStream GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of ickStream nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

    /**
     * Remove a discovery listener previously added with {@link #addDiscoveryListener(DiscoveryListener)}
     *
     * @param listener A device listener
     */
    void removeDiscoveryListener(DiscoveryListener listener);

    /**
     * Remove a message listener previously added with {@link #addMessageListener(MessageListener)}
     *
     * @param listener A message listener
     */
    void removeMessageListener(MessageListener listener);
}

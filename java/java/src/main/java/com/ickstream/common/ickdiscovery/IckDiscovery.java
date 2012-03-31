/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickdiscovery;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class IckDiscovery
{

	public native void initDiscovery(String deviceId, String networkInterface);
    public native void endDiscovery();
    public native void addService(int service);
    public native void removeService(int service);
    public native String[] getDeviceList(int types);
    public native int getDeviceType(String deviceId);
    public native void sendMessage(String deviceId, String message);
    
    private void onMessage(String deviceId, String message) {
        for (MessageListener listener : messageListeners) {
            listener.onMessage(deviceId, message);
        }
    }
    private void onDevice(String deviceId, int change, int services) {
        for (DeviceListener listener : deviceListeners) {
            if(change == 0) {
                listener.onDeviceAdded(deviceId, services);
            }else if(change == 1) {
                listener.onDeviceRemoved(deviceId);
            }else {
                listener.onDeviceUpdated(deviceId, services);
            }
        }
    }


    private Set<DeviceListener> deviceListeners = new TreeSet<DeviceListener>();
    private Set<MessageListener> messageListeners = new TreeSet<MessageListener>();

    public static final int SERVICE_GENERIC = 0;
    public static final int SERVICE_PLAYER = 1;
    public static final int SERVICE_CONTROLLER = 2;
    public static final int SERVICE_SERVER_GENERIC = 4;

    public static interface DeviceListener {
        void onDeviceAdded(String deviceId, int services);
        void onDeviceUpdated(String deviceId, int services);
        void onDeviceRemoved(String deviceId);
    }
    public static interface MessageListener {
        void onMessage(String deviceId, String message);
    }
    public void addDeviceListener(DeviceListener listener) {
        deviceListeners.add(listener);
    }
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

	public static void main(String[] args) throws InterruptedException {
        System.loadLibrary("ickDiscoveryJNI");
        final IckDiscovery discovery = new IckDiscovery();
        discovery.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String deviceId, String message) {
                System.out.println(deviceId+": "+message);
                try {
                    Thread.sleep(1000);
                    discovery.sendMessage(deviceId, "I'm am here");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        String deviceId = UUID.randomUUID().toString();
        discovery.initDiscovery(deviceId,"127.0.0.1");
        discovery.addService(SERVICE_PLAYER);
        Thread.sleep(10000);
        discovery.endDiscovery();
	}
}
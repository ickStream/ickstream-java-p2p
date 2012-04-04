/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickdiscovery;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

public class IckDiscovery
{

	public native void initDiscovery(String deviceId, String networkInterface);
    public native void endDiscovery();
    public native void addService(int service);
    public native void removeService(int service);
    public native String[] getDeviceList(int types);
    public native int getDeviceType(String deviceId);
    public native void sendMessage(String deviceId, String message);

    public IckDiscovery() {
        System.loadLibrary("ickDiscoveryJNI");
    }

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
        final IckDiscovery discovery = new IckDiscovery();
        discovery.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String deviceId, String message) {
                System.out.println(deviceId+": "+message);
                if(!message.startsWith("I am here")) {
                    try {
                        Thread.sleep(1000);
                        discovery.sendMessage(deviceId, "I am here");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        String deviceId = UUID.randomUUID().toString();
        final Set<String> devices = new HashSet<String>();
        discovery.addDeviceListener(new DeviceListener() {
            @Override
            public void onDeviceAdded(String deviceId, int services) {
                System.out.println("Got device: "+deviceId);
                devices.add(deviceId);
            }

            @Override
            public void onDeviceUpdated(String deviceId, int services) {
                System.out.println("Updated device: "+deviceId);
                devices.add(deviceId);
            }

            @Override
            public void onDeviceRemoved(String deviceId) {
                System.out.println("Removed device: "+deviceId);
                devices.remove(deviceId);
            }
        });
        discovery.initDiscovery(deviceId, getNetworkAddress());
        discovery.addService(SERVICE_PLAYER);
        for(int i=0;i<10;i++) {
            Thread.sleep(10000);
            for (String device : devices) {
                discovery.sendMessage(device, "Hello "+device+" are you there ?");
            }
        }
        discovery.endDiscovery();
	}
    private static String getNetworkAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
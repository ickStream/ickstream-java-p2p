/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickdiscovery;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

public class IckDiscovery implements com.ickstream.protocol.device.MessageSender
{

	public native void initDiscovery(String deviceId, String networkInterface, String deviceName, String dataFolder);
    public native void endDiscovery();
    public native void addService(int service);
    public native void removeService(int service);
    public native String[] getDeviceList(int types);
    public native int getDeviceType(String deviceId);
    public native String getDeviceName(String deviceId);
    public native void sendMessage(String deviceId, String message);

    public IckDiscovery() {
        try {
            System.loadLibrary("ickDiscoveryJNI");
        }catch(UnsatisfiedLinkError e) {
            String classPath = System.getProperty("java.class.path", "");
            List<String> classPathElements = new ArrayList<String>(Arrays.asList(classPath.split(":")));
            Boolean loaded = false;
            if(classPath.matches(".*/libickDiscoveryJNI.*")) {
                for (String classPathElement : classPathElements) {
                    if(classPathElement.matches(".*/libickDiscoveryJNI.*")) {
                        System.load(classPathElement);
                        loaded = true;
                        break;
                    }
                }
            }else {
                String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                if(path != null) {
                    File files[] = new File(path).getParentFile().listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.getName().matches("libickDiscoveryJNI.*");
                        }
                    });
                    if(files.length>0) {
                        try {
                            System.load(files[0].getCanonicalPath());
                            loaded = true;
                        } catch (IOException e1) {
                            // Just ignore
                        }
                    }
                }
            }
            if(!loaded) {
                throw e;
            }
        }
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
}

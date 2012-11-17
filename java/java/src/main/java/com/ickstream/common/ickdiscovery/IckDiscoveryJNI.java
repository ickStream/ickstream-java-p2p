/*
 * Copyright (C) 2012 Erland Isaksson (erland@isaksson.info)
 * All rights reserved.
 */

package com.ickstream.common.ickdiscovery;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

public class IckDiscoveryJNI implements IckDiscovery {

    @Override
    public native void initDiscovery(String deviceId, String networkInterface, String deviceName, String dataFolder);

    @Override
    public native void endDiscovery();

    public native void addService(int service);

    public native void removeService(int service);

    public native int getDevicePort(String deviceId);

    public native String getDeviceAddress(String deviceId);

    @Override
    public native String getDeviceName(String deviceId);

    @Override
    public native void sendMessage(String deviceId, byte[] message);

    @Override
    public void sendMessage(byte[] message) {
        sendMessage(null, message);
    }


    @Override
    public void addService(ServiceType serviceType) {
        addService(serviceType.value());
    }

    @Override
    public void removeService(ServiceType serviceType) {
        addService(serviceType.value());
    }

    public IckDiscoveryJNI() {
        this(null);
    }

    public IckDiscoveryJNI(String libraryName) {
        final String postfix = System.getProperty("os.arch").contains("64") ? "64" : "32";
        try {
            if (libraryName == null) {
                libraryName = "ickDiscoveryJNI" + postfix;
            }
            System.loadLibrary(libraryName);
        } catch (UnsatisfiedLinkError e) {
            String classPath = System.getProperty("java.class.path", "");
            List<String> classPathElements = new ArrayList<String>(Arrays.asList(classPath.split(":")));
            Boolean loaded = false;
            if (classPath.matches(".*/libickDiscoveryJNI" + postfix + ".*")) {
                for (String classPathElement : classPathElements) {
                    if (classPathElement.matches(".*/libickDiscoveryJNI" + postfix + ".*")) {
                        System.load(classPathElement);
                        loaded = true;
                        break;
                    }
                }
            } else {
                String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                if (path != null) {
                    File files[] = new File(path).getParentFile().listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.getName().matches("libickDiscoveryJNI" + postfix + ".*");
                        }
                    });
                    if (files.length > 0) {
                        try {
                            System.load(files[0].getCanonicalPath());
                            loaded = true;
                        } catch (IOException e1) {
                            // Just ignore
                        }
                    }
                }
            }
            if (!loaded) {
                throw e;
            }
        }
    }

    private void onMessage(String deviceId, byte[] message) {
        for (MessageListener listener : messageListeners) {
            listener.onMessage(deviceId, message);
        }
    }

    private void onDevice(final String deviceId, final int change, final int services) {
        for (final DeviceListener listener : deviceListeners) {
            if (change == 0) {
                final String deviceName = getDeviceName(deviceId);
                listener.onDeviceAdded(deviceId, deviceName, ServiceType.valueOf(services));
            } else if (change == 1) {
                listener.onDeviceRemoved(deviceId);
            } else {
                final String deviceName = getDeviceName(deviceId);
                listener.onDeviceUpdated(deviceId, deviceName, ServiceType.valueOf(services));
            }
        }
    }


    private Set<DeviceListener> deviceListeners = new HashSet<DeviceListener>();
    private Set<MessageListener> messageListeners = new HashSet<MessageListener>();

    @Override
    public void addDeviceListener(DeviceListener listener) {
        deviceListeners.add(listener);
    }

    @Override
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }
}

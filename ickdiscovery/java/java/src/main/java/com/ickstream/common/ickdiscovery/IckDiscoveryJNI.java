/*
 * Copyright (C) 2013 ickStream GmbH
 * All rights reserved
 */

package com.ickstream.common.ickdiscovery;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

public class IckDiscoveryJNI implements IckDiscovery {
    private String deviceId;

    public native int nativeInitDiscovery(String deviceId, String networkInterface, String deviceName, String dataFolder);

    @Override
    public native void endDiscovery();

    public native int addService(int service);

    public native int removeService(int service);

    public native int getDevicePort(String deviceId);

    public native String getDeviceAddress(String deviceId);

    @Override
    public native String getDeviceName(String deviceId);

    public native boolean nativeSendMessage(String sourceDeviceId, String targetDeviceId, byte[] message);

    public native boolean nativeSendTargetedMessage(String sourceDeviceId, String targetDeviceId, int serviceType, byte[] message);

    @Override
    public DiscoveryResult initDiscovery(String deviceId, String networkInterface, String deviceName, String dataFolder) {
        this.deviceId = deviceId;
        int result = nativeInitDiscovery(deviceId, networkInterface, deviceName, dataFolder);
        return DiscoveryResult.valueOf(result);
    }

    @Override
    public Boolean sendMessage(String targetDeviceId, byte[] message) {
        return nativeSendMessage(deviceId, targetDeviceId, message);
    }

    @Override
    public Boolean sendMessage(byte[] message) {
        return nativeSendMessage(deviceId, null, message);
    }

    @Override
    public Boolean sendMessage(String targetDeviceId, ServiceType serviceType, byte[] message) {
        if (serviceType != null) {
            return nativeSendTargetedMessage(deviceId, targetDeviceId, serviceType.value(), message);
        } else {
            return nativeSendMessage(deviceId, targetDeviceId, message);
        }
    }

    @Override
    public DiscoveryResult addService(ServiceType serviceType) {
        int result = addService(serviceType.value());
        return DiscoveryResult.valueOf(result);
    }

    @Override
    public DiscoveryResult removeService(ServiceType serviceType) {
        int result = removeService(serviceType.value());
        return DiscoveryResult.valueOf(result);
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

    private void onNativeMessage(String sourceDeviceId, String targetDeviceId, int targetServiceType, byte[] message) {
        for (MessageListener listener : messageListeners) {
            listener.onMessage(sourceDeviceId, targetDeviceId, ServiceType.valueOf(targetServiceType), message);
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

/*
 * Copyright (C) 2013 ickStream GmbH
 * All rights reserved
 */

package com.ickstream.common.ickdiscovery;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * An implementation of {@link IckDiscovery} using JNI to connect to the native ickStream P2P module
 */
public class IckDiscoveryJNI implements IckDiscovery {
    private String deviceId;

    private native int nativeInitDiscovery(String deviceId, String networkInterface, String deviceName, String dataFolder);

    /**
     * See {@link com.ickstream.common.ickdiscovery.IckDiscovery#endDiscovery()} for more information
     */
    @Override
    public native void endDiscovery();

    private native int addService(int service);

    private native int removeService(int service);

    /**
     * Get the communication port used by the device
     *
     * @param deviceId The device to get communication port for
     * @return The port used
     */
    public native int getDevicePort(String deviceId);

    /**
     * Get IP address of device
     *
     * @param deviceId The device to get IP address for
     * @return The IP address
     */
    public native String getDeviceAddress(String deviceId);

    /**
     * See {@link IckDiscovery#getDeviceName(String)} for more information
     */
    @Override
    public native String getDeviceName(String deviceId);

    private native boolean nativeSendMessage(String sourceDeviceId, String targetDeviceId, byte[] message);

    private native boolean nativeSendTargetedMessage(String sourceDeviceId, String targetDeviceId, int serviceType, byte[] message);

    /**
     * See {@link IckDiscovery#initDiscovery(String, String, String, String)} for more information
     */
    @Override
    public DiscoveryResult initDiscovery(String deviceId, String networkInterface, String deviceName, String dataFolder) {
        this.deviceId = deviceId;
        int result = nativeInitDiscovery(deviceId, networkInterface, deviceName, dataFolder);
        return DiscoveryResult.valueOf(result);
    }

    /**
     * See {@link MessageSender#sendMessage(String, byte[])} for more information
     */
    @Override
    public Boolean sendMessage(String targetDeviceId, byte[] message) {
        return nativeSendMessage(deviceId, targetDeviceId, message);
    }

    /**
     * See {@link MessageSender#sendMessage(byte[])} for more information
     */
    @Override
    public Boolean sendMessage(byte[] message) {
        return nativeSendMessage(deviceId, null, message);
    }

    /**
     * See {@link MessageSender#sendMessage(String, ServiceType, byte[])} for more information
     */
    @Override
    public Boolean sendMessage(String targetDeviceId, ServiceType serviceType, byte[] message) {
        if (serviceType != null) {
            return nativeSendTargetedMessage(deviceId, targetDeviceId, serviceType.value(), message);
        } else {
            return nativeSendMessage(deviceId, targetDeviceId, message);
        }
    }

    /**
     * See {@link IckDiscovery#addService(ServiceType)} for more information
     */
    @Override
    public DiscoveryResult addService(ServiceType serviceType) {
        int result = addService(serviceType.value());
        return DiscoveryResult.valueOf(result);
    }

    /**
     * See {@link IckDiscovery#removeService(ServiceType)} for more information
     */
    @Override
    public DiscoveryResult removeService(ServiceType serviceType) {
        int result = removeService(serviceType.value());
        return DiscoveryResult.valueOf(result);
    }

    /**
     * Creates a new instance
     */
    public IckDiscoveryJNI() {
        this(null);
    }

    /**
     * Creates a new instance but use a custom JNI implementation.
     * The JNI implementation will be loaded from the specified name but with "32" or "64" appended to it depending on the OS architecture used
     *
     * @param libraryName The name of the JNI implementation module
     */
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
                        try {
                            System.load(classPathElement);
                            loaded = true;
                            break;
                        } catch (UnsatisfiedLinkError e1) {
                            // Just ignore, we want to continue and try any other alternatives
                        }
                    }
                }
            } else {
                String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                if (path != null) {
                    try {
                        path = URLDecoder.decode(path, "UTF-8");
                        File files[] = new File(path).getParentFile().listFiles(new FileFilter() {
                            @Override
                            public boolean accept(File file) {
                                return file.getName().matches("libickDiscoveryJNI" + postfix + ".*");
                            }
                        });
                        if (files.length > 0) {
                            for (File file : files) {
                                try {
                                    System.load(file.getCanonicalPath());
                                    loaded = true;
                                    break;
                                } catch (UnsatisfiedLinkError e1) {
                                    // Just ignore, we want to continue and try any other alternatives
                                } catch (IOException e1) {
                                    // Just ignore
                                }
                            }
                        }
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
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

    /**
     * See {@link IckDiscovery#addDeviceListener(DeviceListener)} for more information
     *
     * @param listener A device listener
     */
    @Override
    public void addDeviceListener(DeviceListener listener) {
        deviceListeners.add(listener);
    }

    /**
     * See {@link IckDiscovery#addMessageListener(MessageListener)} for more information
     *
     * @param listener A message listener
     */
    @Override
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }
}

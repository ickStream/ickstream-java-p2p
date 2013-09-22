/*
 * Copyright (C) 2013 ickStream GmbH
 * All rights reserved
 */

package com.ickstream.common.ickp2p;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation of {@link IckP2p} using JNI to connect to the native ickStream P2P module
 */
public class IckP2pJNI implements IckP2p {
    private String deviceId;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private native int ickP2pCreate(String deviceName, String deviceUuid, String dataFolder, int lifetime, int port, int service);

    private native int ickP2pAddInterface(String ifname, String hostname);

    private native int ickP2pResume();

    private native int ickP2pSuspend();

    private native int ickP2pEnd();

    private native int ickP2pSendMsg(String uuid, int targetServices, int sourceService, byte[] message);

    @Override
    public void create(String deviceName, String deviceUuid, String dataFolder, Integer lifetime, Integer port, ServiceType service) throws IckP2pException {
        int error = ickP2pCreate(deviceName, deviceUuid, dataFolder, lifetime != null ? lifetime : 100, port != null ? port : 1900, service.value());
        if (error != 0) {
            throw new IckP2pException(error);
        } else {
            deviceId = deviceUuid;
        }
    }

    @Override
    public void resume() throws IckP2pException {
        int error = ickP2pResume();
        if (error != 0) {
            throw new IckP2pException(error);
        }
    }

    @Override
    public void suspend() throws IckP2pException {
        int error = ickP2pSuspend();
        if (error != 0) {
            throw new IckP2pException(error);
        }
    }

    @Override
    public void end() throws IckP2pException {
        int error = ickP2pEnd();
        if (error != 0) {
            throw new IckP2pException(error);
        }
    }

    @Override
    public void addInterface(String ifName, String hostname) throws IckP2pException {
        int error = ickP2pAddInterface(ifName, hostname);
        if (error != 0) {
            throw new IckP2pException(error);
        }
    }

    @Override
    public void sendMsg(String targetDeviceUuid, ServiceType targetService, ServiceType sourceService, byte[] message) throws IckP2pException {
        int error = ickP2pSendMsg(targetDeviceUuid, targetService.value(), sourceService.value(), message);
        if (error != 0) {
            throw new IckP2pException(error);
        }
    }

    @Override
    public void sendMsg(ServiceType sourceService, byte[] message) throws IckP2pException {
        int error = ickP2pSendMsg(null, 0, sourceService.value(), message);
        if (error != 0) {
            throw new IckP2pException(error);
        }
    }

    private void messageCb(final String sourceDeviceUuid, final int sourceService, final int targetService, final byte[] message) {
        System.err.println("messageCb(fromUuid=" + sourceDeviceUuid + ",fromService=" + sourceService + ",toService=" + targetService + ",message=byte[" + message.length + "])");
        for (final MessageListener listener : messageListeners) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    listener.onMessage(sourceDeviceUuid, ServiceType.valueOf(sourceService), deviceId, ServiceType.valueOf(targetService), message);
                }
            });
        }
    }

    private void discoveryCb(final String deviceUuid, final String deviceName, final String deviceLocation, final int service, final int change) {
        System.err.println("discoveryCb(uuid=" + deviceUuid + ",name=" + deviceName + ",location=" + deviceLocation + ",service=" + service + ",command=" + change + ")");

        final DiscoveryEvent discoveryEvent = new DiscoveryEvent();
        discoveryEvent.setDeviceId(deviceUuid);
        discoveryEvent.setDeviceName(deviceName);
        if (service > 0) {
            discoveryEvent.setServices(ServiceType.valueOf(service));
        }
        if (deviceLocation != null) {
            try {
                URL deviceUrl = new URL(deviceLocation);
                discoveryEvent.setDeviceLocation(deviceUrl);
                discoveryEvent.setDeviceAddress(deviceUrl.getHost());
                discoveryEvent.setDevicePort(deviceUrl.getPort());
            } catch (MalformedURLException e) {
                Pattern p = Pattern.compile("^([^:]+):([0-9]+)$");
                Matcher m = p.matcher(deviceLocation);
                if(m.find()) {
                    discoveryEvent.setDeviceAddress(m.group(1));
                    discoveryEvent.setDevicePort(Integer.valueOf(m.group(2)));
                }else {
                    e.printStackTrace();
                }
            }
        }
        for (final DiscoveryListener listener : deviceListeners) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (change == 1) {
                        listener.onConnectedDevice(discoveryEvent);
                    } else if (change == 2) {
                        listener.onDisconnectedDevice(deviceUuid);
                    } else if (change == 5) {
                        listener.onNewDevice(discoveryEvent);
                    } else if (change == 6) {
                        listener.onRemovedDevice(deviceUuid);
                    } else if (change == 7) {
                        listener.onExpiredDevice(deviceUuid);
                    } else if (change == 8) {
                        listener.onTerminatedDevice(deviceUuid);
                    } else {
                        System.err.println("Unsupported discovery event: " + change);
                    }
                }
            });
        }
    }

    /**
     * Creates a new instance
     */
    public IckP2pJNI() {
        this(null);
    }

    /**
     * Creates a new instance but use a custom JNI implementation.
     * The JNI implementation will be loaded from the specified name but with "32" or "64" appended to it depending on the OS architecture used
     *
     * @param libraryName The name of the JNI implementation module
     */
    public IckP2pJNI(String libraryName) {
        final String postfix = System.getProperty("os.arch").contains("64") ? "64" : "32";
        try {
            if (libraryName == null) {
                libraryName = "ickstream-ickp2p-java-native-jni" + postfix;
            }
            System.loadLibrary(libraryName);
        } catch (UnsatisfiedLinkError e) {
            String classPath = System.getProperty("java.class.path", "");
            List<String> classPathElements = new ArrayList<String>(Arrays.asList(classPath.split(":")));
            Boolean loaded = false;
            if (classPath.matches(".*/libickstream\\-ickp2p\\-java\\-native\\-jni" + postfix + ".*")) {
                for (String classPathElement : classPathElements) {
                    if (classPathElement.matches(".*/libickstream\\-ickp2p\\-java\\-native\\-jni" + postfix + ".*")) {
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
                                return file.getName().matches("libickstream\\-ickp2p\\-java\\-native\\-jni" + postfix + ".*");
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


    private Set<DiscoveryListener> deviceListeners = new HashSet<DiscoveryListener>();
    private Set<MessageListener> messageListeners = new HashSet<MessageListener>();

    /**
     * See {@link IckP2p#addDiscoveryListener(DiscoveryListener)} for more information
     *
     * @param listener A device listener
     */
    @Override
    public void addDiscoveryListener(DiscoveryListener listener) {
        deviceListeners.add(listener);
    }

    /**
     * See {@link IckP2p#addMessageListener(MessageListener)} for more information
     *
     * @param listener A message listener
     */
    @Override
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }
}

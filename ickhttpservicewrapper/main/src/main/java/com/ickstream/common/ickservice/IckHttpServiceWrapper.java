/*
 * Copyright (C) 2013 ickStream GmbH
 * All rights reserved
 */

package com.ickstream.common.ickservice;

import com.ickstream.common.ickdiscovery.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.Enumeration;

public class IckHttpServiceWrapper implements MessageListener, DeviceListener {
    private URL callbackUrl;
    private IckDiscovery ickDiscovery = new IckDiscoveryJNI();
    HttpClient httpClient;
    Boolean debug = Boolean.FALSE;
    private String serviceId;

    private IckHttpServiceWrapper(String serviceId, final String name, URL callbackUrl) {
        try {
            String debugString = System.getProperty("com.ickstream.common.ickservice.debug");
            if (debugString != null && debugString.equalsIgnoreCase("true")) {
                debug = Boolean.TRUE;
            }
            String customStdOut = System.getProperty("com.ickstream.common.ickservice.stdout");
            String customStdErr = System.getProperty("com.ickstream.common.ickservice.stderr");
            String isDaemon = System.getProperty("com.ickstream.common.ickservice.daemon");
            if (isDaemon != null && isDaemon.equalsIgnoreCase("true")) {
                try {
                    System.out.println("Starting in daemon mode");
                    System.in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (customStdOut != null) {
                try {
                    System.out.println("Redirecting stdout to: " + customStdOut);
                    System.setOut(new PrintStream(new FileOutputStream(customStdOut)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (isDaemon != null && isDaemon.equalsIgnoreCase("true")) {
                System.out.close();
            }
            if (customStdErr != null) {
                try {
                    System.err.println("Redirecting stderr to: " + customStdErr);
                    if (customStdOut != null && customStdErr.equals(customStdOut)) {
                        System.setErr(System.out);
                    } else {
                        System.setErr(new PrintStream(new FileOutputStream(customStdErr)));
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (isDaemon != null && isDaemon.equalsIgnoreCase("true")) {
                System.err.close();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        this.callbackUrl = callbackUrl;
        this.serviceId = serviceId;
        String ipAddress = getCurrentNetworkAddress();
        httpClient = new DefaultHttpClient();
        ickDiscovery.addMessageListener(this);
        ickDiscovery.addDeviceListener(this);
        System.out.println("initDiscovery(\"" + serviceId + "\",\"" + ipAddress + "\",\"" + name + "\")");
        DiscoveryResult result = ickDiscovery.initDiscovery(serviceId, ipAddress, name, null);
        System.out.println("initDiscovery = " + result);
        System.out.println("addService(" + ServiceType.SERVICE + ")");
        result = ickDiscovery.addService(ServiceType.SERVICE);
        System.out.println("addService = " + result);
        System.out.println(name + " initialized with identity " + serviceId + " at " + ipAddress + " using callback " + callbackUrl.toString());
        System.out.flush();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println(name + " is about to shutdown");
                System.out.flush();
                ickDiscovery.endDiscovery();
                System.out.println(name + " shutdown");
                System.out.flush();
            }
        });
        synchronized (this) {
            while (true) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    @Override
    public void onDeviceAdded(String deviceId, String deviceName, ServiceType serviceType) {
        System.out.println("New device: " + deviceId + "(" + deviceName + ") of type " + serviceType.toString());
        System.out.flush();
    }

    @Override
    public void onDeviceUpdated(String deviceId, String deviceName, ServiceType serviceType) {
        System.out.println("Updated device: " + deviceId + "(" + deviceName + ") of type " + serviceType.toString());
        System.out.flush();
    }

    @Override
    public void onDeviceRemoved(String deviceId) {
        System.out.println("Removed device: " + deviceId);
        System.out.flush();
    }

    @Override
    public void onMessage(String sourceDeviceId, String targetDeviceId, ServiceType targetServiceType, byte[] byteMessage) {
        String message;
        try {
            message = new String(byteMessage, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Just ignore, we should always get UTF-8
            e.printStackTrace();
            System.err.flush();
            return;
        }
        if (debug) {
            System.out.println("From " + sourceDeviceId + ": " + message);
            System.out.flush();
        }
        HttpPost httpRequest = new HttpPost(callbackUrl.toString());
        try {
            httpRequest.setEntity(new StringEntity(message));
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            if (httpResponse.getStatusLine().getStatusCode() < 400) {
                String responseString = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
                if (responseString != null && responseString.length() > 0) {
                    if (debug) {
                        System.out.println("To " + sourceDeviceId + ": " + responseString);
                        System.out.flush();
                    }
                    ickDiscovery.sendMessage(sourceDeviceId, responseString.getBytes("UTF-8"));
                }
            }
        } catch (ClientProtocolException e) {
            System.err.println("Error handling message from " + sourceDeviceId + ": " + message);
            e.printStackTrace();
            System.err.flush();
        } catch (UnsupportedEncodingException e) {
            System.err.println("Error handling message from " + sourceDeviceId + ": " + message);
            e.printStackTrace();
            System.err.flush();
        } catch (IOException e) {
            System.err.println("Error handling message from " + sourceDeviceId + ": " + message);
            e.printStackTrace();
            System.err.flush();
        }
    }

    private String getCurrentNetworkAddress() {
        String currentAddress = null;
        try {
            InetAddress addrs[] = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());


            for (InetAddress addr : addrs) {
                if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
                    currentAddress = addr.getHostAddress();
                    break;
                }
            }
            if (currentAddress == null || currentAddress.length() == 0) {
                try {
                    Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
                    for (NetworkInterface iface : Collections.list(ifaces)) {
                        Enumeration<InetAddress> raddrs = iface.getInetAddresses();
                        for (InetAddress raddr : Collections.list(raddrs)) {
                            if (!raddr.isLoopbackAddress() && raddr.isSiteLocalAddress()) {
                                currentAddress = raddr.getHostAddress();
                                break;
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        } catch (UnknownHostException e) {
        }
        if (currentAddress == null || currentAddress.trim().length() == 0) {
            currentAddress = "127.0.0.1";
        }
        return currentAddress;
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length >= 3) {
            try {
                new IckHttpServiceWrapper(args[0], args[1], new URL(args[2]));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Thread.sleep(1000);
                System.exit(2);
            } catch (RuntimeException e) {
                System.err.println("Error starting ickStream daemon: " + e.getLocalizedMessage());
                e.printStackTrace();
                System.err.flush();
                Thread.sleep(1000);
                System.exit(3);
            }
        } else {
            System.err.println("Invalid parameters, should be called with <uuid> <name> <url> as parameters");
            System.err.flush();
            Thread.sleep(1000);
            System.exit(1);
        }
    }
}
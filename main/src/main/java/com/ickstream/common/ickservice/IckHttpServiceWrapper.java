package com.ickstream.common.ickservice;

import com.ickstream.common.ickdiscovery.IckDiscovery;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
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
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class IckHttpServiceWrapper implements IckDiscovery.MessageListener {
    private URL callbackUrl;
    private IckDiscovery ickDiscovery = new IckDiscovery();
    HttpClient httpClient;

    private IckHttpServiceWrapper(String serviceId, final String name, URL callbackUrl) throws BackingStoreException {
        String customStdOut = System.getProperty("com.ickstream.common.ickservice.stdout");
        String customStdErr = System.getProperty("com.ickstream.common.ickservice.stderr");
        String isDaemon = System.getProperty("com.ickstream.common.ickservice.daemon");
        if (isDaemon != null && isDaemon.equalsIgnoreCase("true")) {
            try {
                System.in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (customStdOut != null) {
            try {
                System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(customStdOut, true))));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (isDaemon != null && isDaemon.equalsIgnoreCase("true")) {
            System.out.close();
        }
        if (customStdErr != null) {
            try {
                if (customStdOut != null && customStdErr.equals(customStdOut)) {
                    System.setErr(System.out);
                } else {
                    System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(customStdErr, true))));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (isDaemon != null && isDaemon.equalsIgnoreCase("true")) {
            System.err.close();
        }

        this.callbackUrl = callbackUrl;
        Preferences preferences = Preferences.userNodeForPackage(this.getClass());
        preferences.flush();
        String ipAddress = getCurrentNetworkAddress();
        httpClient = new DefaultHttpClient();
        ickDiscovery.addMessageListener(this);
        ickDiscovery.initDiscovery(serviceId, ipAddress, name, null);
        ickDiscovery.addService(4);
        System.out.println(name + " initialized with identity " + serviceId + " using callback " + callbackUrl.toString());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println(name + " is about to shutdown");
                ickDiscovery.endDiscovery();
                System.out.println(name + " shutdown");
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
    public void onMessage(String deviceId, String message) {
        HttpPost httpRequest = new HttpPost(callbackUrl.toString());
        try {
            httpRequest.setEntity(new StringEntity(message));
            HttpResponse httpResponse = httpClient.execute(httpRequest);
            String responseString = EntityUtils.toString(httpResponse.getEntity(),"utf-8");
            ickDiscovery.sendMessage(deviceId, responseString);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

    public static void main(String[] args) {
        if (args.length >= 3) {
            try {
                new IckHttpServiceWrapper(args[0], args[1], new URL(args[2]));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.exit(2);
            } catch (BackingStoreException e) {
                e.printStackTrace();
                System.exit(3);
            }
        } else {
            System.err.println("Invalid parameters, should be called with <uuid> <name> <url> as parameters");
            System.exit(1);
        }
    }
}

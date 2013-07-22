package com.WifiAudioDistribution;

import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class NetworkClient {
    public static final String TAG = "NetworkClient";

    public static HashMap<String, NsdServiceInfo> mClientMap = new HashMap<String, NsdServiceInfo>();
    public static HashMap<String, Socket> mSocketMap = new HashMap<String, Socket>();

    public static void add(String key, NsdServiceInfo value) {
        if(!mClientMap.containsKey(key)) {
            mClientMap.put(key, value);
        }
    }

    public static void remove(String key) {
        if(mClientMap.containsKey(key)) {
            mClientMap.remove(key);
        }
    }

    public static void setUpSockets() {
        Set<String> set = mClientMap.keySet();
        Iterator<String> itr = set.iterator();
        while(itr.hasNext()) {
            String key = itr.next();
            NsdServiceInfo serviceInfo = mClientMap.get(key);

            try {
                Socket nSocket = new Socket(serviceInfo.getHost(), serviceInfo.getPort());
                mSocketMap.put(key, nSocket);
            } catch(IOException e) {
                Log.e(TAG, "Socket creation failed", e);
            }
        }
    }

    public Socket mSocket;

    public NetworkClient(String key) {
        // Create socket based on key
        NsdServiceInfo serviceInfo = mClientMap.get(key);
        try {
            mSocket = new Socket(serviceInfo.getHost(), serviceInfo.getPort());
        } catch(IOException e) {
            Log.e(TAG, "Couldnt create client socket: " + key, e);
        }
    }




}

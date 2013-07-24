package com.WifiAudioDistribution.Nsd;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

public class NsdHelper {
    public static final String TAG = "MYAPP:NsdHelper";

    public static final String SERVICE_NAME = "WifiHomeAudio";
    public static final String SERVICE_TYPE = "_wifihomeaudio._tcp.";

    public final boolean ALLOW_SAME_MACHINE_DISCOVERY = true;

    public NsdManager mNsdManager;
    public NsdManager.ResolveListener mResolveListener;
    public NsdManager.DiscoveryListener mDiscoveryListener;
    public NsdManager.RegistrationListener mRegistrationListener;

    public boolean mRegisteredService;
    public boolean mDiscoverServices;

    public String mServiceName;

    private OnResolvedServiceListener mOnResolvedServiceListener;

    public NsdHelper(NsdManager nsdManager) {
        mNsdManager = nsdManager;

        mRegisteredService = false;
        mDiscoverServices = false;

        initializeResolveListener();
        initializeDiscoveryListener();
        initializeRegistrationListener();
    }

    public void tearDown() {
        tearDownServiceRegister();
        tearDownServiceDiscovery();
    }

    private void tearDownServiceRegister() {
        if(mRegisteredService) {
            mNsdManager.unregisterService(mRegistrationListener);
        }
    }

    private void tearDownServiceDiscovery() {
        if(mDiscoverServices) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }
    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);

        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    public void discoverServices() {
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                mRegisteredService = true;

                mServiceName = NsdServiceInfo.getServiceName();
                Log.d(TAG, "Service Name: "+mServiceName);

                // Discover services after we know we're online
                discoverServices();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) { }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                Log.i(TAG, "Unregistered Service: " + mServiceName);

                mRegisteredService = false;
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                tearDownServiceRegister();
            }
        };
    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                mDiscoverServices = true;

                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success" + service);

                if(!service.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if(!ALLOW_SAME_MACHINE_DISCOVERY && service.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else if(service.getServiceName().contains(SERVICE_NAME)) {
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                mDiscoverServices = false;

                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);

                tearDownServiceDiscovery();
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);

                tearDownServiceDiscovery();
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "Resolve Succeeded. " + serviceInfo);

                Log.d(TAG, "Found friendly service.");
                Log.d(TAG, "Host: "+serviceInfo.getHost());
                Log.d(TAG, "Port: "+serviceInfo.getPort());

                if(mOnResolvedServiceListener != null) {
                    mOnResolvedServiceListener.onResolve(serviceInfo);
                }
            }
        };
    }

    public void setOnResolvedService(OnResolvedServiceListener l) {
        mOnResolvedServiceListener = l;
    }

    public static interface OnResolvedServiceListener {
        void onResolve(NsdServiceInfo serviceInfo);
    }
}

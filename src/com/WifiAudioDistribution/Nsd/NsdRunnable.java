package com.WifiAudioDistribution.Nsd;

import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import com.WifiAudioDistribution.ClientInfo;
import com.WifiAudioDistribution.ClientManager;
import com.WifiAudioDistribution.Db.ClientInfoDbWrapper;
import com.WifiAudioDistribution.MyActivity;

public class NsdRunnable implements Runnable {
    private final String TAG = "MYAPP:NsdRunnable";

    // Length of time to run discovery
    private MyActivity mActivity;
    private int msRunning;
    private ClientManager mClientManager;

    public NsdRunnable(MyActivity activity, ClientManager mClientManager, int msRunning) {
        this.mActivity = activity;
        this.mClientManager = mClientManager;
        this.msRunning = msRunning;
    }

    public void run() {
        Log.d(TAG, "Start Discovery, create NsdHelper.");

        if(!mClientManager.isBound()) {
            Log.d(TAG, "Server Socket is not bound.");
            return;
        }

        NsdHelper mNsdHelper = new NsdHelper(mClientManager.getNsdManager());
        mNsdHelper.registerService(mClientManager.getPort());
        mNsdHelper.setOnResolvedService(new NsdHelper.OnResolvedServiceListener() {
            public void onResolve(NsdServiceInfo serviceInfo) {
                ClientInfoDbWrapper clientInfo = new ClientInfoDbWrapper();
                clientInfo.host = serviceInfo.getHost().getHostAddress();
                clientInfo.name = serviceInfo.getServiceName();
                clientInfo.port = serviceInfo.getPort();

                mActivity.resolvedClient(clientInfo);
            }
        });

        long startTime = System.currentTimeMillis();

        while(true) {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                Log.d(TAG, "Interrupted thread.");
                break;
            }

            if((System.currentTimeMillis() - startTime) > msRunning) {
                break;
            }
        }

        Log.d(TAG, "Stop Discovery and tear down NsdHelper");

        mNsdHelper.tearDown();
    }
}

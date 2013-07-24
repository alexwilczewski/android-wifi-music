package com.WifiAudioDistribution;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import com.WifiAudioDistribution.Nsd.NsdRunnable;

public class MyActivity extends Activity {
    private final String TAG = "MYAPP:Activity";

    public ClientManager mClientManager;

    public Thread mDiscoveryThread;

    public MyActivity mActivity;

    // This allows me to easily mock up objects instead of being tied to NsdServiceInfo objects
    public NsdAdapter mNsdAdapter;

    Handler mNsdAdapterHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(mNsdAdapter != null) {
                ClientInfo clientInfo = (ClientInfo) msg.obj;
                mNsdAdapter.add(clientInfo);
                mNsdAdapter.notifyDataSetChanged();
            }
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Lock to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mActivity = this;

        Button mMusicBtn = (Button) findViewById(R.id.music);
        Button mDiscoverBtn = (Button) findViewById(R.id.discover);

        mMusicBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mClientManager.initializeSendingThread();
            }
        });

        mDiscoverBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                discoveryThread(10000);
            }
        });

        ListView mConnectionsList = (ListView) findViewById(R.id.connections);
        mNsdAdapter = new NsdAdapter(this, R.layout.row);
        mConnectionsList.setAdapter(mNsdAdapter);

        mClientManager = new ClientManager(this);
        mClientManager.initializeServerSocket();

        init();
    }

    public void init() {
        // Add static clientinfo
        ClientInfo staticInfo = new ClientInfo();
        staticInfo.host = "192.168.0.11";
        staticInfo.port = ClientManager.PORT;
        staticInfo.name = "Static Client";

        resolvedClient(staticInfo);

        ClientInfo staticInfo2 = new ClientInfo();
        staticInfo2.host = "192.168.0.19";
        staticInfo2.port = ClientManager.PORT;
        staticInfo2.name = "Static Client 2";

        resolvedClient(staticInfo2);
    }

    // Used to find services over a thread and timed
    public void discoveryThread(int msRunning) {
        mDiscoveryThread = new Thread(new NsdRunnable(this, mClientManager, msRunning));
        mDiscoveryThread.start();
    }

    public void resolvedClient(ClientInfo clientInfo) {
        mClientManager.serviceResolved(clientInfo);

        Message msg = new Message();
        msg.obj = clientInfo;
        mNsdAdapterHandler.sendMessage(msg);
    }

    @Override
    protected void onPause() {
        if(mDiscoveryThread != null) {
            mDiscoveryThread.interrupt();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if(mDiscoveryThread != null) {
            mDiscoveryThread.interrupt();
        }
        if(mClientManager != null) {
            Log.d(TAG, "Client Manager Tear Down");
            mClientManager.tearDown();
        }

        super.onDestroy();
    }
}

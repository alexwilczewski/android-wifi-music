package com.WifiAudioDistribution;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import com.WifiAudioDistribution.Activities.ManageConnectionsActivity;
import com.WifiAudioDistribution.Db.ClientInfoDataSource;
import com.WifiAudioDistribution.Db.ClientInfoDbWrapper;
import com.WifiAudioDistribution.Nsd.NsdRunnable;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class MyActivity extends Activity {
    private final String TAG = "MYAPP:Activity";

    final int ACTIVITY_CHOOSE_FILE = 1;
    private final int ACTIVITY_MANAGE_CONNECTIONS = 2;

    public ClientManager mClientManager;

    public Thread mDiscoveryThread;

    public MyActivity mActivity;

    public ClientInfoDataSource mDataSource;

    // This allows me to easily mock up objects instead of being tied to NsdServiceInfo objects
    public NsdAdapter mNsdAdapter;

    Handler mNsdAdapterHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(mNsdAdapter != null) {
                ClientInfoDbWrapper item = (ClientInfoDbWrapper) msg.obj;
                mNsdAdapter.add(item);
                mNsdAdapter.notifyDataSetChanged();
            }
        }
    };

    Handler mCheckClientAvailability = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int count = mNsdAdapter.getCount();
            for(int i = 0; i<count; i++) {
                ClientInfoDbWrapper item = mNsdAdapter.getItem(i);
                item.linkAvailable();
            }
            mNsdAdapter.notifyDataSetChanged();
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "OnCreate");

        init();
    }

    public void init() {
        // Lock to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mActivity = this;

        mNsdAdapter = new NsdAdapter(this, R.layout.row);

        mClientManager = new ClientManager(this);
        mClientManager.initializeServerSocket();

        mDataSource = new ClientInfoDataSource(this);
        mDataSource.open();

        layoutInit();
    }

    public void layoutInit() {
        setContentView(R.layout.main);

        ListView mConnectionsList = (ListView) findViewById(R.id.connections);
        mConnectionsList.setAdapter(mNsdAdapter);
        refreshConnectionList();

        Button mMusicBtn = (Button) findViewById(R.id.music);
        Button mManageConnectionsBtn = (Button) findViewById(R.id.manage_connections);
        Button mRediscovery = (Button) findViewById(R.id.rediscovery);

        mMusicBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                sendMusic();
            }
        });

        mManageConnectionsBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                modifyConnections();
            }
        });

        mRediscovery.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                refreshAvailability();
            }
        });
    }

    public void modifyConnections() {
        Intent intent = new Intent(this, ManageConnectionsActivity.class);
        startActivityForResult(intent, ACTIVITY_MANAGE_CONNECTIONS);
    }

    public void sendMusic() {
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("file/*");
        intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case ACTIVITY_CHOOSE_FILE: {
                if (resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    String filePath = uri.getPath();
                    Log.i(TAG, "PATH: "+filePath);

                    mClientManager.initializeSendingThread(new File(filePath));
                }
            }
            case ACTIVITY_MANAGE_CONNECTIONS: {
                // Refresh Adapter List always after coming from Management
                refreshConnectionList();
                Log.d(TAG, "Modify Finished Successfully");
                if(resultCode == RESULT_OK) { }
            }
        }
    }

    public void refreshConnectionList() {
        mNsdAdapter.clear();

        List<ClientInfoDbWrapper> clients = mDataSource.getAll();
        Iterator<ClientInfoDbWrapper> itr = clients.iterator();
        while(itr.hasNext()) {
            resolvedClient(itr.next());
        }
        refreshAvailability();
    }

    public void refreshAvailability() {
        mCheckClientAvailability.sendEmptyMessage(0);
    }

    // Used to find services over a thread and timed
    public void discoveryThread(int msRunning) {
        mDiscoveryThread = new Thread(new NsdRunnable(this, mClientManager, msRunning));
        mDiscoveryThread.start();
    }

    public void resolvedClient(ClientInfoDbWrapper clientInfo) {
        mClientManager.serviceResolved(clientInfo);

        Message msg = new Message();
        msg.obj = clientInfo;
        mNsdAdapterHandler.sendMessage(msg);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "OnPause");
        if(mDiscoveryThread != null) {
            mDiscoveryThread.interrupt();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "OnResume");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mDataSource.close();

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

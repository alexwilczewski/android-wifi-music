package com.WifiAudioDistribution.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.WifiAudioDistribution.Adapter.PodListAdapter;
import com.WifiAudioDistribution.ClientManager;
import com.WifiAudioDistribution.Db.ClientInfoDataSource;
import com.WifiAudioDistribution.Db.PodDataSource;
import com.WifiAudioDistribution.Networking.ClientInfo;
import com.WifiAudioDistribution.Nsd.NsdRunnable;
import com.WifiAudioDistribution.NsdAdapter;
import com.WifiAudioDistribution.Pod;
import com.WifiAudioDistribution.R;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends Activity {
    private final String TAG = "MYAPP:Activity";

    private final int ACTIVITY_MANAGE_CONNECTIONS = 1;
    private final int ACTIVITY_MANAGE_PODS = 2;
    private final int ACTIVITY_POD_VIEW = 3;

    public ClientManager mClientManager;

    public MainActivity mActivity;

    private List<Pod> mPods;

    public PodListAdapter mPodListAdapter;

    // This allows me to easily mock up objects instead of being tied to NsdServiceInfo objects
    public NsdAdapter mNsdAdapter;

    Handler mNsdAdapterHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(mNsdAdapter != null) {
                ClientInfo item = (ClientInfo) msg.obj;
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
                ClientInfo item = mNsdAdapter.getItem(i);
//                item.linkAvailable();
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

        setContentView(R.layout.main);

        // Lock to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mActivity = this;

        mClientManager = new ClientManager(this);
        mClientManager.initializeServerSocket();

        mPodListAdapter = new PodListAdapter(this, R.layout.pod_row);
        resetPodList();

        layoutInit();
    }

    public void layoutInit() {
        Button managePods = (Button) findViewById(R.id.manage_pods);
        Button manageConnections = (Button) findViewById(R.id.manage_connections);

        managePods.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                startManagePods();
            }
        });
        manageConnections.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                startManageConnections();
            }
        });

        ListView mPodList = (ListView) findViewById(R.id.pod_list);
        mPodList.setAdapter(mPodListAdapter);
        mPodList.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pod pod = mPodListAdapter.getItem(position);
                startPodView(pod);
            }
        });
    }

    public void startManagePods() {
        Intent intent = new Intent(this, ManagePodsActivity.class);
        startActivityForResult(intent, ACTIVITY_MANAGE_PODS);
    }

    public void startManageConnections() {
        Intent intent = new Intent(this, ManageConnectionsActivity.class);
        startActivityForResult(intent, ACTIVITY_MANAGE_CONNECTIONS);
    }

    public void startPodView(Pod pod) {
        Intent intent = new Intent(this, PodViewActivity.class);
        intent.putExtra(PodViewActivity.MESSAGE_POD_ID, pod.getId());
        startActivityForResult(intent, ACTIVITY_POD_VIEW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "OnActivityResult; RequestCode: " + requestCode);

        switch(requestCode) {
            case ACTIVITY_MANAGE_CONNECTIONS: {
                resetPodList();
                if(resultCode == RESULT_OK) { }
            }
            case ACTIVITY_MANAGE_PODS: {
                resetPodList();
                if(resultCode == RESULT_OK) { }
            }
            case ACTIVITY_POD_VIEW: {
                if(resultCode == RESULT_OK) { }
            }
        }
    }

    public void resetPodList() {
        mPodListAdapter.clear();

        setUpPods();
        Iterator<Pod> itr = mPods.iterator();
        while(itr.hasNext()) {
            mPodListAdapter.add(itr.next());
        }

        mPodListAdapter.notifyDataSetChanged();
    }

    public void setUpPods() {
        PodDataSource mPodDataSource = new PodDataSource(this);
        mPodDataSource.open();
        mPods = mPodDataSource.getAll();
        mPodDataSource.close();

        ClientInfoDataSource mClientInfoDataSource = new ClientInfoDataSource(this);
        mClientInfoDataSource.open();
        List<ClientInfo> connections = mClientInfoDataSource.getAll();
        mClientInfoDataSource.close();

        Iterator<ClientInfo> connItr;
        Iterator<Pod> podItr = mPods.iterator();
        while(podItr.hasNext()) {
            Pod pod = podItr.next();
            pod.clearConnections();

            boolean found = false;
            connItr = connections.iterator();
            while(!found && connItr.hasNext()) {
                ClientInfo connection = connItr.next();
                if(pod.getId() == connection.pod_id) {
                    pod.addConnection(connection);
                    found = true;
                }
            }
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "OnPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "OnResume");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if(mClientManager != null) {
            Log.d(TAG, "Client Manager Tear Down");
            mClientManager.tearDown();
        }

        super.onDestroy();
    }
}

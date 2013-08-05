package com.WifiAudioDistribution.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.WifiAudioDistribution.Db.ClientInfoDataSource;
import com.WifiAudioDistribution.Networking.ClientInfo;
import com.WifiAudioDistribution.NsdAdapter;
import com.WifiAudioDistribution.R;

import java.util.Iterator;
import java.util.List;

public class ManageConnectionsActivity extends Activity {
    private static final String TAG = "MYAPP:ManageConnectionsActivity";

    private static final int EDIT_CONNECTION_REQ = 1;
    private static final int ACTIVITY_ADD_CONNECTION = 2;

    private NsdAdapter mNsdAdapter;

//    private Handler mNsdAdapterHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            if(mNsdAdapter != null) {
//                ClientInfo item = (ClientInfo) msg.obj;
//                mNsdAdapter.add(item);
//                mNsdAdapter.notifyDataSetChanged();
//            }
//        }
//    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "OnCreate");

        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Lock to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.manage_connections);

        Button mAddService = (Button) findViewById(R.id.add_service);
        mAddService.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                addConnection();
            }
        });

        mNsdAdapter = new NsdAdapter(this, R.layout.connection_row);
        ListView mConnectionsList = (ListView) findViewById(R.id.connection_list);
        mConnectionsList.setAdapter(mNsdAdapter);

        mConnectionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ClientInfo item = (ClientInfo) mNsdAdapter.getItem(position);
                modifyConnection(item.id);
            }
        });

        refreshConnectionList();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "OnPause");

        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "OnResume");

        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "OnDestroy");

        super.onDestroy();
    }

    public void endActivity() {
        setResult(RESULT_OK);
        finish();
    }

//    public void resolvedClient(ClientInfo clientInfo) {
//        Message msg = new Message();
//        msg.obj = clientInfo;
//        mNsdAdapterHandler.sendMessage(msg);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addConnection() {
        Intent intent = new Intent(this, EditConnectionActivity.class);
        intent.putExtra(EditConnectionActivity.MESSAGE_CLIENT_ID, ClientInfo.NON_EXISTANT_ID);
        startActivityForResult(intent, ACTIVITY_ADD_CONNECTION);
    }

    public void modifyConnection(long id) {
        Intent intent = new Intent(this, EditConnectionActivity.class);
        intent.putExtra(EditConnectionActivity.MESSAGE_CLIENT_ID, id);
        startActivityForResult(intent, EDIT_CONNECTION_REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case EDIT_CONNECTION_REQ: {
                if(resultCode == RESULT_OK) {
                    // Refresh Adapter List
                    refreshConnectionList();
                    Log.d(TAG, "Modify Finished Successfully");
                }
            }
            case ACTIVITY_ADD_CONNECTION: {
                if(resultCode == RESULT_OK) {
                    refreshConnectionList();
                }
            }
        }
    }

    public void refreshConnectionList() {
        mNsdAdapter.clear();

        ClientInfoDataSource mDataSource = new ClientInfoDataSource(this);
        mDataSource.open();
        List<ClientInfo> clients = mDataSource.getAll();
        mDataSource.close();

        Iterator<ClientInfo> itr = clients.iterator();
        while(itr.hasNext()) {
            mNsdAdapter.add(itr.next());
        }
    }
}

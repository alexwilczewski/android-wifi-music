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
import com.WifiAudioDistribution.ClientInfo;
import com.WifiAudioDistribution.Db.ClientInfoDataSource;
import com.WifiAudioDistribution.Db.ClientInfoDbWrapper;
import com.WifiAudioDistribution.NsdAdapter;
import com.WifiAudioDistribution.R;

import java.util.Iterator;
import java.util.List;

public class ManageConnectionsActivity extends Activity {
    private static final String TAG = "MYAPP:ManageConnectionsActivity";

    private static final int EDIT_CONNECTION_REQ = 1;

    private NsdAdapter mNsdAdapter;
    private ListView mConnectionsList;

    private ClientInfoDataSource mDataSource;

    private Handler mNsdAdapterHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(mNsdAdapter != null) {
                ClientInfoDbWrapper clientInfo = (ClientInfoDbWrapper) msg.obj;
                mNsdAdapter.add(clientInfo);
                mNsdAdapter.notifyDataSetChanged();
            }
        }
    };

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
                // Insert into DB
                EditText hostname = (EditText) findViewById(R.id.hostname);
                EditText port = (EditText) findViewById(R.id.port);
                EditText servicename = (EditText) findViewById(R.id.servicename_edt);

                ClientInfoDbWrapper item = ClientInfoDbWrapper.getEmpty();
                item.host = hostname.getText().toString();
                item.port = Integer.parseInt(port.getText().toString());
                item.name = servicename.getText().toString();

                hostname.setText("");
                port.setText("");
                servicename.setText("");

                mDataSource.create(item);

                endActivity();
            }
        });

        mNsdAdapter = new NsdAdapter(this, R.layout.row);
        mConnectionsList = (ListView) findViewById(R.id.connections);
        mConnectionsList.setAdapter(mNsdAdapter);

        mConnectionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ClientInfoDbWrapper item = (ClientInfoDbWrapper) mConnectionsList.getItemAtPosition(position);
                modifyClient(item.id);
            }
        });


        mDataSource = new ClientInfoDataSource(this);
        mDataSource.open();

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
        mDataSource.close();

        super.onDestroy();
    }

    public void endActivity() {
        setResult(RESULT_OK);
        finish();
    }

    public void resolvedClient(ClientInfoDbWrapper clientInfo) {
        Message msg = new Message();
        msg.obj = clientInfo;
        mNsdAdapterHandler.sendMessage(msg);
    }

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

    public void modifyClient(long id) {
        Intent intent = new Intent(this, EditConnectionActivity.class);
        intent.putExtra(EditConnectionActivity.MESSAGE_CLIENT_ID, id);
        startActivityForResult(intent, EDIT_CONNECTION_REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == EDIT_CONNECTION_REQ) {
            if(resultCode == RESULT_OK) {
                // Refresh Adapter List
                refreshConnectionList();
                Log.d(TAG, "Modify Finished Successfully");
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
    }


}

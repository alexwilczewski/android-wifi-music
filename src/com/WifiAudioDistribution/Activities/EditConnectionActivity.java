package com.WifiAudioDistribution.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.WifiAudioDistribution.Db.ClientInfoDataSource;
import com.WifiAudioDistribution.Networking.ClientInfo;
import com.WifiAudioDistribution.R;

public class EditConnectionActivity extends Activity {
    private static final String TAG = "MYAPP:EditConnectionActivity";

    public static final String MESSAGE_CLIENT_ID = "com.WifiAudioDistribution.CLIENT_ID";

    private ClientInfoDataSource mDataSource;
    private ClientInfo mEditClientInfo;
    private boolean mNewConnection;

    private EditText hostnameInput;
    private EditText portInput;
    private EditText servicenameInput;
    private EditText podIdInput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "OnCreate");

        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Lock to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.edit_connection);

        hostnameInput = (EditText) findViewById(R.id.hostname_input);
        portInput = (EditText) findViewById(R.id.port_input);
        servicenameInput = (EditText) findViewById(R.id.servicename_input);
        podIdInput = (EditText) findViewById(R.id.pod_id_input);

        Intent startingIntent = getIntent();
        long id = startingIntent.getLongExtra(EditConnectionActivity.MESSAGE_CLIENT_ID, 0);

        mDataSource = new ClientInfoDataSource(this);
        mDataSource.open();

        if(id == ClientInfo.NON_EXISTANT_ID) {
            mNewConnection = true;
            Log.d(TAG, "Adding new Connection");
            mEditClientInfo = ClientInfo.getEmpty();
        } else {
            mNewConnection = false;
            mEditClientInfo = mDataSource.find(id);
            Log.d(TAG, "Editing Connection: id("+mEditClientInfo.id+")");

            hostnameInput.setText(mEditClientInfo.host);
            portInput.setText(""+mEditClientInfo.port);
            servicenameInput.setText(mEditClientInfo.name);
            podIdInput.setText(""+mEditClientInfo.pod_id);
        }

        Button mSaveService = (Button) findViewById(R.id.save_service);
        mSaveService.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Modify in DB
                mEditClientInfo.host = hostnameInput.getText().toString();
                mEditClientInfo.port = Integer.parseInt(portInput.getText().toString());
                mEditClientInfo.name = servicenameInput.getText().toString();
                mEditClientInfo.pod_id = Integer.parseInt(podIdInput.getText().toString());

                mDataSource.save(mEditClientInfo);

                endActivity();
            }
        });

        Button mDeleteService = (Button) findViewById(R.id.delete_service);
        mDeleteService.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Delete from DB
                mDataSource.delete(mEditClientInfo.id);

                endActivity();
            }
        });
        if(mNewConnection) {
            mDeleteService.setEnabled(false);
        }
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

    private void endActivity() {
        setResult(RESULT_OK);
        finish();
    }
}

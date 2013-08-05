package com.WifiAudioDistribution.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import com.WifiAudioDistribution.Adapter.PodListAdapter;
import com.WifiAudioDistribution.Db.ClientInfoDataSource;
import com.WifiAudioDistribution.Db.PodDataSource;
import com.WifiAudioDistribution.Networking.ClientInfo;
import com.WifiAudioDistribution.NsdAdapter;
import com.WifiAudioDistribution.Pod;
import com.WifiAudioDistribution.R;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class PodViewActivity extends Activity {
    private final String TAG = "MYAPP:PodViewActivity";

    private final int ACTIVITY_CHOOSE_SONG = 1;

    public static final String MESSAGE_POD_ID = "com.WifiAudioDistribution.POD_ID";

    private Pod mPod;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "OnCreate");

        setContentView(R.layout.pod);

        Intent startingIntent = getIntent();
        long id = startingIntent.getLongExtra(MESSAGE_POD_ID, 0);

        PodDataSource podDataSource = new PodDataSource(this);
        podDataSource.open();
        mPod = podDataSource.find(id);
        podDataSource.close();

        setTitle(mPod.getName());

        // Add connections to Pod
        ClientInfoDataSource clientInfoDataSource = new ClientInfoDataSource(this);
        clientInfoDataSource.open();
        List<ClientInfo> connections = clientInfoDataSource.findByPodId(mPod.getId());
        clientInfoDataSource.close();

        Iterator<ClientInfo> itr = connections.iterator();
        while(itr.hasNext()) {
            mPod.addConnection(itr.next());
        }

        // Lock to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Button mSelectSong = (Button) findViewById(R.id.select_song);
        mSelectSong.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                sendMusic();
            }
        });

        Button mStopPlayback = (Button) findViewById(R.id.stop_playback);
        mStopPlayback.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                stopPlayback();
            }
        });

        ListView mConnectionList = (ListView) findViewById(R.id.pod_connection_list);
        NsdAdapter mConnectionListAdapter = new NsdAdapter(this, R.layout.connection_row);
        mConnectionList.setAdapter(mConnectionListAdapter);

        List<ClientInfo> mConnections = mPod.getConnections();
        itr = mConnections.iterator();
        while(itr.hasNext()) {
            mConnectionListAdapter.add(itr.next());
        }
        mConnectionListAdapter.notifyDataSetChanged();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "OnActivityResult; RequestCode: " + requestCode);

        switch(requestCode) {
            case ACTIVITY_CHOOSE_SONG: {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String filePath = uri.getPath();
                    Log.i(TAG, "PATH: "+filePath);

                    mPod.setFile(new File(filePath));

                    mPod.start();
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
        Log.d(TAG, "OnDestroy");
        super.onDestroy();
    }

    public void sendMusic() {
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("file/*");
        intent = Intent.createChooser(chooseFile, "Choose a song");
        startActivityForResult(intent, ACTIVITY_CHOOSE_SONG);
    }

    public void stopPlayback() {
        mPod.stopPlayback();
    }
}

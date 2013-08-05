package com.WifiAudioDistribution.Activities;

import android.app.Activity;
import android.app.DialogFragment;
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
import com.WifiAudioDistribution.Adapter.PodListAdapter;
import com.WifiAudioDistribution.Db.ClientInfoDataSource;
import com.WifiAudioDistribution.Db.PodDataSource;
import com.WifiAudioDistribution.Fragments.EditPodDialogFragment;
import com.WifiAudioDistribution.Networking.ClientInfo;
import com.WifiAudioDistribution.NsdAdapter;
import com.WifiAudioDistribution.Pod;
import com.WifiAudioDistribution.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ManagePodsActivity extends Activity implements EditPodDialogFragment.EditPodDialogListener {
    private static final String TAG = "MYAPP:ManagePodsActivity";

    private PodListAdapter mPodListAdapter;
    private boolean mOnCreate;
    private Pod mEditingPod;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "OnCreate");

        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Lock to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.manage_pods);

        Button mAddPod = (Button) findViewById(R.id.add_pod);
        mAddPod.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add new pod to DB based on Named entered
                // @TODO: What way to type in text?
                addPod();
            }
        });

        mPodListAdapter = new PodListAdapter(this, R.layout.pod_row);
        resetPodList();

        ListView mPodList = (ListView) findViewById(R.id.manage_pod_list);
        mPodList.setAdapter(mPodListAdapter);
        mPodList.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pod pod = mPodListAdapter.getItem(position);
                modifyPod(pod);
            }
        });
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

    }

    public void resetPodList() {
        mPodListAdapter.clear();

        List<Pod> mPods = setUpPods();
        Iterator<Pod> itr = mPods.iterator();
        while(itr.hasNext()) {
            mPodListAdapter.add(itr.next());
        }

        mPodListAdapter.notifyDataSetChanged();
    }

    public List<Pod> setUpPods() {
        PodDataSource mPodDataSource = new PodDataSource(this);
        mPodDataSource.open();
        List<Pod> mPods = mPodDataSource.getAll();
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

        return mPods;
    }

    public void endActivity() {
        setResult(RESULT_OK);
        finish();
    }

    public void addPod() {
        mOnCreate = true;

        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new EditPodDialogFragment();
        dialog.show(this.getFragmentManager(), "EditPodDialogFragment");
    }

    public void modifyPod(Pod pod) {
        mOnCreate = false;
        mEditingPod = pod;

        EditPodDialogFragment dialog = new EditPodDialogFragment();
        dialog.show(this.getFragmentManager(), "EditPodDialogFragment");
        dialog.setDefaultPodName(pod.getName());
    }

    // @TODO: Don't use mOnCreate. Change to listeners added in addPod() and modifyPod() functions
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String podName) {
        // Open Datasource
        PodDataSource mPodDataSource = new PodDataSource(this);
        mPodDataSource.open();
        if(mOnCreate) {
            Pod pod = Pod.getNew();
            pod.setName(podName);
            mPodDataSource.save(pod);
        } else if(mEditingPod != null) {
            mEditingPod.setName(podName);
            mPodDataSource.save(mEditingPod);
        }
        mPodDataSource.close();

        resetPodList();

        endDialog();
    }
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();

        endDialog();
    }

    public void endDialog() {
        mOnCreate = false;
        mEditingPod = null;
    }
}


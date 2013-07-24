package com.WifiAudioDistribution;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NsdAdapter extends ArrayAdapter<ClientInfo> {
    private final String TAG = "MYAPP:NsdAdapter";

    private MyActivity mActivity;
    private int viewId;

    public NsdAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mActivity = (MyActivity) context;
        viewId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(viewId, null);
        }

        ClientInfo clientInfo = getItem(position);
        Log.d(TAG, "Service Added: " + clientInfo.host);

        TextView hostnameText = (TextView) v.findViewById(R.id.hostname);
        TextView servicenameText = (TextView) v.findViewById(R.id.servicename);
        TextView portText = (TextView) v.findViewById(R.id.port);

        hostnameText.setText(clientInfo.host);
        servicenameText.setText(clientInfo.name);
        portText.setText(""+clientInfo.port);

        return v;
    }
}

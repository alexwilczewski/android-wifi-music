package com.WifiAudioDistribution;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.WifiAudioDistribution.Networking.ClientInfo;

public class NsdAdapter extends ArrayAdapter<ClientInfo> {
    private final String TAG = "MYAPP:NsdAdapter";

    private Context mActivity;
    private int viewId;

    public NsdAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mActivity = context;
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

        TextView hostnamePortText = (TextView) v.findViewById(R.id.hostname_port);
        TextView servicenameText = (TextView) v.findViewById(R.id.row_servicename);
        TextView availableText = (TextView) v.findViewById(R.id.available);

        hostnamePortText.setText(clientInfo.host+":"+clientInfo.port);
        servicenameText.setText(clientInfo.name);
        availableText.setText(clientInfo.available ? "Y" : "N");

        if(clientInfo.available) {
            hostnamePortText.setTextColor(Color.WHITE);
            servicenameText.setTextColor(Color.WHITE);
            availableText.setTextColor(Color.WHITE);
        } else {
            hostnamePortText.setTextColor(Color.GRAY);
            servicenameText.setTextColor(Color.GRAY);
            availableText.setTextColor(Color.GRAY);
        }

        return v;
    }
}

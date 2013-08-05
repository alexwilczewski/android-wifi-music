package com.WifiAudioDistribution.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.WifiAudioDistribution.Networking.ClientInfo;
import com.WifiAudioDistribution.Pod;
import com.WifiAudioDistribution.R;

public class PodListAdapter extends ArrayAdapter<Pod> {
    private final String TAG = "MYAPP:PodListAdapter";

    private Context mContext;
    private int mRowResourceId;

    public PodListAdapter(Context context, int rowResourceId) {
        super(context, rowResourceId);

        mContext = context;
        mRowResourceId = rowResourceId;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(mRowResourceId, null);
        }

        Pod item = getItem(position);

        TextView podName = (TextView) v.findViewById(R.id.pod_row_name);
        TextView deviceCount = (TextView) v.findViewById(R.id.pod_row_device_count);
        TextView podId = (TextView) v.findViewById(R.id.pod_row_id);

        podName.setText(item.getName());
        deviceCount.setText(""+item.getConnectionCount());
        podId.setText("ID("+item.getId()+")");

        return v;
    }
}

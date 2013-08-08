package com.WifiAudioDistribution;

import com.WifiAudioDistribution.Networking.ClientInfo;
import com.WifiAudioDistribution.Networking.SendingClientRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pod {
    private final String TAG = "MYAPP:Pod";

    private long id;
    private String name;

    private boolean isNew;
    private List<ClientInfo> mConnections;

    static public Pod getNew() {
        return new Pod();
    }

    private Pod() {
        isNew = true;
        init();
    }

    public Pod(long id, String name) {
        setId(id);
        this.name = name;
        init();
    }

    private void init() {
        clearConnections();
    }

    public void setId(long id) {
        isNew = false;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getConnectionCount() {
        return mConnections.size();
    }

    public List<ClientInfo> getConnections() {
        return mConnections;
    }

    public boolean isNew() {
        return isNew;
    }

    public void addConnection(ClientInfo connection) {
        mConnections.add(connection);
    }

    public void clearConnections() {
        mConnections = new ArrayList<ClientInfo>();
    }
}

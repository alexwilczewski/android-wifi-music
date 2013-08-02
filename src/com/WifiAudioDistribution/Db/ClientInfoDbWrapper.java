package com.WifiAudioDistribution.Db;

import com.WifiAudioDistribution.ClientInfo;

public class ClientInfoDbWrapper extends ClientInfo {
    public static final int NON_EXISTANT_ID = -1;

    public long id;

    public static ClientInfoDbWrapper getEmpty() {
        ClientInfoDbWrapper item = new ClientInfoDbWrapper();
        item.id = NON_EXISTANT_ID;
        return item;
    }

    public static boolean isEmpty(ClientInfoDbWrapper item) {
        return (item.id == NON_EXISTANT_ID);
    }
}

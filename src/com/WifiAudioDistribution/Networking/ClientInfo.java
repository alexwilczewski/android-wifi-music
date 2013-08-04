package com.WifiAudioDistribution.Networking;

import java.io.IOException;
import java.net.Socket;

public class ClientInfo {
    public static final int NON_EXISTANT_ID = -1;

    public long id;
    public String host;
    public int port;
    public String name;
    public boolean available;

    public void linkAvailable() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Socket s = new Socket(host, port);
                    s.close();
                    available = true;
                } catch(IOException e) {
                    available = false;
                }
            }
        }).start();
    }

    public static ClientInfo getEmpty() {
        ClientInfo item = new ClientInfo();
        item.id = NON_EXISTANT_ID;
        return item;
    }

    public static boolean isEmpty(ClientInfo item) {
        return (item.id == NON_EXISTANT_ID);
    }
}

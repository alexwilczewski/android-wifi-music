package com.WifiAudioDistribution;

import java.io.IOException;
import java.net.Socket;

public class ClientInfo {
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
}

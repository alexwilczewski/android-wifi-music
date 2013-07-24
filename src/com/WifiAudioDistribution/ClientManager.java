package com.WifiAudioDistribution;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.util.Log;
import com.WifiAudioDistribution.Networking.ReadingClientRunnable;
import com.WifiAudioDistribution.Networking.SendingClientRunnable;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class ClientManager {
    private static final String TAG = "MYAPP:ClientManager";
    public static final int PORT = 1234;

    public static String FILE = "/storage/emulated/0/Music/C2C - Down The Road.mp3";

    public static final int SENDING_FILE   = 1;
    public static final int BUFFER_READY   = 2;
    public static final int START_PLAYBACK = 3;
    public static final int CONTINUE = 4;
    public static final int UNKNOWN_MESSAGE = 5;

    public static final int KBYTE = 1024;
    public static final int BUFFER_READY_SIZE = 50*KBYTE;

    public ServerSocket mServerSocket;
    public int mLocalPort;

    public HashMap<String, ClientInfo> mFoundServices;

    public Thread mListeningThread;
    public Thread mSendingThread;

    public MyActivity mActivity;

    public ClientManager(MyActivity context) {
        mActivity = context;
    }

    public NsdManager getNsdManager() {
        return (NsdManager) mActivity.getSystemService(Context.NSD_SERVICE);
    }

    public void initializeServerSocket() {
        mFoundServices = new HashMap<String, ClientInfo>();
        try {
            // Initialize a server socket on the next available port.
            mServerSocket = new ServerSocket(PORT);

            // Store the chosen port.
            mLocalPort =  mServerSocket.getLocalPort();
            Log.d(TAG, "Server Port: " + mLocalPort);
            Log.d(TAG, "Is Bound: " + (mServerSocket.isBound() ? "Yes" : "no"));
        } catch(IOException e) {
            e.printStackTrace();
        }

        if(mServerSocket.isBound()) {
            initializeListeningThread();
        }
    }

    public void initializeListeningThread() {
        // Create thread for ServerSocket to listen on
        mListeningThread = new Thread(new ListeningRunnable());
        mListeningThread.start();
    }

    public void initializeSendingThread() {
        // Create thread for ServerSocket to send on
        mSendingThread = new Thread(new SendingClientRunnable(this));
        mSendingThread.start();
    }

    public void serviceResolved(ClientInfo clientInfo) {
        mFoundServices.put(clientInfo.host, clientInfo);
    }

    public Collection<ClientInfo> getServices() {
        return mFoundServices.values();
    }

    public void tearDown() {
        Log.d(TAG, "Tear Down");
        mListeningThread.interrupt();

        try {
            if(mServerSocket.isBound()) {
                mServerSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error when closing server socket.");
        }
    }

    public boolean isBound() {
        return mServerSocket.isBound();
    }

    public int getPort() {
        return mLocalPort;
    }

    // Thread for ServerSocket to listen on
    class ListeningRunnable implements Runnable {
        private ArrayList<Thread> mThreads;

        public ListeningRunnable() {
            mThreads = new ArrayList<Thread>();
        }

        public void run() {
            boolean listening = true;
            while(listening) {
                try {
                    // Accept is blocking. So it sits and waits.
                    Socket s = mServerSocket.accept();
                    Thread t = new Thread(new ReadingClientRunnable(mActivity, s));
                    t.start();
                    mThreads.add(t);
                } catch(IOException e) {
                    listening = false;
                    // No connection made to server
                    e.printStackTrace();
                }

                if(Thread.currentThread().isInterrupted()) {
                    listening = false;
                }
            }

            Log.d(TAG, "Listening interuptted");
            tearDown();
        }

        public void tearDown() {
            Log.d(TAG, "Listening tear down");
            Log.d(TAG, "mThread Size: "+mThreads.size());

            Iterator<Thread> itr = mThreads.iterator();
            while(itr.hasNext()) {
                itr.next().interrupt();
            }
            Log.d(TAG, "Listening end tear down");
        }
    }
}

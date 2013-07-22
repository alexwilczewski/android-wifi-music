package com.WifiAudioDistribution;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class MyActivity extends Activity {
    private final String TAG = "MYAPP:Activity";

    public static String FILE = "/storage/emulated/0/Music/C2C - Down The Road.mp3";

    public NsdStreamServer server;
    public NsdStreamClient client;

    public ClientManager mClientManager;

    public Thread mDiscoveryThread;

    public boolean mSetUpSockets;

    public MyActivity mActivity;

    // @TODO: Change NsdAdapter to something more specific (public .host, .port, .name)
    // This allows me to easily mock up objects instead of being tied to NsdServiceInfo objects
    public NsdAdapter mNsdAdapter;

    Handler mNsdAdapterHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(mNsdAdapter != null) {
                ClientInfo clientInfo = (ClientInfo) msg.obj;
                mNsdAdapter.add(clientInfo);
                mNsdAdapter.notifyDataSetChanged();
            }
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // @TODO: Init, for mock devices (itself)

        // Lock to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mActivity = this;

        Button mMusicBtn = (Button) findViewById(R.id.music);
        Button mDiscoverBtn = (Button) findViewById(R.id.discover);

        mMusicBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mClientManager.initializeSendingThread(mActivity);
            }
        });

        mDiscoverBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                discoveryThread(10000);
            }
        });

        ListView mConnectionsList = (ListView) findViewById(R.id.connections);
        mNsdAdapter = new NsdAdapter(this, R.layout.row);
        mConnectionsList.setAdapter(mNsdAdapter);

        mClientManager = new ClientManager(this);
        mClientManager.initializeServerSocket();

        init();
    }

    public void init() {
        // Add static clientinfo
        ClientInfo staticInfo = new ClientInfo();
        staticInfo.host = "192.168.0.11";
        staticInfo.port = ClientManager.PORT;
        staticInfo.name = "Static Client";

        resolvedClient(staticInfo);
    }

    // Used to find services over a thread and timed
    public void discoveryThread(int msRunning) {
        mDiscoveryThread = new Thread(new NsdRunnable(msRunning));
        mDiscoveryThread.start();
    }

    public void resolvedClient(ClientInfo clientInfo) {
        mClientManager.serviceResolved(clientInfo);

        Message msg = new Message();
        msg.obj = clientInfo;
        mNsdAdapterHandler.sendMessage(msg);
    }

    class NsdRunnable implements Runnable {
        // Length of time to run discovery
        private int msRunning;

        public NsdRunnable(int msRunning) {
            this.msRunning = msRunning;
        }

        public void run() {
            Log.d(TAG, "Start Discovery, create NsdHelper.");

            if(!mClientManager.isBound()) {
                Log.d(TAG, "Server Socket is not bound.");
                return;
            }

            NsdHelper mNsdHelper = new NsdHelper(mActivity);
            mNsdHelper.registerService(mClientManager.getPort());
            mNsdHelper.setOnResolvedService(new NsdHelper.OnResolvedServiceListener() {
                public void onResolve(NsdServiceInfo serviceInfo) {
                    ClientInfo clientInfo = new ClientInfo();
                    clientInfo.host = serviceInfo.getHost().getHostAddress();
                    clientInfo.name = serviceInfo.getServiceName();
                    clientInfo.port = serviceInfo.getPort();

                    resolvedClient(clientInfo);
                }
            });

            long startTime = System.currentTimeMillis();

            while(true) {
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException e) {
                    Log.d(TAG, "Interrupted thread.");
                    break;
                }

                if((System.currentTimeMillis() - startTime) > msRunning) {
                    break;
                }
            }

            Log.d(TAG, "Stop Discovery and tear down NsdHelper");

            mNsdHelper.tearDown();
        }
    }

    public void sendThread() {
        Thread r = new Thread(new SendRunnable());
        r.start();
    }

    class SendRunnable implements Runnable {
        public void run() {
            // Distribute music over socket
            // Send buffered bytes

            if(!mSetUpSockets) {
                NetworkClient.setUpSockets();

                mSetUpSockets = true;
            }

            Collection<Socket> sockets = NetworkClient.mSocketMap.values();
            Iterator<Socket> itr = sockets.iterator();

            File streamingFile = new File(FILE);

            while(itr.hasNext()) {
                Log.d(TAG, "mFriend isn't null. Send some bytes");

                Socket s = itr.next();

                ByteArrayOutputStream ous = new ByteArrayOutputStream();
                try {
                    byte[] buffer = new byte[4096];
                    ous = new ByteArrayOutputStream();
                    InputStream ios = new FileInputStream(streamingFile);

                    int read = 0;
                    int cnt = 0;
                    int stop = 50;
                    while ((read = ios.read(buffer)) != -1 && cnt < stop) {
                        ous.write(buffer, 0, read);

                        cnt++;
                    }
                    ios.close();
                } catch(FileNotFoundException e) {
                    Log.e(TAG, "File not found", e);
                } catch(IOException e) {
                    Log.e(TAG, "IOException", e);
                }


                try {
//                    PrintWriter out = new PrintWriter(
//                            new BufferedWriter(
//                                    new OutputStreamWriter(s.getOutputStream())), true);
                    OutputStream out = s.getOutputStream();
                    out.write(ous.toByteArray());
                    out.flush();
                    out.close();
                } catch(IOException e) {
                    Log.d(TAG, "I/O Exception", e);
                }
            }
        }
    }

    public void listenThread() {
        Thread r = new Thread(new TestRunnable());
        r.start();
    }

    class TestRunnable implements Runnable {
        public void run() {
            BufferedReader input;
            ByteArrayOutputStream ous;
            try {
                StreamingMediaPlayer smp = new StreamingMediaPlayer(mActivity);

                ous = new ByteArrayOutputStream();
//                input = new BufferedReader(new InputStreamReader(
//                        mNsdHelper.mServerSocket.accept().getInputStream()));
                byte[] buffer = new byte[4096];
                InputStream is = mClientManager.mServerSocket.accept().getInputStream();

                while (!Thread.currentThread().isInterrupted()) {
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        ous.write(buffer, 0, read);
                    }


                    try {
                        smp.storeAudioIncrement(ous.toByteArray(), ous.size());
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    ous.close();

//                    String messageStr = null;
//                    messageStr = input.readLine();
//                    if (messageStr != null) {
//                        Log.d(TAG, "Read from the stream: " + messageStr);
//                    } else {
//                        Log.d(TAG, "The nulls! The nulls!");
//                        break;
//                    }
                }
                is.close();

            } catch (IOException e) {
                Log.e(TAG, "Server loop error: ", e);
            }
        }
    }

    @Override
    protected void onPause() {
        if(mDiscoveryThread != null) {
            mDiscoveryThread.interrupt();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if(mDiscoveryThread != null) {
            mDiscoveryThread.interrupt();
        }
        if(mClientManager != null) {
            mClientManager.tearDown();
        }

        super.onDestroy();
    }

    // @TODO: Is extending an ArrayAdapter the best solution?
    private class NsdAdapter extends ArrayAdapter<ClientInfo> {
        private int viewId;

        public NsdAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            viewId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(viewId, null);
            }

            ClientInfo clientInfo = getItem(position);
            Log.d(TAG, "Service Added: "+clientInfo.host);

            TextView hostnameText = (TextView) v.findViewById(R.id.hostname);
            TextView servicenameText = (TextView) v.findViewById(R.id.servicename);
            TextView portText = (TextView) v.findViewById(R.id.port);

            hostnameText.setText(clientInfo.host);
            servicenameText.setText(clientInfo.name);
            portText.setText(""+clientInfo.port);

            return v;
        }
    }
}

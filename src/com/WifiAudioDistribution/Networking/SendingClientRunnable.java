package com.WifiAudioDistribution.Networking;

import android.util.Log;
import com.WifiAudioDistribution.ClientManager;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class SendingClientRunnable implements Runnable {
    private static final String TAG = "MYAPP:SendingClientRunnable";

    static public Map<Integer, SendingClientRunnable> runnables = new HashMap<Integer, SendingClientRunnable>();

    private List<ClientInfo> tConnections;
    private ArrayList<Socket> tSockets;
    private File tPlaybackFile;
    private boolean tSocketsOpened;
    private LinkedBlockingQueue<Integer> tMessages;

    public SendingClientRunnable(List<ClientInfo> connections) {
        tConnections = connections;
        tSocketsOpened = false;
        tMessages = new LinkedBlockingQueue<Integer>();
    }

    public void setFile(File playbackFile) {
        tPlaybackFile = playbackFile;
    }

    public void stopPlayback() {
        tMessages.add(new Integer(ClientManager.STOP_PLAYBACK));
    }

    public void initializeSockets() {
        // Get list of available Clients and bind sockets
        Iterator<ClientInfo> itrNsd = tConnections.iterator();
        while(itrNsd.hasNext()) {
            ClientInfo clientInfo = itrNsd.next();
            String host = clientInfo.host;
            int port = clientInfo.port;

            try {
                tSockets.add(new Socket(host, port));
            } catch(IOException e) {
                Log.e(TAG, "Connection failed. [" + host + ":" + port + "]");
            }
        }
        tSocketsOpened = true;
    }

    public void closeSockets() {
        tSocketsOpened = false;
        // Attempt to close sockets in case server didn't close them for us
        Iterator<Socket> itr = tSockets.iterator();
        while(itr.hasNext()) {
            try {
                Socket s = itr.next();
                if(s.isConnected() && !s.isClosed()) {
                    s.shutdownOutput();
                    s.close();
                }
            } catch(IOException e) {
                Log.e(TAG, "Could not close socket.", e);
            }
        }
    }

    public void run() {
        tSockets = new ArrayList<Socket>();
        initializeSockets();

        // Get data to send to sockets
        byte[] buffer = new byte[4096];
        long latency[] = new long[2];
        long largestLatency = 0;
        try {
            FileInputStream ios = new FileInputStream(tPlaybackFile);

            // Notify of sending file
            Iterator<Socket> itr = tSockets.iterator();
            int itrcnt = 0;
            while(itr.hasNext()) {
                Socket s = itr.next();
                try {
                    OutputStream os = s.getOutputStream();
                    InputStream is = s.getInputStream();

                    long curr = System.currentTimeMillis();
                    os.write(ClientManager.SENDING_FILE);
                    os.flush();

                    int b = is.read();
                    if(b == ClientManager.CONTINUE) {
                        // K, we good.
                        latency[itrcnt] = (System.currentTimeMillis() - curr)/2;
                        Log.d(TAG, "Latency Itr("+itrcnt+"): "+latency[itrcnt]);

                        if(largestLatency < latency[itrcnt]) {
                            largestLatency = latency[itrcnt];
                        }
                    }
                } catch(IOException e) {
                    Log.e(TAG, "Could not create socket OutputStream.", e);
                }
                itrcnt++;
            }





            int read;
            boolean reading = true;
            boolean waiting = false;



            int total = 2;

            boolean[] readingList = new boolean[total];
            readingList[0] = readingList[1] = true;
            boolean[] waitingList = new boolean[total];
            waitingList[0] = waitingList[1] = false;

            boolean flagSendStart = false;

            abstract class TimerTask2 extends TimerTask {
                public Socket s;
                public TimerTask2(Socket s) {
                    super();
                    this.s = s;
                }
            }

            int itrCnt = 0;
            Timer[] timers = new Timer[2];
            TimerTask2[] timertasks = new TimerTask2[2];
            while(reading) {
                if(flagSendStart) {
                    itr = tSockets.iterator();

                    itrCnt = 0;
                    while(itr.hasNext()) {
                        long lat = latency[itrCnt];
                        Socket s = itr.next();

                        Log.d(TAG, "Starting up a timer for socket #: "+itrCnt);
                        timers[itrCnt] = new Timer();
                        timertasks[itrCnt] = new TimerTask2(s) {
                            @Override
                            public void run() {
                                try {
                                    OutputStream os = this.s.getOutputStream();
                                    InputStream is = this.s.getInputStream();

                                    do {
                                        Log.d(TAG, "Send Start Playback");
                                        os.write(ClientManager.START_PLAYBACK);
                                        os.flush();
                                    } while((is.read()) != ClientManager.CONTINUE);
                                } catch(IOException e) {
                                    Log.e(TAG, "IOException", e);
                                }
                            }
                        };
                        timers[itrCnt].schedule(timertasks[itrCnt], largestLatency-lat+50);

                        itrCnt++;
                    }

                    try {
                        Thread.sleep(largestLatency+200);
                    } catch(InterruptedException e) {
                        Log.e(TAG, "InterruptedException", e);
                    }
                    flagSendStart = false;
                } else {
                    read = ios.read(buffer);

                    if(read == -1) {
                        reading = false;
                        break;
                    }

                    itr = tSockets.iterator();

                    itrCnt = 0;
                    while(itr.hasNext()) {
                        Socket s = itr.next();
                        OutputStream os = s.getOutputStream();
                        InputStream is = s.getInputStream();

                        os.write(buffer);
                        os.flush();

                        int b = is.read();
                        if(b == ClientManager.BUFFER_READY) {
//                            flagSendStart = true;
                        }

                        itrCnt++;
                    }
                }
            }

            ios.close();
        } catch(FileNotFoundException e) {
            Log.e(TAG, "File not found", e);
        } catch(IOException e) {
            Log.e(TAG, "IOException", e);
        }

        try {
            Thread.sleep(500);

            Log.d(TAG, "File read. Sending start playback.");
            sendMessage(ClientManager.START_PLAYBACK);
        } catch(InterruptedException e) { }

        while(!Thread.currentThread().isInterrupted()) {
            // Check message queue. If there is anything, send message
            if(tMessages.size() > 0) {
                Integer message = tMessages.poll();
                sendMessage(message.intValue());
            }

            try {
                Thread.sleep(500);
            } catch(InterruptedException e) { }
        }

        closeSockets();
    }

    public void sendMessage(int message) {
        Iterator<Socket> itr = tSockets.iterator();
        while(itr.hasNext()) {
            try {
                Socket s = itr.next();
                OutputStream os = s.getOutputStream();
                InputStream is = s.getInputStream();

                int read;
                do {
                    Log.d(TAG, "Send Message: "+message);
                    os.write(message);
                    os.flush();

                    read = is.read();
                    Log.d(TAG, "Pong Message: "+read);
                } while((read) == ClientManager.UNKNOWN_MESSAGE);
            } catch(IOException e) {
                Log.e(TAG, "Could not send message.", e);
            }
        }
    }
}

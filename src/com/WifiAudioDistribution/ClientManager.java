package com.WifiAudioDistribution;

import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class ClientManager {
    private static final String TAG = "MYAPP:ClientManager";
    public static final int PORT = 1234;

    public final int SENDING_FILE   = 1;
    public final int BUFFER_READY   = 2;
    public final int START_PLAYBACK = 3;
    public final int CONTINUE = 4;

    public final int KBYTE = 1024;
    public final int BUFFER_READY_SIZE = 30*KBYTE;

    public ServerSocket mServerSocket;
    public int mLocalPort;

    public ArrayList<ClientInfo> mFoundServices;

    public Thread mListeningThread;
    public Thread mSendingThread;

    public MyActivity mActivity;

    public ClientManager(MyActivity context) {
        mActivity = context;
    }

    public void initializeServerSocket() {
        mFoundServices = new ArrayList<ClientInfo>();
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

    public void initializeSendingThread(MyActivity activity) {
        // Create thread for ServerSocket to send on
        mSendingThread = new Thread(new SendingClientRunnable(activity));
        mSendingThread.start();
    }

    public void serviceResolved(ClientInfo clientInfo) {
        // @TODO: Check for dup services
        mFoundServices.add(clientInfo);
    }

    public ArrayList<ClientInfo> getServices() {
        return mFoundServices;
    }

    public void tearDown() {
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
            while(!Thread.currentThread().interrupted()) {
                try {
                    // Accept is blocking. So it sits and waits.
                    Socket s = mServerSocket.accept();
                    Thread t = new Thread(new ReadingClientRunnable(s));
                    t.start();
                    mThreads.add(t);
                } catch(IOException e) {
                    // No connection made to server
                    e.printStackTrace();
                }
            }

            tearDown();
        }

        public void tearDown() {
            Iterator<Thread> itr = mThreads.iterator();
            while(itr.hasNext()) {
                itr.next().interrupt();
            }
        }
    }

    // Thread for Data to be read-in by Socket
    class ReadingClientRunnable implements Runnable {
        Socket mSocket;

        public ReadingClientRunnable(Socket socket) {
            mSocket = socket;
        }

        public void run() {
            byte[] buffer = new byte[4096];
            int read = 0;
            InputStream in;
            OutputStream out;
            ByteArrayOutputStream ous = new ByteArrayOutputStream();
            try {
                in = mSocket.getInputStream();
                out = mSocket.getOutputStream();

                StreamingMediaPlayer smp = new StreamingMediaPlayer(mActivity);

                boolean readingInFile = false;
                boolean bufferReadySent = false;
                while((read = in.read(buffer)) != -1) {
                    if(read == 1) {
                        // We've got a message...
                        // Buffer could receive 1 byte without a message
                        // @TODO: Figure out best way to send messages within data transfer
                        int message = buffer[0];
                        if(message == SENDING_FILE) {
                            Log.d(TAG, "Begin Reading in the file");
                            readingInFile = true;
                        } else if(message == START_PLAYBACK) {
                            try {
                                smp.storeAudioIncrement(ous.toByteArray(), ous.size());
                            } catch(IOException e) {
                                Log.e(TAG, "Could not store buffer to play.", e);
                            }
                        }
                    } else if(readingInFile) {
                        ous.write(buffer, 0, read);

                        if(!bufferReadySent && ous.size() >= BUFFER_READY_SIZE) {
                            Log.d(TAG, "Send Buffer Ready");
                            out.write(BUFFER_READY);
                            bufferReadySent = true;
                        }
                    }
                    Log.d(TAG, "Read: " + read);

                    out.write(CONTINUE);
                }
                Log.d(TAG, "Read: "+read);
                in.close();

                Log.d(TAG, "Total: "+ous.size());
            } catch(IOException e) {
                Log.e(TAG, "Could not read in from socket.", e);
            }

            try {
                mSocket.close();
            } catch(IOException e) {
                Log.e(TAG, "Could not close socket.", e);
            }
        }
    }

    class SendingClientRunnable implements Runnable {
        private MyActivity tActivity;
        private ArrayList<Socket> tSockets;

        public SendingClientRunnable(MyActivity context) {
            tActivity = context;
        }

        public void initializeSockets() {
            ArrayList<ClientInfo> services = getServices();

            // Get list of available Clients and bind sockets
            Iterator<ClientInfo> itrNsd = services.iterator();
            while(itrNsd.hasNext()) {
                ClientInfo clientInfo = itrNsd.next();
                String host = clientInfo.host;
                int port = clientInfo.port;

                try {
                    tSockets.add(new Socket(host, port));
                } catch(IOException e) {
                    Log.e(TAG, "Connection failed. ["+host+":"+port+"]", e);
                }
            }
        }

//        public void sendToSockets(byte[] data) {
//            Iterator<Socket> itr = tSockets.iterator();
//            while(itr.hasNext()) {
//                Socket s = itr.next();
//                try {
//                    OutputStream os = s.getOutputStream();
//                    InputStream is = s.getInputStream();
//
//
//
//
//                    int b = is.read();
//                    Log.d(TAG, "Byte Read: "+b);
//
//
//
//
//
//                    s.shutdownOutput();
//                } catch(IOException e) {
//                    Log.e(TAG, "Could not create socket OutputStream.", e);
//                }
//            }
//        }

        public void closeSockets() {
            Iterator<Socket> itr = tSockets.iterator();
            while(itr.hasNext()) {
                try {
                    itr.next().close();
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
            try {
                File f = new File(MyActivity.FILE);
                FileInputStream ios = new FileInputStream(f);

                // Notify of sending file
                Iterator<Socket> itr = tSockets.iterator();
                while(itr.hasNext()) {
                    Socket s = itr.next();
                    try {
                        OutputStream os = s.getOutputStream();

                        os.write(SENDING_FILE);
                    } catch(IOException e) {
                        Log.e(TAG, "Could not create socket OutputStream.", e);
                    }
                }

                int read;
                while((read = ios.read(buffer)) != -1) {

                    itr = tSockets.iterator();
                    while(itr.hasNext()) {
                        Socket s = itr.next();
                        try {
                            OutputStream os = s.getOutputStream();
                            InputStream is = s.getInputStream();

                            os.write(buffer);

                            int b = is.read();
                            if(b == BUFFER_READY) {
                                os.write(START_PLAYBACK);
                            }
                        } catch(IOException e) {
                            Log.e(TAG, "Could not create socket OutputStream.", e);
                        }
                    }

                }
                ios.close();


                itr = tSockets.iterator();
                while(itr.hasNext()) {
                    Socket s = itr.next();
                    s.shutdownOutput();
                }
            } catch(FileNotFoundException e) {
                Log.e(TAG, "File not found", e);
            } catch(IOException e) {
                Log.e(TAG, "IOException", e);
            }

            // Close sockets
            closeSockets();
        }
    }
}

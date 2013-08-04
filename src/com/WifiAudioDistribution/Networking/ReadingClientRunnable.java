package com.WifiAudioDistribution.Networking;

import android.util.Log;
import com.WifiAudioDistribution.ClientManager;
import com.WifiAudioDistribution.Media.MediaContainer;
import com.WifiAudioDistribution.MyActivity;
import com.WifiAudioDistribution.StreamingFile;
import com.WifiAudioDistribution.Media.StreamingMediaPlayer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

// Thread for Data to be read-in by Socket
public class ReadingClientRunnable implements Runnable {
    private static final String TAG = "MYAPP:ReadingClientRunnable";

    private final String StreamingFileName = "StreamingFile";

    private Socket mServerSocket;
    private MyActivity mActivity;

    public ReadingClientRunnable(MyActivity activity, Socket socket) {
        mActivity = activity;
        mServerSocket = socket;
    }

    public void run() {
        // Read in some data
        // this data can be:
        //  a message
        //  file data

        // how long should the socket remain open?
        // socket should remain open till end of thread lifetime
        // bc media player is handled by MediaContainer

        MediaContainer mc = MediaContainer.getInstance();
        mc.resetStopped();

        byte[] buffer = new byte[4096];
        int read = 0;
        InputStream in;
        OutputStream out;
        try {
            in = mServerSocket.getInputStream();
            out = mServerSocket.getOutputStream();

            boolean bufferReadySent = false;
            while((read = in.read(buffer)) != -1 && !Thread.currentThread().isInterrupted()) {
                if(read == 1) {
                    int message = buffer[0];

                    Log.d(TAG, "Message: " + message);
                    if(message == ClientManager.SENDING_FILE) {
                        Log.d(TAG, "Begin Reading in the file");

                        mc.setUp(new StreamingFile(mActivity.getCacheDir(), StreamingFileName));
                    } else if(message == ClientManager.START_PLAYBACK) {
                        Log.d(TAG, "Start Playback");

                        mc.start();

                        out.write(ClientManager.TEST);
                        out.flush();
                        continue;
                    } else if(message == ClientManager.STOP_PLAYBACK) {
                        Log.d(TAG, "Stop Playback");
                        mc.stop();

                        out.write(ClientManager.CONTINUE);
                        out.flush();
                        continue;
                    } else {
                        out.write(ClientManager.UNKNOWN_MESSAGE);
                        out.flush();
                        continue;
                    }
                } else if(mc.isSetUp()) {
                    mc.getStreamingFile().write(buffer, read);

                    if(!bufferReadySent && mc.bufferReady(ClientManager.BUFFER_READY_SIZE)) {
                        Log.d(TAG, "Send Buffer Ready");
                        out.write(ClientManager.BUFFER_READY);
                        out.flush();
                        bufferReadySent = true;
                        continue;
                    }
                }

                out.write(ClientManager.CONTINUE);
                out.flush();
            }
            Log.d(TAG, "Read: "+read);
            in.close();
        } catch(IOException e) {
            Log.e(TAG, "Could not read in from socket.", e);
        }

        try {
            mServerSocket.close();
        } catch(IOException e) {
            Log.e(TAG, "Could not close socket.", e);
        }
    }
}

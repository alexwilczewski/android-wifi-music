package com.WifiAudioDistribution.Networking;

import android.os.Looper;
import android.util.Log;
import com.WifiAudioDistribution.ClientManager;
import com.WifiAudioDistribution.MyActivity;
import com.WifiAudioDistribution.StreamingFile;
import com.WifiAudioDistribution.StreamingMediaPlayer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

// Thread for Data to be read-in by Socket
public class ReadingClientRunnable implements Runnable {
    private static final String TAG = "MYAPP:ReadingClientRunnable";

    private final String StreamingFileName = "StreamingFile";

    private Socket mSocket;
    private MyActivity mActivity;
    private StreamingMediaPlayer smp;

    public ReadingClientRunnable(MyActivity activity, Socket socket) {
        mActivity = activity;
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

            StreamingFile streamFile = new StreamingFile(mActivity.getCacheDir(), StreamingFileName);
            smp.setStreamFile(streamFile);

            boolean readingInFile = false;
            boolean bufferReadySent = false;
            boolean playbackStarted = false;
            while((read = in.read(buffer)) != -1) {
                if(read == 1) {
                    // We've got a message...
                    // Buffer could receive 1 byte without a message
                    // @TODO: Figure out best way to send messages within data transfer
                    int message = buffer[0];
                    Log.d(TAG, "Message: " + message);
                    if(message == ClientManager.SENDING_FILE) {
                        Log.d(TAG, "Begin Reading in the file");
                        startUpReadingInFile();
                        readingInFile = true;
                    } else if(message == ClientManager.START_PLAYBACK) {
                        if(!playbackStarted) {
                            playbackStarted = true;
                            Log.d(TAG, "Start Playback");


                            Log.d(TAG, "Streamfile Tmpsize: "+streamFile.tmpsize());
                            streamFile.transfer();
                            Log.d(TAG, "File Transferred");
                            smp.reassociateStreamFile();
                            smp.start();
                            Log.d(TAG, "Started");
                        }

                        out.write(ClientManager.CONTINUE);
                        out.flush();
                        continue;
                    } else {
                        out.write(ClientManager.UNKNOWN_MESSAGE);
                        out.flush();
                        continue;
                    }
                } else if(readingInFile) {
                    streamFile.write(buffer, read);

                    if(!bufferReadySent && streamFile.tmpsize() >= ClientManager.BUFFER_READY_SIZE) {
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

            if(readingInFile) {
                streamFile.transfer();
                smp.reassociateStreamFile();
            }

        } catch(IOException e) {
            Log.e(TAG, "Could not read in from socket.", e);
        }

        try {
            mSocket.close();
        } catch(IOException e) {
            Log.e(TAG, "Could not close socket.", e);
        }

        Log.d(TAG, "Waiting to interrupt");

        while(!Thread.currentThread().isInterrupted()) { }
        if(smp != null) {
            smp.tearDown();
        }
        Log.d(TAG, "Thread interrupted.");
    }

    public void startUpReadingInFile() {
        smp = new StreamingMediaPlayer();
        smp.setOnPlayingSecondListener(new StreamingMediaPlayer.OnPlayingSecondListener() {
            @Override
            public void onPlayingSecond(StreamingMediaPlayer smp) { }
        });
    }
}

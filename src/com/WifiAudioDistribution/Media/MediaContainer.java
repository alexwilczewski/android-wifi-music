package com.WifiAudioDistribution.Media;

import android.content.Context;
import android.util.Log;
import com.WifiAudioDistribution.ClientManager;
import com.WifiAudioDistribution.StreamingFile;

public class MediaContainer {
    private final String TAG = "MYAPP:MediaContainer";

    public static MediaContainer instance;

    private StreamingMediaPlayer mPlayer;
    private StreamingFile mStreamingFile;
    private boolean hasSetUp;
    private boolean stopped;

    private MediaContainer() {
        reset();
    }

    public static MediaContainer getInstance() {
        if(instance == null) {
            synchronized(MediaContainer.class) {
                if(instance == null) {
                    instance = new MediaContainer();
                }
            }
        }

        return instance;
    }

    public void reset() {
        if(mPlayer != null) {
            mPlayer.tearDown();
        }
        if(mStreamingFile != null) {
            mStreamingFile.tearDown();
        }

        mStreamingFile = null;
        mPlayer = new StreamingMediaPlayer();
        hasSetUp = false;
        stopped = false;
    }

    public void resetStopped() {
        stopped = false;
    }

    public void setUp(StreamingFile streamingFile) {
        mStreamingFile = streamingFile;
        mPlayer.associateStreamingFile(streamingFile);
//        mPlayer.setOnPlayingSecondListener(new StreamingMediaPlayer.OnPlayingSecondListener() {
//            @Override
//            public void onPlayingSecond(StreamingMediaPlayer smp) { }
//        });

        hasSetUp = true;
    }

    public StreamingMediaPlayer getStreamingMediaPlayer() {
        return mPlayer;
    }

    public StreamingFile getStreamingFile() {
        return mStreamingFile;
    }

    public boolean bufferReady(long buffer) {
        return (mStreamingFile.tmpsize() >= buffer);
    }

    public boolean isSetUp() {
        return hasSetUp;
    }

    public void stop() {
        stopped = true;
        mPlayer.stop();
        reset();
    }

    public boolean isStopped() {
        return stopped;
    }

    public void start() {
        Log.d(TAG, "Start");

        stopped = false;

        Log.d(TAG, "Streamfile Tmpsize: " + mStreamingFile.tmpsize());
        mStreamingFile.transfer();
        Log.d(TAG, "File Transferred");
        mPlayer.setUp();
    }
}

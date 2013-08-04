package com.WifiAudioDistribution.Media;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import com.WifiAudioDistribution.StreamingFile;

public class StreamingMediaPlayer {
    private static final String TAG = "MYAPP:StreamingMediaPlayer";

    private final int MESSAGE_SECOND_HANDLER = 1;

    private boolean started = false;

    public MediaPlayer theMediaPlayer;

    private StreamingFile mStreamFile;

    private OnPlayingSecondListener mOnPlayingSecondListener;

    public Timer timer = new Timer();
    public TimerTask tt;

    public StreamingMediaPlayer() {
        Log.d(TAG, "Media Player generated");
        theMediaPlayer = new MediaPlayer();
        reset();
    }

    public void reset() {
        started = false;
        if(theMediaPlayer.isPlaying()) {
            theMediaPlayer.stop();
        }
        theMediaPlayer.reset();
        theMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void tearDown() {
        Log.d(TAG, "MP Tear down");

        started = false;
        mStreamFile = null;

        stopOnPlaying();

        theMediaPlayer.stop();
        theMediaPlayer.release();
    }

    public void stop() {
        started = false;
        stopOnPlaying();
        theMediaPlayer.stop();
    }

    public void associateStreamingFile(StreamingFile streamFile) {
        Log.d(TAG, "Set stream file");
        mStreamFile = streamFile;
    }

    public boolean setUpDataSource() {
        if(mStreamFile == null) {
            return false;
        }

        try {
            stopOnPlaying();
            reset();
            if(mStreamFile.tmpsize() <= 0) {
                return false;
            }

            FileInputStream ins = new FileInputStream(mStreamFile.mFile);
            theMediaPlayer.setDataSource(ins.getFD());
            return true;
        } catch(IllegalStateException e) {
            Log.e(TAG, "IllegalStateException", e);
        } catch(FileNotFoundException e) {
            Log.e(TAG, "FileNotFound", e);
        } catch(IOException e) {
            Log.e(TAG, "IOException", e);
        }

        return false;
    }

    public boolean reassociateStreamFile() {
        Log.d(TAG, "Reassociate");

        if(mStreamFile == null) {
            return false;
        }

        int curr = 0;

        if(!started) {
            curr = theMediaPlayer.getCurrentPosition();
        }

        boolean ret = setUpDataSource();
        Log.d(TAG, "Set up data source: " + (ret ? "Yes" : "No"));
        if(ret) {
            start();
            theMediaPlayer.seekTo(curr);
            return true;
        }

        return false;
    }

    public void start() {
        if(!started) {
            Log.d(TAG, "Starting");
            try {
                theMediaPlayer.prepare();
                started = true;
                Log.i(TAG,"Start Player");
                theMediaPlayer.start();

                startOnPlaying();
            } catch(IOException e) {
                Log.e(TAG, "IOException", e);
            }
        }
    }

    public void startOnPlaying() {
        if(tt == null && mOnPlayingSecondListener != null) {
            tt = new TimerTask() {
                @Override
                public void run() {
                    mOnPlayingSecondListener.onPlayingSecond(StreamingMediaPlayer.this);
                }
            };
            timer.schedule(tt, 0, 1000);
        }
    }

    public void stopOnPlaying() {
        if(tt != null) {
            tt.cancel();
            tt = null;
        }
    }

    public void setOnPlayingSecondListener(OnPlayingSecondListener l) {
        mOnPlayingSecondListener = l;
    }

    public static interface OnPlayingSecondListener {
        void onPlayingSecond(StreamingMediaPlayer smp);
    }
}

package com.WifiAudioDistribution;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

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
        resetMP();
    }

    public void tearDown() {
        Log.d(TAG, "MP Tear down");

        started = false;
        stopOnPlaying();
        theMediaPlayer.stop();
        theMediaPlayer.release();
    }

    public void setStreamFile(StreamingFile streamFile) {
        Log.d(TAG, "Set up streaming file");
        mStreamFile = streamFile;
    }

    public boolean setTheDataSource() {
        try {
            stopOnPlaying();
            resetMP();
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

    public void resetMP() {
        started = false;
        theMediaPlayer.reset();
        theMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public boolean reassociateStreamFile() {
        Log.d(TAG, "Reassociate");

        int curr = theMediaPlayer.getCurrentPosition();
        boolean ret = setTheDataSource();
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
        tt = new TimerTask() {
            @Override
            public void run() {
                if(mOnPlayingSecondListener != null) {
                    mOnPlayingSecondListener.onPlayingSecond(StreamingMediaPlayer.this);
                }
            }
        };
        timer.schedule(tt, 0, 1000);
    }

    public void stopOnPlaying() {
        if(tt != null) {
            tt.cancel();
        }
    }

    public void setOnPlayingSecondListener(OnPlayingSecondListener l) {
        mOnPlayingSecondListener = l;
    }

    public static interface OnPlayingSecondListener {
        void onPlayingSecond(StreamingMediaPlayer smp);
    }
}

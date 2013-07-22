package com.WifiAudioDistribution;
//Code taken from below URL.
//http://blog.pocketjourney.com/2008/04/04/tutorial-custom-media-streaming-for-androids-mediaplayer/
//Good looking out!

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class StreamingMediaPlayer {

    private String DOWNFILE = "downloadingMediaFile";

    private Context context;
    private int counter;
    //TODO should convert to Stack object instead of Vector
    private Vector<MediaPlayer> mediaplayers = new Vector<MediaPlayer>(3);
    private boolean started = false;

    public StreamingMediaPlayer(Context c) {
        counter = 0;
        context = c;
    }

    /**
     * Download the url stream to a temporary location and then call the setDataSource
     * for that local file
     */
    public void storeAudioIncrement(byte[] buf, int numread) throws IOException {
        final String TAG = "storeAudioIncrement";

        File downloadingMediaFile = new File(context.getCacheDir(), DOWNFILE + counter);
        Log.i(TAG, "File name: " + downloadingMediaFile);

        BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(downloadingMediaFile), 32 * 1024);

        bout.write(buf, 0, numread);
        bout.flush();
        bout.close();

        setupplayer(downloadingMediaFile);

        counter++;
    }

    /**
     * Set Up player(s)
     */
    private void setupplayer(File partofaudio) {
        final File f = partofaudio;
        final String TAG = "setupplayer";
        Log.i(TAG, "File " + f.getAbsolutePath());

        Runnable r = new Runnable() {
            public void run() {
                MediaPlayer mp = new MediaPlayer();
                try {
                    MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener () {
                        public void onCompletion(MediaPlayer mp) {
                            String TAG = "MediaPlayer.OnCompletionListener";

                            Log.i(TAG, "Current size of mediaplayer list: " + mediaplayers.size() );
                            boolean waitingForPlayer = false;
                            boolean leave = false;
//                            while (mediaplayers.size() <= 1 && leave == false) {
//                                Log.v(TAG, "waiting for another mediaplayer");
//                                if (waitingForPlayer == false) {
//                                    try {
//                                        Log.v(TAG, "Sleep for a moment");
//                                        //Spin the spinner
//                                        PlayListTab a = (PlayListTab) context ;
//                                        a.handler.sendEmptyMessage(PlayListTab.SPIN);
//                                        Thread.sleep(1000 * 15);
//                                        a.handler.sendEmptyMessage(PlayListTab.STOPSPIN);
//                                        waitingForPlayer = true;
//                                    } catch (InterruptedException e) {
//                                        Log.e(TAG, e.toString());
//                                    }
//                                } else {
//                                    Log.e(TAG, "Timeout occured waiting for another media player");
//                                    Toast.makeText(context, "Trouble downloading audio. :-(" , Toast.LENGTH_LONG).show();
//                                    stop();
//
//                                    leave = true;
//                                }
//                            }
                            if (leave == false && mediaplayers.size() > 1) {
                                MediaPlayer mp2 = mediaplayers.get(1);
                                mp2.start();
                                Log.i(TAG, "Start another player");

                                mp.release();
                                mediaplayers.remove(mp);
                            }
                        }
                    };

                    FileInputStream ins = new FileInputStream(f);
                    mp.setDataSource(ins.getFD());
                    mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

                    mp.setOnCompletionListener(listener);
                    Log.i(TAG, "Prepare Media Player " + f);

                    if ( ! started  ){
                        mp.prepare();
                    } else {
                        //This will save us a few more seconds
                        mp.prepareAsync();
                    }

                    mediaplayers.add(mp);

                    // Notify listener of mediaplayer set up
                    start();
                } catch  (IllegalStateException e) {
                    Log.e(TAG, e.toString(), e);
                } catch  (IOException e) {
                    Log.e(TAG, e.toString(), e);
                }

            }
        };
        new Thread(r).start();
    }

    //Start first audio clip
    public void start() {
        String TAG = "startMediaPlayer";

        //Grab out first media player
        started = true;
        MediaPlayer mp = mediaplayers.get(0);
        Log.i(TAG,"Start Player");
        mp.start();
    }

    //Is the streamer playing audio?
    public boolean isPlaying() {
        String TAG = "isPlaying";
        boolean result = false;
        try {
            MediaPlayer mp = mediaplayers.get(0);
            if (mp.isPlaying()){
                result = true;
            } else {
                result = false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "No items in Media player List");
        }

        return result;
    }

}

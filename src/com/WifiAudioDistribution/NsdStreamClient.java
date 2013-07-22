package com.WifiAudioDistribution;

import android.content.Context;

public class NsdStreamClient {
    public StreamingMediaPlayer smp;

    public NsdStreamClient(Context c) {
        smp = new StreamingMediaPlayer(c);
    }

    public void inMetadataStream(byte[] buf) {

    }

    public void inAudioStream(byte[] buf, int read) {
        try {
            smp.storeAudioIncrement(buf, read);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

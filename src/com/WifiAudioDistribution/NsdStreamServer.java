package com.WifiAudioDistribution;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class NsdStreamServer {

    private static String FILE = "/storage/emulated/0/Music/C2C - Down The Road.mp3";

    public MyActivity mActivity;
    public File streamingFile;

    public NsdStreamServer(MyActivity c) {
        streamingFile = new File(FILE);
        mActivity = c;
    }

    public void stream() {
        try {
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream ous = new ByteArrayOutputStream();
            InputStream ios = new FileInputStream(streamingFile);

            int read = 0;
            int cnt = 0;
            int stop = 50;
            while(read != -1) {
                while ((read = ios.read(buffer)) != -1 && cnt < stop) {
                    ous.write(buffer, 0, read);

                    cnt++;
                }

//                mActivity.readStream(ous.toByteArray(), ous.size());

                ous.reset();
                stop += 50;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

package com.WifiAudioDistribution;

import android.util.Log;

import java.io.*;

public class StreamingFile {
    private static final String TAG = "MYAPP:StreamingFile";

    public File mFile;
    private File mTmpFile;

    private int tmpBytesStored;

    // 1. Create file
    // 2. Write buffer to temp file
    // 3. Write temp file to streamingFile

    public StreamingFile(File dir, String filename) {
        String tmpFilename = "."+filename+".tmp";

        mFile = new File(dir, filename);
        mTmpFile = new File(dir, tmpFilename);

        Log.d(TAG, "File Location: "+mFile.getAbsolutePath());

        try {
            new FileOutputStream(mFile, false).close();
            new FileOutputStream(mTmpFile, false).close();
        } catch(FileNotFoundException e) {
            Log.e(TAG, "File not found", e);
        } catch(IOException e) {
            Log.e(TAG, "IOException", e);
        }

        tmpBytesStored = 0;
    }

    public void tearDown() {
        tmpBytesStored = 0;
        mFile.delete();
        mTmpFile.delete();
    }

    public synchronized void write(byte[] data, int len) {
        BufferedOutputStream bout = null;
        try {
            bout = new BufferedOutputStream(new FileOutputStream(mTmpFile, true));

            bout.write(data, 0, len);
            tmpBytesStored += len;
            bout.flush();
            bout.close();
        } catch(FileNotFoundException e) {
            Log.e(TAG, "File not found", e);
        } catch(IOException e) {
            Log.e(TAG, "IOException", e);
        }
    }

    public synchronized void transfer() {
        BufferedInputStream fromStream = null;
        BufferedOutputStream toStream = null;
        try {
            fromStream = new BufferedInputStream(new FileInputStream(mTmpFile));
            toStream = new BufferedOutputStream(new FileOutputStream(mFile));

            byte[] buffer = new byte[4096];
            int read = 0;
            while((read = fromStream.read(buffer)) != -1) {
                toStream.write(buffer, 0, read);
            }
            fromStream.close();
            toStream.flush();
            toStream.close();
        } catch(FileNotFoundException e) {
            Log.e(TAG, "File not found", e);
        } catch(IOException e) {
            Log.e(TAG, "IOException", e);
        }
    }

    public int tmpsize() {
        return tmpBytesStored;
    }
}

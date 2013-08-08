package com.WifiAudioDistribution;

import com.WifiAudioDistribution.Networking.ClientInfo;
import com.WifiAudioDistribution.Networking.SendingClientRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunningPod {
    private final String TAG = "MYAPP:RunningPod";

    private static Map<Long, RunningPod> mRunningPods;
    private List<ClientInfo> mConnections;

    private File mFile;

    private Thread mSendingThread;
    private SendingClientRunnable mSendingClientRunnable;

    public static RunningPod get(Pod pod) {
        if(mRunningPods == null) {
            mRunningPods = new HashMap<Long, RunningPod>();
        }

        Long Key = new Long(pod.getId());
        if(mRunningPods.containsKey(Key)) {
            return mRunningPods.get(Key);
        }

        RunningPod rPod = new RunningPod(pod);
        mRunningPods.put(Key, rPod);
        return rPod;
    }

    private RunningPod(Pod pod) {
        mConnections = pod.getConnections();
    }

    public void tearDown() {
        mSendingThread.interrupt();
    }

    public void setFile(File playbackFile) {
        mFile = playbackFile;
    }

    public void start() {
        mSendingClientRunnable = new SendingClientRunnable(mConnections);
        mSendingClientRunnable.setFile(mFile);

        mSendingThread = new Thread(mSendingClientRunnable);
        mSendingThread.start();
    }

    public void stopPlayback() {
        if(mSendingClientRunnable != null) {
            mSendingClientRunnable.stopPlayback();
        }
    }
}

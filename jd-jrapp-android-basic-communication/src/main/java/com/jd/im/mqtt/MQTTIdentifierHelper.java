package com.jd.im.mqtt;


import com.jd.im.utils.Log;
import android.util.SparseArray;

import com.jd.im.IMQTTMessage;
import com.jd.im.mqtt.messages.MQTTMessage;


public class MQTTIdentifierHelper {

    /**
     * 最小消息id
     */
    private static final int MIN_MSG_ID = 1;
    /**
     * 最大消息id
     */
    private static final int MAX_MSG_ID = 65535;
    private static String TAG = "MQTTIdentifierHelper";
    private final Object lock = new Object();
    private SparseArray<MQTTMessage> sentPackages = new SparseArray<>();
    private SparseArray<MQTTMessage> receivedPackages = new SparseArray<>();
    private SparseArray<Integer> usedIds = new SparseArray<>();
    private int nextId = MIN_MSG_ID;


    public void addSentPackage(MQTTMessage msg) {
        synchronized (lock) {
            nextId = Math.max(nextId, msg.getPackageIdentifier());
            sentPackages.put(msg.getPackageIdentifier(), msg);
            usedIds.put(msg.getPackageIdentifier(), msg.getPackageIdentifier());
        }
    }


    public synchronized void removeSentPackage(int identifier) {
        synchronized (lock) {
            sentPackages.remove(identifier);
            usedIds.remove(identifier);
        }
    }

    public SparseArray<MQTTMessage> getSentPackages() {
        synchronized (lock) {
            return sentPackages;
        }
    }



    public IMQTTMessage getMessageFromReceivedPackages(int identifier) {
        synchronized (lock) {
            return receivedPackages.get(identifier);
        }
    }


    public void addReceivedPackage(MQTTMessage msg) {
        synchronized (lock) {
            receivedPackages.put(msg.getPackageIdentifier(), msg);
            nextId = Math.max(nextId, msg.getPackageIdentifier());
            usedIds.put(msg.getPackageIdentifier(), msg.getPackageIdentifier());
        }
    }


    public void removeReceivedPackage(int identifier) {
        synchronized (lock) {
            receivedPackages.remove(identifier);
            usedIds.remove(identifier);
        }
    }

    /**
     * 在客户端域内产生不重复的消息协议id
     *
     * @return
     */
    public int getIdentifier() {
        synchronized (lock) {
            int startId = nextId;

            int loopCount = 0;

            do {
                nextId++;
                if (nextId > MAX_MSG_ID) {
                    nextId = MIN_MSG_ID;
                }
                if (nextId == startId) {
                    loopCount++;
                    if (loopCount == 2) {
                        Log.e(TAG, "消息id资源用完");
                    }
                }
            } while (usedIds.get(nextId) != null);
            usedIds.put(nextId, nextId);
            return nextId;
        }
    }
}

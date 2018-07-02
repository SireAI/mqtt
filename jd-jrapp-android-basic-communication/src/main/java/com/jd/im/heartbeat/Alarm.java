package com.jd.im.heartbeat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.RestrictTo;

import com.jd.im.utils.Log;


import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/30
 * Author:wangkai
 * Description: alarm 管理器
 * =====================================================
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public  class Alarm extends BroadcastReceiver {

    private final static String TAG = "MicroMsg.Alarm";
    private final static String KEXTRA_ID = "ID";
    private final static String KEXTRA_PID = "PID";
    private final static int ID = 0;
    private final static int WAITTIME = 1;
    private final static int PENDINGINTENT = 2;


    private static WakerLock wakerlock = null;
    private static Alarm bc_alarm = null;
    private static TreeSet<Object[]> alarm_waiting_set = new TreeSet(new ComparatorAlarm());
    private ICallBack callBack;

    public static void resetAlarm(final Context context) {
        synchronized (alarm_waiting_set) {
            Iterator<Object[]> it = alarm_waiting_set.iterator();
            while (it.hasNext()) {
                cancelAlarmMgr(context, (PendingIntent) (it.next()[PENDINGINTENT]));
            }
            alarm_waiting_set.clear();
            if (null != bc_alarm) {
                context.unregisterReceiver(bc_alarm);
                bc_alarm = null;
            }
        }
    }

    /**
     * 通过闹钟发送定时任务
     *
     * @param id      任务id
     * @param after   过多久执行
     * @param context 上下文
     * @return
     */
    public static boolean start(final long id, final long after, final Context context, ICallBack callBack) {
        long curtime = SystemClock.elapsedRealtime();

        if (0 > after) {
            Log.e(TAG, "after is " + after);
            return false;
        }

        if (null == context) {
            Log.e(TAG, "null==context");
            return false;
        }

        synchronized (alarm_waiting_set) {
            if (null == wakerlock) {
                wakerlock = new WakerLock(context);
                Log.i(TAG, "start new wakerlock");
            }

            if (null == bc_alarm) {
                bc_alarm = new Alarm();
                context.registerReceiver(bc_alarm, new IntentFilter("ALARM_ACTION(" + String.valueOf(Process.myPid()) + ")"));
            }
            bc_alarm.setCallBack(callBack);

            Iterator<Object[]> it = alarm_waiting_set.iterator();
            while (it.hasNext()) {
                if ((Long) (it.next()[ID]) == id) {
                    Log.e(TAG, "id exist");
                    return false;
                }
            }

            long waittime = after >= 0 ? curtime + after : curtime;

            PendingIntent pendingIntent = setAlarmMgr(id, waittime, context);
            if (null == pendingIntent) return false;
            //缓存
            alarm_waiting_set.add(new Object[]{id, waittime, pendingIntent});
        }
        return true;
    }

    public static boolean stop(final long id, final Context context) {

        if (null == context) {
            Log.e(TAG, "context==null");
            return false;
        }

        synchronized (alarm_waiting_set) {
            Iterator<Object[]> it = alarm_waiting_set.iterator();
            while (it.hasNext()) {
                Object[] next = it.next();
                if ((Long) (next[ID]) == id) {
                    cancelAlarmMgr(context, (PendingIntent) (next[PENDINGINTENT]));
                    it.remove();
                    return true;
                }
            }

        }

        return false;
    }

    /**
     * 构建pendingintent,并设定发送时间
     *
     * @param id
     * @param time
     * @param context
     * @return
     */
    private static PendingIntent setAlarmMgr(final long id, final long time, final Context context) {
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) {
            Log.e(TAG, "am == null");
            return null;
        }

        Intent intent = new Intent();
        intent.setAction("ALARM_ACTION(" + String.valueOf(Process.myPid()) + ")");
        intent.putExtra(KEXTRA_ID, id);
        intent.putExtra(KEXTRA_PID, Process.myPid());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (Build.VERSION.SDK_INT < 19) { //KITKAT
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, time, pendingIntent);
        } else {
            am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, time, pendingIntent);
        }

        return pendingIntent;
    }

    private static boolean cancelAlarmMgr(final Context context, PendingIntent pendingIntent) {
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) {
            Log.e(TAG, "am == null");
            return false;
        }
        if (pendingIntent == null) {
            Log.e(TAG, "pendingIntent == null");
            return false;
        }

        am.cancel(pendingIntent);
        pendingIntent.cancel();
        return true;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (null == context || null == intent) return;

        final long id = intent.getLongExtra(KEXTRA_ID, -1);
        final Integer pid = intent.getIntExtra(KEXTRA_PID, -1);

        if (-1 == id || -1 == pid) return;
        //应用重启后，pid较大可能不一致，上一次应用程序的定时请求不处理
        if (pid != Process.myPid()) {
            Log.w(TAG, "onReceive id:"+id+", pid:%d, mypid:"+pid);
            return;
        }

        boolean found = false;
        synchronized (alarm_waiting_set) {
            Iterator<Object[]> it = alarm_waiting_set.iterator();
            while (it.hasNext()) {
                Object[] next = it.next();
                long curId = (long) next[ID];
                if (curId==id) {
                    it.remove();
                    found = true;
                    break;
                }
            }
            if (!found)
                Log.e(TAG, "onReceive not found id:"+id+", pid:"+pid+", alarm_waiting_set.size:"+alarm_waiting_set.size());
        }
        if (null != wakerlock) wakerlock.lock(200);
        if(callBack!=null){
            callBack.onAlarm();
        }
    }

    private void setCallBack(ICallBack callBack) {
        this.callBack = callBack;
    }


    private static class ComparatorAlarm implements Comparator<Object[]> {
        @Override
        public int compare(Object[] lhs, Object[] rhs) {
            return (int) ((Long) (lhs[ID]) - (Long) (rhs[ID]));
        }
    }

    public interface ICallBack{
        void onAlarm();
    }
}

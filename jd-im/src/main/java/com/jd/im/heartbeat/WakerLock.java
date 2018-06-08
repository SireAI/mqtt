package com.jd.im.heartbeat;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/30
 * Author:wangkai
 * Description: 屏幕锁，保活系统cpu
 * =====================================================
 */
public class WakerLock {
	private static final String TAG = "MicroMsg.WakerLock";

	private PowerManager.WakeLock wakeLock = null;
	private Handler mHandler = null;
	private Runnable mReleaser = new Runnable() {
		@Override
		public void run() {
			unLock();
		}
	};

	public WakerLock(final Context context) {
		final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		wakeLock.setReferenceCounted(false);
		mHandler = new Handler(context.getMainLooper());
	}

	@Override
	protected void finalize() throws Throwable {
		unLock();
		super.finalize();
	}

	/**
	 * 获取屏幕锁一段时间后释放锁
	 * @param timeInMills
	 */
	public void lock(final long timeInMills) {
		lock();
		mHandler.postDelayed(mReleaser, timeInMills);
	}

	/**
	 * 获取锁
	 */
	public void lock() {
		mHandler.removeCallbacks(mReleaser);
		wakeLock.acquire();
	}

	/**
	 * 释放锁
	 */
	public void unLock() {
		mHandler.removeCallbacks(mReleaser);
		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
	}

	/**
	 * 是否获得锁
	 * @return
	 */
	public boolean isLocking() {
		return wakeLock.isHeld();
	}
	
}

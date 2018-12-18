package com.jd.im.heartbeat;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.support.annotation.IntDef;
import android.support.annotation.RestrictTo;

import com.jd.im.utils.Log;

import com.jd.im.heartbeat.net.NetState;
import com.jd.im.heartbeat.net.NetStatusUtil;
import com.jd.im.heartbeat.net.NetworkSignalStrength;
import com.jd.im.utils.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.atomic.AtomicInteger;

import static com.jd.im.mqtt.MQTTConnectionConstants.CLIENT_MAX_INTERVAL;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/30
 * Author:wangkai
 * Description:心跳自适应
 * =====================================================
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class PinDetecter {
    public static final String ACTION = "com.jd.im.mode";
    /**
     * 前台
     */
    public static final int ACTIVE_MODE = 1;
    /**
     * 后台或关屏
     */
    public static final int BACKGORUND_MODE = 2;
    /**
     * 断网状态
     */
    public static final int IDEL_MODE = 3;
    /**
     * 边界摆动值
     */
    public static final int SAFE_OUTER = 2000;
    /**
     * 心跳最短间隔
     */
    private static final int MIN_HEART_INTERVAL = 10000;
    /**
     * 心跳最大间隔
     */
    private static final int MAX_HEART_INTERVAL = CLIENT_MAX_INTERVAL;
    /**
     * 心跳间隔失败后的重试次数
     */
    private static final int HEAT_FAILED_ATEMPT_COUNT = 3;
    private  Context mContext;
    /**
     * 心跳探测期增加步长
     */
    private  int HEART_ATEMPT_STEP = 1000;
    private static int DEFAULT_PRE_FIX_COUNT = 5;
    private static String TAG = "PinDetecter";
    /**
     * pin策略回调通知
     */
    private final PinDetectCallBack pinDetectCallBack;
    /**
     * 网络信号强弱
     */
    private final NetworkSignalStrength networkSignalStrength;
    /**
     * 固定心跳间隔
     */
    private int FIXED_HEART_INTERVAL = MIN_HEART_INTERVAL;
    /**
     * 当前尝试心跳
     */
    private int currentHeartInterval = FIXED_HEART_INTERVAL;

    private int failedRepeatCount = HEAT_FAILED_ATEMPT_COUNT;
    private AtomicInteger mode = new AtomicInteger(IDEL_MODE);
    /**
     * 是否稳定态
     */
    private boolean isHeartIntervalStable;
    /**
     * NAT超时边界
     */
    private boolean reachNATthreshold = false;

    /**
     * 在进入探测前确定稳定的网络环境心跳次数
     */
    private int backgroudModePreFixCount = DEFAULT_PRE_FIX_COUNT;
    /**
     * 探测态连接重试参数
     */
    private RetrayParam tryModeParam = new RetrayParam(3 * HEAT_FAILED_ATEMPT_COUNT + 2, 0);
    /**
     * 稳定态连接重试参数
     */
    private RetrayParam stableModeParam = new RetrayParam(3, 5000);


    /**
     * 网络状态变化通知
     */
    private NetState netState = new NetState() {
        @Override
        public void onNetworkChange(Context context, boolean connected) {
            Log.d(TAG, "network changed :"+(connected?"connected":"disConnected"));
            FIXED_HEART_INTERVAL = MIN_HEART_INTERVAL;
            if (connected) {
                //网络变化处于连接状态
                int previouseMode = mode.get();
                if (previouseMode == IDEL_MODE) {
                    //从断网到联网
                    boolean appIsInBackground = Utils.isAppIsInBackground(context);
                    PinDetecter.this.mode.set(appIsInBackground ? BACKGORUND_MODE : ACTIVE_MODE);
                    if (PinDetecter.this.mode.get() == BACKGORUND_MODE) {
                        resetBackgroudMode();
                    }
                } else if (previouseMode == ACTIVE_MODE) {
                    //切换网络，调整到最小跳
                } else if (previouseMode == BACKGORUND_MODE) {
                    //切换网络，重新学习
                    resetBackgroudMode();
                }
                if (pinDetectCallBack != null) {
                    pinDetectCallBack.onNetConnected();
                }
            } else {
                //断网
                PinDetecter.this.mode.set(IDEL_MODE);
                if (pinDetectCallBack != null) {
                    pinDetectCallBack.onNetLoss();
                }
            }
        }
    };

    public int getCurrentHeartInterval() {
        return currentHeartInterval;
    }

    public PinDetecter(Context context, @Mode int mode, PinDetectCallBack pinDetectCallBack) {
        this.mode.set(mode);
        this.pinDetectCallBack = pinDetectCallBack;
        networkSignalStrength = new NetworkSignalStrength(context);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(netState, filter);
        this.mContext = context;
    }

    public int getFixedHeartInterval() {
        return FIXED_HEART_INTERVAL;
    }

    /**
     * 若前置状态是idel,
     * idel ,background
     * idel,active
     * active ,background
     * backgroud,active
     *
     * @param mode
     */
    public void changeEvent(@Mode int mode) {
        int previouseMode = this.mode.get();
        if(previouseMode == mode){
            return;
        }
        if (mode == BACKGORUND_MODE) {
            //已经达到稳态
            boolean goodSignal = false;
            if (previouseMode == ACTIVE_MODE && isHeartIntervalStable) {
                goodSignal = goodSignalInterval();
            } else {
                FIXED_HEART_INTERVAL = MIN_HEART_INTERVAL;
            }
            resetBackgroudMode();
            //若固定心跳不等于最小间隔，并且当前信号强度仍然是非常好，那么不需要探测
            if (goodSignal && FIXED_HEART_INTERVAL != MIN_HEART_INTERVAL) {
                this.isHeartIntervalStable = true;
            }
        }
        if (mode == ACTIVE_MODE) {
            if (previouseMode == BACKGORUND_MODE && isHeartIntervalStable) {
                goodSignalInterval();
            } else {
                FIXED_HEART_INTERVAL = MIN_HEART_INTERVAL;
            }
        }
        if (previouseMode != IDEL_MODE) {
            this.mode.set(mode);
        }
    }

    public void setMode(int mode){
        this.mode.set(mode);
    }

    private boolean goodSignalInterval() {
        int signalLevel = networkSignalStrength.getSignalLevel();
        if (signalLevel >= 3) {
            FIXED_HEART_INTERVAL = Math.max(this.currentHeartInterval, MIN_HEART_INTERVAL);
            return true;
        }
        return false;
    }


    /**
     * 稳定态或者ACTIVE_MODE使用stable
     *
     * @return
     */
    public RetrayParam getRetrayParam() {
        return isHeartIntervalStable || mode.get() == ACTIVE_MODE ? stableModeParam : tryModeParam;
    }


    /**
     * 重置参数
     */
    public void reset() {
        this.FIXED_HEART_INTERVAL = MIN_HEART_INTERVAL;
        this.currentHeartInterval = FIXED_HEART_INTERVAL;
        this.failedRepeatCount = HEAT_FAILED_ATEMPT_COUNT;
    }

    private void resetBackgroudMode() {
        this.backgroudModePreFixCount = DEFAULT_PRE_FIX_COUNT;
        this.isHeartIntervalStable = false;
        this.currentHeartInterval = FIXED_HEART_INTERVAL;
    }

    /**
     * 收到回复pin
     */
    public void pinReceivedProcess() {
        switch (mode.get()) {
            case ACTIVE_MODE:
                if (pinDetectCallBack != null) {
                    //前台时使用固定的心跳，保证及时收发消息
                    this.currentHeartInterval = FIXED_HEART_INTERVAL;
                    pinDetectCallBack.onHeartIntervalChanged(true, this.currentHeartInterval);
                }
                break;
            case BACKGORUND_MODE:
                if (pinDetectCallBack != null) {
                    if (backgroudModePreFixCount > 0) {
                        //确定网络稳定环境
                        backgroudModePreFixCount--;
                        this.currentHeartInterval = FIXED_HEART_INTERVAL;
                        pinDetectCallBack.onHeartIntervalChanged(true, this.currentHeartInterval);
                    } else {
                        //探测期
                        if (failedRepeatCount < HEAT_FAILED_ATEMPT_COUNT || reachNATthreshold) {
                            //达到边界值特定次数重试成功，或者回退一步直接成功
                            int atempts = HEAT_FAILED_ATEMPT_COUNT - failedRepeatCount;
                            if (atempts > 0) {
                                Log.d(TAG, "心跳间隔" + currentHeartInterval + "进行了" + atempts + "次重试后成功...");
                            }
                            this.failedRepeatCount = HEAT_FAILED_ATEMPT_COUNT;
                            //非稳定态，需要回退
                            if (!isHeartIntervalStable) {
                                currentHeartInterval -= SAFE_OUTER;
                                if (currentHeartInterval < MIN_HEART_INTERVAL) {
                                    currentHeartInterval = MIN_HEART_INTERVAL;
                                }
                                reachNATthreshold = false;
                            }
                            this.isHeartIntervalStable = true;
                            Log.d(TAG, "稳定态心跳间隔时间：" + currentHeartInterval);
                        }
                        if (isHeartIntervalStable) {
                            pinDetectCallBack.onHeartIntervalChanged(true, currentHeartInterval);
                        } else {
                            calculateStep();
                            //继续探测，增大间隔
                            this.currentHeartInterval += HEART_ATEMPT_STEP;
                            if(this.currentHeartInterval>MAX_HEART_INTERVAL){
                                this.currentHeartInterval = MAX_HEART_INTERVAL;
                                this.reachNATthreshold = true;
                            }
                            pinDetectCallBack.onHeartIntervalChanged(true, this.currentHeartInterval);
                        }
                    }

                }
                break;
            case IDEL_MODE:
                break;
            default:
                break;
        }
    }


    /**
     * 长连接连通后维持失败
     */
    public synchronized void onSocketFailed() {
        //处于后台模式，当前间隔时间与固定间隔不相同，视为探测过程中
        if (mode.get() == BACKGORUND_MODE) {
            //一次失败即重置固定心跳间隔
            if (FIXED_HEART_INTERVAL != MIN_HEART_INTERVAL) {
                FIXED_HEART_INTERVAL = MIN_HEART_INTERVAL;
            }
            if (backgroudModePreFixCount == 0) {
                if (failedRepeatCount > 0) {
                    this.failedRepeatCount--;
                    Log.d(TAG, "间隔" + currentHeartInterval + "重试第" + (HEAT_FAILED_ATEMPT_COUNT - failedRepeatCount) + "次");
                } else {
                    //该临界值已经尝试特定次数，无法通过,回退到上一次成功的间隔
                    this.reachNATthreshold = true;
                    calculateStep();
                    int newHeartInterval = this.currentHeartInterval - HEART_ATEMPT_STEP;
                    if (newHeartInterval < MIN_HEART_INTERVAL) {
                        Log.w(TAG, "已经触顶，达到最小间隔：" + MIN_HEART_INTERVAL);
                        this.currentHeartInterval = MIN_HEART_INTERVAL;
                    } else {
                        this.currentHeartInterval = newHeartInterval;
                    }
                    Log.d(TAG, "当前间隔连接失败，更换连接间隔为：" + this.currentHeartInterval);
                    failedRepeatCount = HEAT_FAILED_ATEMPT_COUNT;
                    if (pinDetectCallBack != null) {
                        pinDetectCallBack.onHeartIntervalChanged(false, this.currentHeartInterval);
                    }
                }
            } else {
                //如果值为0，那么说明已经连续一定次数收到Pin,表明网络环境稳定，否则继续重试
                backgroudModePreFixCount = DEFAULT_PRE_FIX_COUNT;
            }

        } else {
            reset();
        }
    }

    /**
     * 加快收敛
     */
    private void calculateStep() {
        int signalLevel = networkSignalStrength.getSignalLevel();
        if(signalLevel>1){
            HEART_ATEMPT_STEP = (signalLevel-1)*1000;
        }else {
            HEART_ATEMPT_STEP = 1000;
        }
    }

    public void release() {
        if (netState != null) {
            try {
                mContext.unregisterReceiver(netState);
                mContext = null;
            }catch (Exception e){
            }
        }
    }

    public boolean isActive(){
        return mode.get() == ACTIVE_MODE;
    }

    public int getMode() {
        return mode.get();
    }

    @IntDef({ACTIVE_MODE, BACKGORUND_MODE, IDEL_MODE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    public interface PinDetectCallBack {
        /**
         * 心跳间隔改变
         *
         * @param needResetState
         * @param newHeartInterval
         */
        void onHeartIntervalChanged(boolean needResetState, int newHeartInterval);


        /**
         * 联网
         */
        void onNetConnected();

        /**
         * 断网
         */
        void onNetLoss();
    }

}

package com.jd.im.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.WorkerThread;

import com.jd.im.ConnectStateCallBack;
import com.jd.im.IMLocalService;
import com.jd.im.IMQTTMessage;
import com.jd.im.IMRemoteService;
import com.jd.im.IMqttConnectOptions;
import com.jd.im.IVariableHeaderExtraPart;
import com.jd.im.heartbeat.PinSender;
import com.jd.im.heartbeat.RetrayParam;
import com.jd.im.heartbeat.ScheduledRetray;
import com.jd.im.heartbeat.net.NetStatusUtil;
import com.jd.im.mqtt.MqttQos;
import com.jd.im.mqtt.messages.MQTTConnect;
import com.jd.im.mqtt.messages.MQTTDisconnect;
import com.jd.im.mqtt.messages.MQTTPingreq;
import com.jd.im.mqtt.messages.MQTTPublish;
import com.jd.im.mqtt.messages.MQTTSubscribe;
import com.jd.im.mqtt.messages.MQTTUnsubscribe;
import com.jd.im.socket.Event;
import com.jd.im.socket.ExpirationListener;
import com.jd.im.socket.SocketCallBack;
import com.jd.im.socket.SocketWorker;
import com.jd.im.socket.TimingWheel;
import com.jd.im.storage.DatabaseMessageStore;
import com.jd.im.storage.MessageStore;
import com.jd.im.storage.Persistentable;
import com.jd.im.utils.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.jd.im.heartbeat.PinDetecter.ACTIVE_MODE;
import static com.jd.im.mqtt.MQTTConnectionConstants.STATE_CONNECTED;
import static com.jd.im.mqtt.MQTTConnectionConstants.STATE_CONNECTING;
import static com.jd.im.mqtt.MQTTConnectionConstants.STATE_CONNECTION_FAILED;
import static com.jd.im.mqtt.MQTTConnectionConstants.STATE_NONE;
import static com.jd.im.mqtt.MQTTConstants.AT_MOST_ONCE;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/30
 * Author:wangkai
 * Description:消息后台服务
 * =====================================================
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class MqttService extends Service implements SocketCallBack, Handler.Callback, MqttQos.QosCallBack, ExpirationListener<Event<MqttSenderAction>>, PinSender.ICallBack, ScheduledRetray.ICallBack {
    private static final String DATA_WORKER = "dataWorker";
    private static final String SOCKET_WORKER = "socketWorker";
    private static final String TAG = "MqttService";
    private static final int DATA = 5;
    private static final int RECONNECT = 6;
    private Object lock = new Object();
    /**
     * 配合超时时间，这个次数大约是半天，正常连续超时重传1小时，如果仍然无法正确应答
     * 那么此类消息服务器可能无法正确应答，丢弃掉
     */
    private static final int MESSAGE_REPEAT_COUNT = 180;
    /**
     * 默认信息发送超时时间,单位s
     */
    private static  int MESSAGE_DELIVERRY_RETRAY_INTERVAL = 30;
    /**
     * 客户端标识，用于处理不同服务地址问题
     */
    private String clientHandle;
    /**
     * 是否一周期重试
     */
    private boolean automaticReconnect = true;
    /**
     * 客户端操作接口
     */
    private IMRemoteService imRemoteService;
    /**
     * qos操作
     */
    private MqttQos mqttQos;

    /**
     * 处理连接socket回调
     */
    private ConnectStateCallBack connectCallBack;

    /**
     * socket处理线程
     */
    private SocketWorker socketWorer;

    /**
     * 心跳时间间隔
     */
    private int KEEP_ALIVE_TIMER = 3000;
    /**
     * 连接配置
     */
    private IMqttConnectOptions connectOptions;
    private Handler dataWorkerSender;
    /**
     * 异步处理，在服务创建时创建
     */
    private HandlerThread dataWorker;
    /**
     * 时间轮定时器
     */
    private TimingWheel<Event<MqttSenderAction>> eventTimingWheel;
    private PinSender pinSender;
    private MqttReconnectAction reconnectAction = new MqttReconnectAction();
    private ScheduledRetray scheduledRetray;
    private int connectState = STATE_NONE;
    private MessageStore messageStore;
    private IBinder.DeathRecipient mDeathRecipient;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return imRemoteService.asBinder();
    }

    /**
     * 再次绑定调用 onRebind方法
     *
     * @param intent
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        if(imRemoteService == null){
            imRemoteService = new IMRemoteServiceImpl(this);
        }
        binderLifeCycle();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        imRemoteService = new IMRemoteServiceImpl(this);
        binderLifeCycle();
        mqttQos = new MqttQos(this);
        messageStore = new DatabaseMessageStore(this);
        startDataWorker();
    }

    private void binderLifeCycle() {
        if(imRemoteService!=null && mDeathRecipient == null){
            try {
                mDeathRecipient = new IBinder.DeathRecipient() {
                    @Override
                    public void binderDied() {
                        synchronized (lock){
                            Log.d(TAG, "binder died. name:" + Thread.currentThread().getName());
                            if (imRemoteService == null)
                                return;
                            imRemoteService.asBinder().unlinkToDeath(this, 0);
                            imRemoteService = null;
                            mDeathRecipient = null;
                        }
                    }
                };
                imRemoteService.asBinder().linkToDeath(mDeathRecipient, 0);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onDestroy() {
        Log.w(TAG, "服务销毁");
//        stopForeground(true);
        disconnect(true);
        pinSender.release();
        cancelDataWorker();
        stopTimingWheel();
        closeDB();
        synchronized (lock){
            if(imRemoteService!=null && mDeathRecipient!=null){
                imRemoteService.asBinder().unlinkToDeath(mDeathRecipient,0);
            }
        }
        super.onDestroy();
    }

    private void closeDB() {
        if (messageStore != null) {
            messageStore.close();
            messageStore = null;
        }
    }

    /**
     * 连接状态通知
     *
     * @param state
     */
    @Override
    public void notifyConnectState(int state) {
        this.connectState = state;
        try {
            if (state == STATE_CONNECTED) {
                startPin();
                if (connectOptions != null && !connectOptions.isCleanSession()) {
                    resumeData();
                }
            } else {
                stopPin();
            }
            if (connectCallBack != null && connectCallBack.asBinder().isBinderAlive()) {
                connectCallBack.onConnectStateChange(state);
            }
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * socket连接失败处理,socketworker线程
     *
     * @param type 0表示连接异常，1表示socket读异常,2表示socket写异常
     * @param e
     */
    @WorkerThread
    @Override
    public void onSocketFailed(final int type, Exception e) {
        sendTask(new Runnable() {
            @Override
            public void run() {
                if (type == SocketCallBack.CONNECT_EXCEPTION) {
                    //长链建立失败
                    notifyConnectState(STATE_CONNECTION_FAILED);
                    reconnect();
                } else if (type == SocketCallBack.SOCKET_READ_EXCPTION || type == SocketCallBack.SOCKET_WRITE_EXCPTION) {
                    //长链建立后交互失败，重新初始化
                    pinSender.getPinDetecter().onSocketFailed();
                    disconnect(false);
                    reconnect();
                }
            }
        });
    }

    /**
     * 建立连接成功回调，socketworker线程
     */
    @Override
    public void onSocketSuccees() {
        Log.d(TAG, "MQTT建立连接,发送连接验证消息....");
        //长链建立，发送长链验证消息
        startTimingWheel();
        connect();
    }
    int i = 0;
    /**
     * 数据到达,发往数据线程队列处理
     *
     * @param data
     */
    @Override
    public void onDataArrived(byte[] data) {
        if (dataWorkerSender != null) {
            Message message = dataWorkerSender.obtainMessage();
            message.obj = data;
            message.what = DATA;
            dataWorkerSender.sendMessage(message);
        }
    }

    /**
     * 连接关闭前发送disconnect消息，调用者线程
     */
    @WorkerThread
    @Override
    public void onSocketClose(final Runnable task) {
        if (socketWorer != null) {
            socketWorer.sendData(MQTTDisconnect.newInstance().get());
            socketWorer = null;
        }
        task.run();
    }

    /**
     * qos消息机制处理
     *
     * @param msg
     * @return
     */
    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == DATA && msg.obj != null && mqttQos != null && msg.obj instanceof byte[]) {
            mqttQos.operate((byte[]) msg.obj);
            return true;
        }
        if (msg.what == RECONNECT && msg.obj != null && msg.obj instanceof MqttReconnectAction) {
            ((MqttReconnectAction) msg.obj).excute();
            return true;
        }
        return false;
    }

    /**
     * 发布消息
     *
     * @param msg
     */
    @Override
    public void sendMessage(final IMQTTMessage msg) {
        if (socketWorer != null && dataWorkerSender != null && eventTimingWheel != null) {
            Event<MqttSenderAction> event = createEvent(msg);
            if (event != null) {
                deliveryRetrayPost(event);
                pinSender.refreshLatestExchangeTime();
            } else {
                MqttSenderAction mqttSenderAction = new MqttSenderAction(socketWorer, msg);
                sendTask(mqttSenderAction);
            }
        }
    }


    @Override
    public  void dispatchPush(IMQTTMessage publish) {
        try {

            if(imRemoteService != null){
                synchronized (lock){
                    if(!imRemoteService.asBinder().isBinderAlive()){
                        imRemoteService.unBindLocalService();
                    }
                }
                IMLocalService localService = imRemoteService.getLocalService();
                if (localService != null) {
                    if(localService.asBinder().isBinderAlive()){
                        localService.push2Client(publish);
                    }
                } else {
                    Log.w(TAG, "服务端消息到达，但客户端服务未绑定,消息将丢失...");
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPinReceived() {

        Log.d(TAG, "pin received by the time " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
        //处于一次重试的连接
        RetrayParam retrayParam = pinSender.getPinDetecter().getRetrayParam();
        if (retrayParam.hasRetried()) {
            retrayParam.reset();
        }
        if (pinSender != null) {
            pinSender.getPinDetecter().pinReceivedProcess();
        }
    }

    @Override
    public boolean isLocalServiceAvailable() {
        try {
            return imRemoteService != null && imRemoteService.getLocalService() != null;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public  void notifyOperationResult(boolean success, int id,byte[] extraInfor) {
        pinSender.refreshLatestExchangeTime();
        try {
            if(imRemoteService != null){
                synchronized (lock){
                    if(!imRemoteService.asBinder().isBinderAlive()){
                        imRemoteService.unBindLocalService();
                    }
                }
                IMLocalService localService = imRemoteService.getLocalService();
                if (localService != null) {
                    if(localService.asBinder().isBinderAlive()){
                        localService.operationResult(success, id,extraInfor);
                    }
                } else {
                    Log.w(TAG, "服务端消息到达，但客户端服务未绑定,消息将丢失...");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @WorkerThread
    @Override
    public void notifyTimeOutRelease(int id) {
        //操作成功，清除超时重传事件
        if (eventTimingWheel != null) {
            eventTimingWheel.remove(id);
        }
        if (messageStore != null) {
            messageStore.discardArrived(clientHandle, id + "");
        }
    }



    /**
     * 处理发送超时的消息
     *
     * @param expiredObject
     */
    @WorkerThread
    @Override
    public void expired(Event<MqttSenderAction> expiredObject) {
        int repeatCount = expiredObject.getRepeatCount();
        if (repeatCount > 0) {
            expiredObject.setRepeatCount(--repeatCount);
            if (eventTimingWheel != null) {
                IMQTTMessage message = expiredObject.getAction().getMessage();
                if (message instanceof MQTTPublish) {
                    ((MQTTPublish) message).setDup();
                }
                //第一次发送时已经存储
                expiredObject.getAction().setPersistentDataAction(null);
                deliveryRetrayPost(expiredObject);
            }
        } else {
            //一定次数的超时重传过后，仍然失败，定义为超时传输错误
            discardMessage(expiredObject);
        }
    }

    private void discardMessage(Event<MqttSenderAction> expiredObject) {
        mqttQos.getIdentifierHelper().removeSentPackage(expiredObject.getId());
        notifyTimeOutRelease(expiredObject.getId());
        notifyOperationResult(false, expiredObject.getId(),null);
    }
    @WorkerThread
    @Override
    public void onNetConnected() {
        this.automaticReconnect = true;
        if (eventTimingWheel != null) {
            eventTimingWheel.setFreeze(false);
        }
        resumeConnectImmediately();
    }

    private void resumeConnectImmediately() {
        if(this.automaticReconnect && (this.connectState == STATE_NONE || this.connectState == STATE_CONNECTION_FAILED && connectOptions!=null && connectCallBack!=null)){
            connect(connectOptions, connectCallBack);
        }
    }

    @Override
    public void onNetLoss() {
        this.automaticReconnect = false;
        stopPin();
        if (eventTimingWheel != null) {
            eventTimingWheel.setFreeze(true);
        }
        dataWorkerSender.removeMessages(RECONNECT, reconnectAction);
        notifyConnectState(STATE_CONNECTION_FAILED);
    }


    @Override
    public boolean onScheduleRetray() {
        if (isServiceOutage()) {
            reconnect();
        }
        return true;
    }

    /**
     * 发送连接消息
     */
    private void connect() {
        if (socketWorer != null) {
            try {
                MQTTConnect connect = MQTTConnect.newInstance(
                        connectOptions.getClientId()
                        , connectOptions.getUserName()
                        , new String(connectOptions.getPassword())
                        , connectOptions.isCleanSession()
                        , connectOptions.getProtocalName());
                connect.setKeepAlive(connectOptions.getKeepAliveInterval());
                if (connectOptions.getWillMessage() == null) {
                    connect.setWillFlag(false);
                    connect.setWillTopic("");
                    connect.setWillMessage("");
                }
                sendMessage(connect);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发布消息
     *
     * @param topic
     * @param payload
     * @param qos
     */
    public int publish(String topic, byte[] payload, byte qos) {
        int identifier = mqttQos.getIdentifierHelper().getIdentifier();
        MQTTPublish publish = MQTTPublish.newInstance(topic, payload, qos, identifier);
        sendMessage(publish);
        mqttQos.getIdentifierHelper().addSentPackage(publish);
        return identifier;
    }

    /**
     * 服务器需保持这条消息，当有新增的订
     * 阅者时，再将这条消息发给新增的订阅者；如
     * 果RETAIN标志位为0，则不保持消息，也不
     * 用发给新增的订阅者。
     *
     * @param topic
     * @param payload
     * @param qos
     */
    public void publishRetain(String topic, byte[] payload, byte qos) {
        MQTTPublish publish = MQTTPublish.newInstance(topic, payload, qos);
        publish.setRetain(true);
        sendMessage(publish);
    }

    /**
     * 订阅消息
     *
     * @param topics
     * @param qoss
     */
    public int subscribe(String[] topics, byte[] qoss) {
        int identifier = mqttQos.getIdentifierHelper().getIdentifier();
        MQTTSubscribe subscribe = MQTTSubscribe.newInstance(topics, qoss, identifier);
        sendMessage(subscribe);
        mqttQos.getIdentifierHelper().addSentPackage(subscribe);
        return identifier;
    }

    /**
     * 取消订阅
     *
     * @param topicFilters
     */
    public int unSubscribe(String[] topicFilters) {
        int identifier = mqttQos.getIdentifierHelper().getIdentifier();
        MQTTUnsubscribe mqttUnsubscribe = MQTTUnsubscribe.newInstance(identifier, topicFilters);
        sendMessage(mqttUnsubscribe);
        mqttQos.getIdentifierHelper().addSentPackage(mqttUnsubscribe);
        return identifier;
    }

    /**
     * 发送任务
     *
     * @param task
     */
    private void sendTask(Runnable task) {
        if (socketWorer != null && socketWorer.isAlive() && dataWorkerSender != null) {
            dataWorkerSender.post(task);
        }
    }

    /**
     * 建立连接
     *
     * @param connectOptions
     * @param connectCallBack
     */
    public void connect(IMqttConnectOptions connectOptions, ConnectStateCallBack connectCallBack) {
        setConnectConfigure(connectOptions, connectCallBack);
        stopPin();
        resetSocketWorker();
        notifyConnectState(STATE_CONNECTING);
    }

    void setConnectConfigure(IMqttConnectOptions connectOptions, ConnectStateCallBack connectCallBack) {
        this.connectCallBack = connectCallBack;
        this.connectOptions = connectOptions;
        try {
            this.clientHandle = connectOptions.getClientId() + connectOptions.getServerURIs()[0];
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private synchronized void reconnect() {
        if (automaticReconnect) {
            final RetrayParam retrayParam = pinSender.getPinDetecter().getRetrayParam();
            if (!isReconnectActionRunning()) {
                if (retrayParam.getRepeatCount() > 0) {
                    retrayParam.repeatCountStepDown();
                    reconnectAction.setRepeatCount(retrayParam.repeatCountSteped());
                    reconnectAction.setConnectTask(new Runnable() {
                        @Override
                        public void run() {
                            connect(connectOptions, connectCallBack);
                        }
                    });
                    Message message = dataWorkerSender.obtainMessage();
                    message.what = RECONNECT;
                    message.obj = reconnectAction;
                    //延时重连
                    dataWorkerSender.sendMessageDelayed(message, retrayParam.reconnectTimeStepUp());
                } else {
                    retrayParam.reset();
                    Log.d(TAG, "尝试重新建立长连接" + retrayParam.getDefaultRepeatCount() + "次失败...");
                    if (scheduledRetray == null) {
                        scheduledRetray = new ScheduledRetray(this, this);

                    }
                    scheduledRetray.checkCondition(this);
                }
            }
        } else {
            Log.d(TAG, "链接不会重试");
        }
    }

    /**
     * 重连任务已经在进行
     *
     * @return
     */
    private boolean isReconnectActionRunning() {
        return dataWorkerSender.hasMessages(RECONNECT, reconnectAction);
    }


    /**
     * 断开长连接以及Pin
     *
     * @param wrapperTask 是否发到异步线程执行
     */
    public void disconnect(boolean wrapperTask) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (socketWorer != null) {
                    notifyConnectState(STATE_NONE);
                }
                cancelSocketWorker();
            }
        };
        if (wrapperTask) {
            sendTask(runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * 取消原来的长连接，建立新的长连接
     */
    private void resetSocketWorker() {
        cancelSocketWorker();
        try {
            startSocketWorker();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 建立长连接
     *
     * @throws RemoteException
     */
    private void startSocketWorker() throws RemoteException {
        socketWorer = new SocketWorker(connectOptions.getServerURIs()[0], connectOptions.getPorts()[0], connectOptions.getConnectionTimeout(), connectOptions.getReconnectCount(), this);
        socketWorer.setName(SOCKET_WORKER);
        socketWorer.start();
    }

    /**
     * 断开长连接，在断开前会发送断开连接消息
     */
    private void cancelSocketWorker() {
        if (socketWorer != null) {
            socketWorer.cancel(true);
            socketWorer = null;
        }
    }

    /**
     * 建立数据处理线程
     */
    private void startDataWorker() {
        dataWorker = new HandlerThread(DATA_WORKER);
        dataWorker.start();
        dataWorkerSender = new Handler(dataWorker.getLooper(), this);
        pinSender = new PinSender(this, dataWorkerSender, new Runnable() {
            @Override
            public void run() {
                sendMessage(MQTTPingreq.newInstance());
            }
        }, this);
    }

    /**
     * 停止数据处理线程
     */
    private void cancelDataWorker() {
        if (dataWorker != null) {
            dataWorkerSender.removeCallbacksAndMessages(null);
            dataWorkerSender = null;
            dataWorker.quit();
            dataWorker = null;
        }
    }

    /**
     * 启动pin
     */
    private void startPin() {
        pinSender.start(0);
    }

    /**
     * 停止pin
     */
    private void stopPin() {
        pinSender.stop();
    }


    /**
     * 是否重试
     *
     * @param tryReconnect
     */
    public void setTryReconnect(boolean tryReconnect) {
        if (connectOptions != null) {
            this.automaticReconnect = tryReconnect;
        }
    }

    /**
     * 服务是否可用
     *
     * @return
     */
    boolean isServiceOk() {
        if (!NetStatusUtil.isConnected(this) || isServiceOutage()) {
            notifyConnectState(STATE_CONNECTION_FAILED);
            return false;
        }
        return true;
    }


    /**
     * 超时重传
     *
     * @param event
     */
    private void deliveryRetrayPost(Event<MqttSenderAction> event) {
        if (event.getAction() == null) {
            throw new RuntimeException("事件任务不能为空");
        }
        sendTask(event.getAction());
        eventTimingWheel.add(event);
    }

    /**
     * 消息超时设置无论是qos1还是qos2，重
     * 传消息除了初始消息publish,subscribe,unsubcribe,
     * 其他交互消息依赖服务端的重传机制即可，在收到服务器的
     * 反馈消息即可删除超时消息
     *
     * @param msg
     * @return
     */
    private Event<MqttSenderAction> createEvent(IMQTTMessage msg) {
        Event<MqttSenderAction> event = null;
        if (msg instanceof MQTTSubscribe
                || msg instanceof MQTTUnsubscribe
                || (msg instanceof MQTTPublish && ((MQTTPublish) msg).getQoS() > AT_MOST_ONCE)) {
            try {
                event = new Event<>();
                MqttSenderAction mqttSenderAction = new MqttSenderAction(socketWorer, msg);
                if (!(msg instanceof Persistentable && ((Persistentable) msg).isPersistent())) {
                    mqttSenderAction.setPersistentDataAction(new PersistentDataAction(this, msg));
                }
                event.setAction(mqttSenderAction);
                event.setActionId(msg.getPackageIdentifier());
                event.setRepeatCount(MESSAGE_REPEAT_COUNT);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return event;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        startForeground(1000, new Notification());
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 重试间隔事件默认最小30秒
     */
    private void startTimingWheel() {
        if(eventTimingWheel==null){
            try {
                int messageSendTimeout = connectOptions.getMessageSendTimeout();
                MESSAGE_DELIVERRY_RETRAY_INTERVAL = Math.max(messageSendTimeout,MESSAGE_DELIVERRY_RETRAY_INTERVAL);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            eventTimingWheel = new TimingWheel<>(1, MESSAGE_DELIVERRY_RETRAY_INTERVAL, TimeUnit.SECONDS);
            eventTimingWheel.start();
            eventTimingWheel.addExpirationListener(this);
        }
    }

    private void stopTimingWheel() {
        if (eventTimingWheel != null) {
            eventTimingWheel.stop();
            eventTimingWheel.removeExpirationListener(this);
            eventTimingWheel = null;
        }
    }

    public void setMode(int mode) {
        //切换前台，网络正常，非连接状态，将尝试重连一次
        if (isServiceOutage()) {
            if (mode == ACTIVE_MODE) {
                //固定为活跃态
                pinSender.getPinDetecter().changeEvent(ACTIVE_MODE);
                reconnect();
            }
        } else {
            pinSender.getPinDetecter().changeEvent(mode);
        }
    }

    /**
     * 服务器宕机情况
     *
     * @return
     */
    private boolean isServiceOutage() {
        return NetStatusUtil.isConnected(this) && connectState != STATE_CONNECTED && connectState != STATE_CONNECTING && connectOptions != null;
    }

    MessageStore getMessageStore() {
        return messageStore;
    }

    String getClientHandle() {
        return clientHandle;
    }

    TimingWheel<Event<MqttSenderAction>> getEventTimingWheel() {
        return eventTimingWheel;
    }

    MqttQos getMqttQos() {
        return mqttQos;
    }

    /**
     * 数据恢复
     */
    private void resumeData() {
        if (dataWorkerSender != null) {
            RestoreDataAction restoreDataAction = new RestoreDataAction(this);
            sendTask(restoreDataAction);
        }
    }


}

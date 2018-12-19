package com.jd.im.client;

import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;

import com.jd.im.ConnectStateCallBack;
import com.jd.im.IMLocalService;
import com.jd.im.IMRemoteService;
import com.jd.im.IMqttConnectOptions;
import com.jd.im.IVariableHeaderExtraPart;
import com.jd.im.converter.Converter;
import com.jd.im.converter.StringConverterFactory;
import com.jd.im.mqtt.MQTTConnectException;
import com.jd.im.mqtt.PublishHeaderExtraPart;
import com.jd.im.mqtt.MQTTException;
import com.jd.im.service.MqttService;
import com.jd.im.utils.Log;

import static com.jd.im.client.Task.SERIALIZE_REQUEST;
import static com.jd.im.mqtt.MQTTConnectionConstants.STATE_CONNECTED;
import static com.jd.im.mqtt.MQTTConnectionConstants.STATE_CONNECTING;
import static com.jd.im.mqtt.MQTTConnectionConstants.STATE_CONNECTION_FAILED;
import static com.jd.im.mqtt.MQTTConnectionConstants.STATE_NONE;
import static com.jd.im.mqtt.MQTTConstants.DISCONNECT;
import static com.jd.im.mqtt.MQTTConstants.PUBLISH;
import static com.jd.im.mqtt.MQTTConstants.QOS_1;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/27
 * Author:wangkai
 * Description:mqtt客户端
 * =====================================================
 */
public class MqttClient extends ConnectStateCallBack.Stub implements ServiceConnection, IMLocalServiceImpl.ClientReceiver, ModeListener.ICallBack {
    private static String TAG = "MqttClient";
    private static MqttClient instance;
    private  boolean openLog;
    private Context app;
    /**
     * 本地服务
     */
    private IMLocalService imLocalService;
    /**
     * 当前连接状态
     */
    private volatile int connectState = STATE_NONE;
    /**
     * 远程服务
     */
    private IMRemoteService imRemoteService;
    /**
     * 连接监听回调
     */
    private ConnectCallBack connectCallBack;
    /**
     * 初始化
     */
    private boolean inited;
    /**
     * 数据转换
     */
    private Converter.Factory dataConverter;
    /**
     * 回调管理器
     */
    private TokenManager tokenManager;
    /**
     * 消息质量等级
     */
    private byte defaultQos;
    /**
     * 等待服务启动
     */
    private boolean waitForConnect = false;
    /**
     * 连接配置
     */
    private IMqttConnectOptions cachedMqttConnectOptions;

    /**
     * 推送消息回调
     */
    private PushCallBack callBack;
    private ModeListener modeListener;
    private boolean isBind;


    private MqttClient(Context context, Converter.Factory dataConverter, byte qos, boolean openLog, PushCallBack pushCallBack, OutputCallBack defaultOperateCallBack) {
        if (!inited) {
            inited = true;
            this.dataConverter = dataConverter;
            this.defaultQos = qos;
            this.callBack = pushCallBack;
            this.app = context.getApplicationContext();
            this.openLog = openLog;
            tokenManager = new TokenManager();
            tokenManager.setDefaultCallBack(defaultOperateCallBack);
            modeListener = new ModeListener((Application) app, this);
        }
    }

    /**
     * 该实例只能初始化一次，后续获取均沿用第一次创建对象
     * 注意：多进程将导致单例失效，因此在一个进程中实例化
     * 只能在该进程中使用
     *
     * @param context 上下文对象，第一次创建不能为空
     * @return
     */
    public static MqttClient getInstance(Context context) {
        if (instance == null) {
            instance = new Builder().context(context).build();
        }
        return instance;
    }

    /**
     * 初始化过对象后可使用此方法
     *
     * @return
     */
    public static MqttClient getInstance() {
        if (instance == null) {
            throw new RuntimeException("对象未初始化");
        }
        return instance;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "remoteService connected");
        try {
            if (imLocalService == null) {
                imLocalService = new IMLocalServiceImpl(this, dataConverter);
            }
            imRemoteService = IMRemoteService.Stub.asInterface(service);

            if (imRemoteService == null) return;
            if (imRemoteService != null) {
                //绑定本地服务
                imRemoteService.bindLocalService(imLocalService);
                isBind = true;
                if(openLog){
                    Log.openLog();
                    imRemoteService.openLog();
                }
            }
            tryReconnect();

        } catch (Exception e) {
            imRemoteService = null;
            isBind = false;
        }
    }

    private void tryReconnect() {
        if (waitForConnect && cachedMqttConnectOptions != null) {
            waitForConnect = false;
            connectState = STATE_NONE;
            connect(cachedMqttConnectOptions);
        }
    }

    private void restartService() {
        waitService(cachedMqttConnectOptions);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        isBind = false;
        //如果在初始连接或者连接中断开，重试重启
        if (connectState == STATE_CONNECTING || connectState == STATE_CONNECTED) {
            restartService();
            Log.d(TAG, " 服务断开,重新绑定服务...");
        } else {
            if (imRemoteService != null) {
                imRemoteService = null;
            }
            try {
                onConnectStateChange(STATE_NONE);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.d(TAG, " remoteService disconnected");
        }
    }


    /**
     * 检验后台service是否在运行
     *
     * @return
     */
    private boolean checkServiceActive() {
        try {
            if (imRemoteService == null) {
                Log.d(TAG, "try to bind remote  remoteService");
                Intent intent = new Intent(app, MqttService.class);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    app.startForegroundService(intent);
//                } else {
//                    app.startService(intent);
//                }
                app.startService(intent);
                if (!app.bindService(intent, this, Service.BIND_AUTO_CREATE)) {
                    Log.e(TAG, "remote  remoteService bind failed");
                }
                return false;
            }
            Log.d(TAG, "remote  remoteService bind success");
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 建立连接
     *
     * @param mqttConnectOptions 连接配置
     * @param connectCallBack    连接回调，反映连接在连接过程中的变化
     */
    public void connect(IMqttConnectOptions mqttConnectOptions, ConnectCallBack connectCallBack) {
        this.connectCallBack = connectCallBack;
        connect(mqttConnectOptions);
    }

    private void connect(IMqttConnectOptions mqttConnectOptions) {

        if (connectState == STATE_NONE || connectState == STATE_CONNECTION_FAILED) {
            try {
                if (imRemoteService != null) {
                    Log.d(TAG, "发起连接...");
                    imRemoteService.connect(mqttConnectOptions, this);
                } else {
                    Log.d(TAG, "服务未启动，启动服务...");
                    waitService(mqttConnectOptions);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (imRemoteService == null) {
                connectState = STATE_NONE;
                connect(cachedMqttConnectOptions);
            } else if (connectState == STATE_CONNECTING) {
                Log.d(TAG, "已经处于连接中，请勿重复连接...");
            } else if (connectState == STATE_CONNECTED) {
                Log.d(TAG, "已经处于连接状态，请勿重复连接...");
            }
        }
    }

    private void waitService(IMqttConnectOptions mqttConnectOptions) {
        this.waitForConnect = true;
        this.cachedMqttConnectOptions = mqttConnectOptions;
        checkServiceActive();
    }

    /**
     * 断开长连接
     *
     * @param tryReconnect 是否自动重连
     */
    private void disconnect(boolean tryReconnect) {
        if (connectState == STATE_CONNECTED) {
            if (imRemoteService != null) {

                try {
                    imRemoteService.disConnect(tryReconnect);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.e(TAG, "服务未连接");
        }
        if(!tryReconnect){
            try {
                if(isBind){
                    if(imRemoteService!=null){
                        imRemoteService.unBindLocalService();
                    }
                    this.app.unbindService(this);
                    isBind = false;
                }
                Intent intent = new Intent(app, MqttService.class);
                this.app.stopService(intent);
                imRemoteService = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭长连接
     * 注意：再次使用长连接只能由开发者手动调用连接connect,不会重试链接
     */
    public void disconnect() {
        disconnect(false);
    }

    public boolean isConnected() {
        boolean connect = imRemoteService != null && connectState == STATE_CONNECTED;
        if (!connect) {
            Log.e(TAG, "服务未绑定或者未连接...");
        }
        return connect;
    }

    private void saveCallBack(int identifier, MessageCallBack messageCallBack) {
        if (messageCallBack != null && tokenManager!=null) {
            tokenManager.put(identifier, messageCallBack);
        }
    }

    public void subscribe(String[] topics, byte[] qoss, MessageCallBack messageCallBack) {
        if (isConnected()) {
            if(imRemoteService!=null){
                try {
                    int id = imRemoteService.subscribe(topics, qoss);
                    saveCallBack(id, messageCallBack);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }else {
            if(messageCallBack!=null){
                messageCallBack.onFailed(new MQTTConnectException("not connect!"));
            }
        }
    }

    public void subscribe(String topic) {
        subscribe(new String[]{topic}, new byte[]{defaultQos}, null);
    }

    public void subscribe(String topic, MessageCallBack messageCallBack) {
        subscribe(new String[]{topic}, new byte[]{defaultQos}, messageCallBack);
    }

    public void subscribe(String[] topics, MessageCallBack messageCallBack) {
        byte[] qoss = new byte[topics.length];
        for (int i = 0; i < qoss.length; i++) {
            qoss[i] = defaultQos;
        }
        subscribe(topics, qoss, messageCallBack);
    }

    public void subscribe(String[] topics) {
        subscribe(topics, null);
    }


    public void unSubscribe(String topic, MessageCallBack messageCallBack) {
        unSubscribe(new String[]{topic}, messageCallBack);
    }

    public void unSubscribe(String topic) {
        unSubscribe(new String[]{topic}, null);
    }

    public void unSubscribe(String[] topicFilters) {
        unSubscribe(topicFilters, null);
    }

    public void unSubscribe(String[] topicFilters, MessageCallBack messageCallBack) {
        if (isConnected()) {
            try {
                int identifier = imRemoteService.unSubscribe(topicFilters);
                saveCallBack(identifier, messageCallBack);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            if(messageCallBack!=null){
                messageCallBack.onFailed(new MQTTException("not connect!"));
            }
        }
    }


    private void publish(String topic, byte[] payload, byte qos,  MessageCallBack messageCallBack) {
        publish(topic,payload,qos,false,messageCallBack);
    }
    private void publish(String topic, byte[] payload, byte qos,boolean retain,  MessageCallBack messageCallBack) {
        if (isConnected()) {
            try {
                int identifier = imRemoteService.publish(topic, payload, qos,retain);
                if(identifier<0 && messageCallBack!=null){
                    messageCallBack.onFailed(new MQTTConnectException("identifier is "+identifier));
                }else {
                    saveCallBack(identifier, messageCallBack);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            if(messageCallBack!=null){
                messageCallBack.onFailed(new MQTTConnectException("not connect!"));
            }
        }
    }


    /**
     * 发布消息
     *
     * @param topic           主题
     * @param payload         结构化内容
     * @param messageCallBack 回调
     * @param <T>
     */
    public <T> void publish(String topic, T payload, MessageCallBack messageCallBack) {
        if (imLocalService instanceof IMLocalService) {
            ((IMLocalServiceImpl) imLocalService).enqueueTask(Task.create(SERIALIZE_REQUEST, Publish.create(topic, payload,3,1, messageCallBack)));
        }
    }

    /**
     * 消息回执，用于某些情况下，应答服务器
     * @param topic
     * @param payload
     * @param messageCallBack
     * @param <T>
     */
    public <T> void publishCallBack(String topic, T payload, MessageCallBack messageCallBack){
        if (imLocalService instanceof IMLocalService) {
            ((IMLocalServiceImpl) imLocalService).enqueueTask(Task.create(SERIALIZE_REQUEST, Publish.create(topic, payload,3,2, messageCallBack)));
        }
    }

    public <T> void publishCallBack(String topic, T payload){
        publishCallBack(topic,payload,null);
    }

    public <T> void publish(String topic, T payload) {
        publish(topic, payload, null);
    }


    @Override
    public void onConnectStateChange(int state) throws RemoteException {
        this.connectState = state;
        if (connectCallBack != null) {
            switch (state) {
                case STATE_CONNECTION_FAILED:
                case STATE_NONE:
                    MQTTException mqttException = new MQTTException("连接失败...");
                    Log.d(TAG, "connect state change:" + mqttException.getMessage());
                    connectCallBack.onConnectLoss(mqttException);
                    notifyMessageCallbacksFailed(mqttException);
                    break;
                case STATE_CONNECTED:
                    Log.d(TAG, "connect state change:" + "连接验证成功...");
                    connectCallBack.onConnectSuccess();
                    break;
                case STATE_CONNECTING:
                    Log.d(TAG, "connect state change:" + "正在连接...");
                    break;
                default:
                    break;
            }
        }
    }

    private void notifyMessageCallbacksFailed(MQTTException mqttException) {
        if (tokenManager != null) {
            tokenManager.notifyAllFailed(mqttException);
        }
    }


    /**
     * 服务端推送消息
     *
     * @param messageType
     * @param message
     */
    @WorkerThread
    @Override
    public void onPushArrived(int messageType, Object message) {
        Log.d(TAG, "a message has arrived : \n" + message.toString());
        if (callBack != null && messageType == PUBLISH) {
            callBack.onPush(message);
        }else if(connectCallBack!=null && messageType == DISCONNECT){
            disconnect();
            connectCallBack.onKickOff(message);
        }
    }

    /**
     * 客户端操作结果回调，主线程
     *  @param success
     * @param id
     * @param extraInfor
     */
    @MainThread
    @Override
    public void onOperationCallBack(boolean success, int id, Object extraInfor) {
        if(tokenManager!=null){
            MessageCallBack messageCallBack = tokenManager.get(id);
            if(messageCallBack!=null){
                if (success) {
                    messageCallBack.onSuccess(extraInfor);
                } else {
                    messageCallBack.onFailed(new MQTTException("连接断开或操作超时..."));
                }
            }
            tokenManager.remove(id);
        }
    }

    /**
     * 序列化请求数据
     *
     * @param publish
     */
    @WorkerThread
    @Override
    public void onRequestSerialized(Publish publish) {
//        PublishHeaderExtraPart publishHeaderExtraPart = new PublishHeaderExtraPart(publish.getCodingType(), publish.getMessageType());
        publish(publish.getTopic(), (byte[]) publish.getPayload(), defaultQos, publish.getMessageCallBack());
    }

    /**
     * 释放资源
     */
    public void onDestroy() {
        disconnect();
        tokenManager.setDefaultCallBack(null);
        tokenManager = null;
        app = null;
        instance = null;
        modeListener = null;
    }

    @Override
    public void notifyAppMode(int mode) {
        if (imRemoteService != null && isBind) {
            try {
                if(imRemoteService.asBinder().isBinderAlive()){
                    imRemoteService.modeEvent(mode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.w(TAG, "服务未绑定,远程模式切换失败...");
        }
    }

    public interface PushCallBack<T> {
        void onPush(T message);
    }

    public static class Builder {
        private Context app;
        private Converter.Factory dataConverter = new StringConverterFactory();
        private byte qos = QOS_1;
        private PushCallBack pushCallBack;
        private boolean openLog;
        private OutputCallBack defaultOperateCallBack;

        public Builder context(Context context) {
            this.app = context;
            return this;
        }

        public Builder converter(Converter.Factory dataConverter) {
            this.dataConverter = dataConverter;
            return this;
        }
        public Builder defaultOperateCallBack(OutputCallBack defaultCallBack){
            this.defaultOperateCallBack = defaultCallBack;
            return this;
        }


        public Builder qos(byte qos) {
            this.qos = qos;
            return this;
        }

        public <T> Builder pushCallBack(PushCallBack<T> callBack) {
            this.pushCallBack = callBack;
            return this;
        }
        public Builder openLog(){
            this.openLog = true;
            return this;
        }

        public MqttClient build() {
            if (app == null) {
                throw new RuntimeException("context has to be set!");
            }
            if (instance == null) {
                instance = new MqttClient(app, dataConverter, qos,openLog, pushCallBack,defaultOperateCallBack);
            }
            return instance;
        }
    }
}

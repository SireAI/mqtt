package com.jd.jrapp.bm.message.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.jd.im.client.ConnectCallBack;
import com.jd.im.client.MessageCallBack;
import com.jd.im.client.MessageResult;
import com.jd.im.client.MqttClient;
import com.jd.im.client.OutputCallBack;
import com.jd.im.converter.ProtobufferConverterFactory;
import com.jd.im.message.disconnect.nano.DisconnectMessage;
import com.jd.im.message.nano.Ack;
import com.jd.im.message.nano.ClientPublishMessage;
import com.jd.im.message.nano.DeleteMessage;
import com.jd.im.mqtt.MQTTException;
import com.jd.im.mqtt.MQTTVersion;
import com.jd.im.mqtt.MqttConnectOptions;
import com.jd.im.utils.Log;
import com.jd.jrapp.bm.message.bean.Talker;
import com.jd.jrapp.bm.message.constant.Constant;
import com.jd.jrapp.bm.message.model.MessageModel;

import java.util.UUID;

import static com.jd.im.mqtt.MQTTConstants.QOS_1;
import static com.jd.jrapp.bm.message.constant.Constant.CONNECT_NAME;
import static com.jd.jrapp.bm.message.constant.Constant.CONNECT_PASSWORD;
import static com.jd.jrapp.bm.message.constant.Constant.TALKER;

public class JDMqttService extends Service implements MqttClient.PushCallBack<ClientPublishMessage.MessageResponse>,OutputCallBack<Ack.MessageAck> {
    private static final String TAG = "JDMqttService";
    public static final int KEEP_ALIVE_INTERVAL = 8 * 60000;
    //
//    final String serverUri = "tcp://iot.eclipse.org:1883";
    //测试环境地址
//    final String serverUri = "tcp://172.25.47.19:8183";
//    final String serverUri = "tcp://10.13.82.243:8183";
//    final String serverUri = "tcp://pnsmqtt-server.jdpay.com:8183";
    //外网可访问
//    final String serverUri = "tcp://59.151.64.31:8935";
    final String serverUri = "tcp://10.13.81.148:8183";
    final String subscriptionTopic = "AndroidTopic";
    //    final String subscriptionTopic = "RT";
    final String publishTopic = "AndroidPublishTopic";
    final String publishMessage = "Hello World!";
    ////
    String clientId = "PC;version=2.0.1.0430;uuid=0a092ds99a012897dbc";
    private MqttClient mqttClient;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(startId, new Notification());
        if(intent != null) {
            String connectName = intent.getStringExtra(CONNECT_NAME);
            String connectPassword = intent.getStringExtra(CONNECT_PASSWORD);
            Talker talker = intent.getParcelableExtra(TALKER);
            if (!TextUtils.isEmpty(connectName)) {
                MessageModel.getInstance().setUserName(connectName);
            }
            if (!TextUtils.isEmpty(connectPassword)) {
                MessageModel.getInstance().setPassword(connectPassword);
            }
            if (talker != null) {
                MessageModel.getInstance().setTalker(talker);
            }
            if (mqttClient != null) {
                if (!mqttClient.isConnected()) {
                    connect();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * 服务启动入口
     * @param context 上下文
     * @param connectName  连接验证名
     * @param connectPassword  连接验证密码
     * @param userId 用户唯一id
     * @param nickName  用户昵称
     * @param userImg  用户头像地址
     */
    public static void start(Context context,String connectName, String connectPassword, String userId, String nickName, String userImg){
        Intent intent = new Intent(context, JDMqttService.class);
        intent.putExtra(CONNECT_NAME,connectName);
        intent.putExtra(CONNECT_PASSWORD,connectPassword);
        Talker talker = new Talker();
        talker.setUserName(nickName);
        talker.setUserImg(userImg);
        talker.setUserId(userId);
        intent.putExtra(TALKER,talker);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }

    }

    @Override
    public void onCreate() {
        mqttClient = new MqttClient.Builder()
                .context(this)
//                .converter(ProtobufferConverterFactory.create()
//                        .bindPublish(ClientPublishMessage.MessageResponse.class)
//                        .bindDisconnect(DisconnectMessage.Disconnect.class)
//                        .bindAck(Ack.MessageAck.class))
                .pushCallBack(this)
                .defaultOperateCallBack(this)
                .qos(QOS_1)
                .openLog()
                .build();
        MessageModel.getInstance().init(this);
    }
    private void connect() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions()
                .setAutomaticReconnect(true)
                .setCleanSession(false)
                .setClientId(UUID.randomUUID().toString())
                .setKeepAliveInterval(KEEP_ALIVE_INTERVAL)
                .setServerURIs(new String[]{serverUri})
                .setUserName(MessageModel.getInstance().getUserName())
                .setPassword(MessageModel.getInstance().getPassword())
                .setProtocalName(MQTTVersion.VERSION_311)
                ;
        mqttClient.connect(mqttConnectOptions, new ConnectCallBack<DisconnectMessage.Disconnect>() {


            @Override
            public  void onKickOff(DisconnectMessage.Disconnect kickInfor) {
                Log.d(TAG,"被踢下线："+kickInfor.toString());
            }

            @Override
            public void onConnectSuccess() {
                subscribeTopic();
                Log.d(TAG,"连接成功");

            }

            @Override
            public void onConnectLoss(MQTTException excetion) {
                Log.d(TAG,"连接失败");
            }
        });

    }

    private void subscribeTopic() {
        mqttClient.subscribe(Constant.TOPIC_SF, new MessageResult() {
            @Override
            public void onSuccess() {
                Log.d(TAG,"订阅成功");
            }

            @Override
            public void onFailed(MQTTException exception) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mqttClient!=null){
            mqttClient.disconnect();
            mqttClient.onDestroy();
        }
    }

    /**
     * 一条消息的处理周期，从序列化到入库
     * @param message
     */
    @WorkerThread
    @Override
    public void onPush(ClientPublishMessage.MessageResponse message) {
        for (int i = 0; i < message.messages.length; i++) {
            ClientPublishMessage.Message pushMessage = message.messages[i];
            MessageModel.getInstance().onReceiveMessage(pushMessage,pushMessage.time);
            notifyServerDeleteMessage(pushMessage);
        }
    }

    /**
     * 入库即通知服务器删除消息
     * @param pushMessage
     */
    private void notifyServerDeleteMessage(ClientPublishMessage.Message pushMessage) {
        if(mqttClient!=null){
            DeleteMessage.SendMessageReport sendMessageReport = new DeleteMessage.SendMessageReport();
            sendMessageReport.receivedMsgId = pushMessage.msgId;
            //兼容realtime
            mqttClient.publishCallBack(Constant.TOPIC_OFF_LINE, sendMessageReport, new MessageCallBack() {
                @Override
                public void onSuccess(Object extraInfor) {
                    Log.d(TAG,"通知服务器删除成功");
                }

                @Override
                public void onFailed(MQTTException exception) {
                    Log.d(TAG,"通知服务器删除失败");

                }
            });
        }
    }

    /**
     * 处理丢失回调或者没有回调的PUBLISH消息
     * @param messageAck
     */
    @MainThread
    @Override
    public void onSuccess(Ack.MessageAck messageAck) {
        if(messageAck!=null){
            MessageModel.getInstance().updateIMMessageSuccess(messageAck.msgId+"");
        }
    }
}

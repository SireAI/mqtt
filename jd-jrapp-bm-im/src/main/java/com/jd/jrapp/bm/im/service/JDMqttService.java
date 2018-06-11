package com.jd.jrapp.bm.im.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.jd.im.client.ConnectCallBack;
import com.jd.im.client.MqttClient;
import com.jd.im.converter.ProtobufferConverterFactory;
import com.jd.im.message.disconnect.nano.DisconnectMessage;
import com.jd.im.message.nano.ClientPublishMessage;
import com.jd.im.mqtt.IMProtocalExtraPart;
import com.jd.im.mqtt.MQTTException;
import com.jd.im.mqtt.MQTTVersion;
import com.jd.im.mqtt.MqttConnectOptions;
import com.jd.im.utils.Log;

import static com.jd.im.mqtt.MQTTConstants.QOS_2;

public class JDMqttService extends Service implements MqttClient.PushCallBack<ClientPublishMessage.MessageResponse> {
    private static final String TAG = "JDMqttService";
    //
//    final String serverUri = "tcp://iot.eclipse.org:1883";
    //测试环境地址
    final String serverUri = "tcp://172.25.47.19:8183";
    //外网可访问
//    final String serverUri = "tcp://59.151.64.31:8935";
//    final String serverUri = "tcp://10.13.80.235:8183";
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
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        mqttClient = new MqttClient.Builder()
                .context(this)
                .converter(ProtobufferConverterFactory.create()
                        .bindPublish(ClientPublishMessage.MessageResponse.class)
                        .bindDisconnect(DisconnectMessage.Disconnect.class))//默认的数据解析方式为String
                .pushCallBack(this)//服务端主动push数据回调
                .qos(QOS_2) //质量等级，默认是qos_1
                .openLog() //是否打开调试日志，建议正式版本关闭
                .build();
        connect();
    }
    private void connect() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions()
                .setAutomaticReconnect(true)//是否启用重连机制，默认是
                .setCleanSession(false)//是否清楚会话状态，默认否
                .setClientId(clientId)//客户端唯一标识
                .setKeepAliveInterval(8*60000)//服务端保持长连接最大时长
                // ，若超过则会主动断开连接，此值会影响心跳探测最大间隔，建议使用默认8分钟
                .setServerURIs(new String[]{serverUri})//服务器地址
                .setUserName("222")//账号
                .setPassword("sire")//密码
                .setProtocalName(MQTTVersion.VERSION_311)//协议名，默认VERSION_311
                .setProtocalName(MQTTVersion.VERSION_IM)
                .setExtraHeaderPart(new IMProtocalExtraPart())
                ;

        mqttClient.connect(mqttConnectOptions, new ConnectCallBack<DisconnectMessage.Disconnect>() {


            @Override
            public  void onKickOff(DisconnectMessage.Disconnect kickInfor) {
                Log.d(TAG,"被踢下线："+kickInfor.toString());
            }

            @Override
            public void onConnectSuccess() {
                Log.d(TAG,"连接成功=====");

            }

            @Override
            public void onConnectLoss(MQTTException excetion) {
                Log.d(TAG,"连接失败=====");
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mqttClient!=null){
            mqttClient.disconnect();
        }
    }

    @Override
    public void onPush(ClientPublishMessage.MessageResponse message) {

    }
}

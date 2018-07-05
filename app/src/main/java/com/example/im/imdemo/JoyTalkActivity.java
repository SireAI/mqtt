package com.example.im.imdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.jd.im.client.ConnectCallBack;
import com.jd.im.client.MessageCallBack;
import com.jd.im.client.MessageResult;
import com.jd.im.client.MqttClient;
import com.jd.im.converter.ProtobufferConverterFactory;

import com.jd.im.message.disconnect.nano.DisconnectMessage;
import com.jd.im.message.nano.Ack;
import com.jd.im.message.nano.ClientPublishMessage;
import com.jd.im.message.nano.ImageMessage;
import com.jd.im.mqtt.MQTTException;
import com.jd.im.mqtt.MQTTVersion;
import com.jd.im.mqtt.MqttConnectOptions;
import com.jd.jrapp.bm.message.bean.Talker;
import com.jd.jrapp.bm.message.controller.IMMessageController;
import com.jd.jrapp.bm.message.service.JDMqttService;
import com.jd.jrapp.bm.message.utils.CommonUtils;

import java.io.File;
import java.util.ArrayList;


import paho.mqtt.java.example.R;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static com.jd.im.message.nano.ClientPublishMessage.CHAT;
import static com.jd.im.mqtt.MQTTConstants.QOS_1;
import static com.jd.jrapp.bm.message.constant.Constant.PEER_TALKER;

public class JoyTalkActivity extends FragmentActivity implements MqttClient.PushCallBack<Object> {
    private static final String TAG = "JoyTalkActivity";
    //
    final String serverUri = "tcp://iot.eclipse.org:1883";
    //测试环境地址
//    final String serverUri = "tcp://172.25.47.19:8183";
    //外网可访问
//    final String serverUri = "tcp://59.151.64.31:8935";
//    final String serverUri = "tcp://10.13.80.235:8183";
    final String subscriptionTopic = "AndroidTopic";
    //    final String subscriptionTopic = "RT";
    final String publishTopic = "AndroidPublishTopic";
    final String publishMessage = "Hello World!";
    ////
    String clientId = "PC;version=2.0.1.0430;uuid=0a092ds99a012897dbc";

    private HistoryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        android.support.v7.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishMessage();
            }
        });

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.history_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new HistoryAdapter(new ArrayList<String>());
        mRecyclerView.setAdapter(mAdapter);

        if (false) {
            connect();
        } else {
            Intent intent = new Intent(this, JDMqttService.class);
            if(isMonitor){
                JDMqttService.start(this,"1","sire","1","sire","http://www.lagou.com/image2/M00/01/5C/CgqLKVXj66GAaM2cAAFq682mH60142.png");
            }else {
                JDMqttService.start(this,"2","jhon","2","jhon","http://screen.uimg.cn/cc/9b/cc9b7ec9af34a15bd6f94190e8519fc4.jpg");
            }
        }

//        testUpload();
    }

    private void testUpload() {
        for (int i = 0; i < 10; i++) {
            File externalStorageDirectory = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
            System.out.println("-----"+externalStorageDirectory.getPath()+"/test.png");
//            UploadTask uploadTask = new UploadTask(UUIDUtils.generateSingleId());
//            uploadTask.setUploadInfor(new UploadInfor(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath()+"/test.png","test",30+"","image"));
//            UploadManager.getInstance().addUploadTask(uploadTask);
        }

    }

    private void connect() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions()
                .setAutomaticReconnect(true)//是否启用重连机制，默认是
                .setCleanSession(false)//是否清楚会话状态，默认否
                .setClientId(clientId)//客户端唯一标识
                .setKeepAliveInterval(8 * 60000)//服务端保持长连接最大时长
                // ，若超过则会主动断开连接，此值会影响心跳探测最大间隔，建议使用默认8分钟
                .setServerURIs(new String[]{serverUri})//服务器地址
                .setUserName("222")//账号
                .setPassword("sire")//密码
                .setProtocalName(MQTTVersion.VERSION_311)//协议名，默认VERSION_311
//                .setProtocalName(MQTTVersion.VERSION_IM)
//                .setExtraHeaderPart(new PublishHeaderExtraPart())
                ;
        MqttClient mqttClient = new MqttClient.Builder()
                .context(this)
                .converter(ProtobufferConverterFactory.create()
                        .bindPublish(ClientPublishMessage.MessageResponse.class)
                        .bindDisconnect(DisconnectMessage.Disconnect.class).bindAck(Ack.MessageAck.class))//默认的数据解析方式为String
                .pushCallBack(this)//服务端主动push数据回调
                .qos(QOS_1) //质量等级，默认是qos_1
                .openLog() //是否打开调试日志，建议正式版本关闭
                .build();
        mqttClient.connect(mqttConnectOptions, new ConnectCallBack<DisconnectMessage.Disconnect>() {


            @Override
            public void onKickOff(DisconnectMessage.Disconnect kickInfor) {
                //被踢下线
            }

            @Override
            public void onConnectSuccess() {
                //连接成功
            }

            @Override
            public void onConnectLoss(MQTTException excetion) {
                //连接失败
            }
        });

    }


    private void addToHistory(final String mainText) {
        System.out.println("LOG: " + mainText);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.add(mainText);
                Snackbar.make(findViewById(android.R.id.content), mainText, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


    }


    public void subscribeToTopic() {
        try {

//            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    addToHistory("Subscribed!");
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    addToHistory("Failed to subscribe");
//                }
//            });
//
//            // THIS DOES NOT WORK!
//            mqttAndroidClient.subscribe(subscriptionTopic, 0, new IMqttMessageListener() {
//                @Override
//                public void messageArrived(String TOPIC_REAL_TIME, MqttMessage message) throws Exception {
//                    // message Arrived!
//                    addToHistory(new String(message.getPayload()));
//                    System.out.println("Message: " + TOPIC_REAL_TIME + " : " + new String(message.getPayload()));
//                }
//            });

        } catch (Exception ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public void publishMessage() {
//        connect();
//        publish();
//        disconnect();
//        subscribe();
//        unSubscribe();//有问题，订阅无法取消
//        publishImage();
        gotoIm();
        try {
//            MqttMessage message = new MqttMessage();
//            message.setPayload(publishMessage.getBytes());
//            mqttAndroidClient.publish(publishTopic, message);
//            addToHistory("Message Published");
//            if(!mqttAndroidClient.isConnected()){
//                addToHistory(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
//            }
        } catch (Exception e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private boolean isMonitor = false;
    private void gotoIm() {
        Intent intent = new Intent(this, IMMessageController.class);
        Talker peerTalker = new Talker();
        if(isMonitor){
            peerTalker.setUserName("jhon");
            peerTalker.setUserId("2");
            peerTalker.setUserImg("http://screen.uimg.cn/cc/9b/cc9b7ec9af34a15bd6f94190e8519fc4.jpg");
            String toClientId = "ANDROID;version="+ CommonUtils.getVersionName(this)+";uuid=id2dc912db033142beae65916699e00043";
            peerTalker.setClientId(toClientId);
        }else {
            peerTalker.setUserName("sire");
            peerTalker.setUserId("1");
            peerTalker.setUserImg("http://www.lagou.com/image2/M00/01/5C/CgqLKVXj66GAaM2cAAFq682mH60142.png");
            String toClientId = "ANDROID;version="+CommonUtils.getVersionName(this)+";uuid=ida25dd820617d4549bad806ac5e1f3645";
            peerTalker.setClientId(toClientId);
        }
        intent.putExtra(PEER_TALKER,peerTalker);
        startActivity(intent);
    }

    private void unSubscribe() {
        MqttClient.getInstance(this).unSubscribe(subscriptionTopic, new MessageResult() {
            @Override
            public void onSuccess() {
                System.out.println("=========取消订阅成功");
            }

            @Override
            public void onFailed(MQTTException exception) {
                System.out.println("==============onFailed:" + exception.getMessage());

            }
        });
    }

    private void subscribe() {
        MqttClient.getInstance(this).subscribe(subscriptionTopic, new MessageResult() {
            @Override
            public void onSuccess() {
                System.out.println("订阅" + subscriptionTopic + "成功。。");
            }

            @Override
            public void onFailed(MQTTException exception) {
                System.out.println("==============onFailed:" + exception.getMessage());
            }
        });
    }

    private void disconnect() {
        MqttClient.getInstance(this).disconnect();
    }

    private void publish() {
        //uidL:xxx   与用户相同
        String contentStr = "你好世界";
        ClientPublishMessage.Content content = new ClientPublishMessage.Content();
        content.uuid = "222222222222";
        content.fromUri = "pin:222";
        content.fromEpid = clientId;
        content.fromNickname = "sire";
        content.contentType = "text/plain";
        content.contentBuffer = contentStr.getBytes();
        ClientPublishMessage.Receiver receiver = new ClientPublishMessage.Receiver();
        receiver.toUri = "pin:111111";
        receiver.toEpid = "PC;version=2.0.1.0430;uuid=111111";
        receiver.exceptThisEpid = false;
        ClientPublishMessage.MessageRequst messageRequst = new ClientPublishMessage.MessageRequst();
        messageRequst.messageType = CHAT;
        messageRequst.options = "1";
        messageRequst.content = content;
        messageRequst.receivers = new ClientPublishMessage.Receiver[]{receiver};
        messageRequst.peerUri = "pin:111111";
        messageRequst.topic = "RT";

        String topic = "RT";
        Object payload = messageRequst;
//        String TOPIC_REAL_TIME = publishTopic;
//        Object payload = publishMessage;
        MqttClient.getInstance(this).publish(topic, payload, new MessageCallBack<Ack.MessageAck>() {


            @Override
            public void onSuccess(Ack.MessageAck extraInfor) {
                System.out.println("发布成功。。。" + (extraInfor != null ? extraInfor.toString() : ""));

            }

            @Override
            public void onFailed(MQTTException exception) {
                System.out.println("发布失败。。。");
                System.out.println("==============onFailed:" + exception.getMessage());

            }
        });
    }

    public void publishImage() {
        ImageMessage.Image image = new ImageMessage.Image();
        image.file = "423424.jpg";
        image.height = 200;
        image.width = 300;
        image.id = "3213131";
        image.qMQTTlity = 9;
        image.size = 3221424;
        image.text = "fasdfsaf";
        image.thumb = new byte[100];
        image.thumbLevel = 11;
        image.type = "32";
        image.url = "http://www.baidu.com";
        MqttClient.getInstance().publish("RT", image, new MessageCallBack<Ack.MessageAck>() {


            @Override
            public void onSuccess(Ack.MessageAck extraInfor) {
                System.out.println("图片消息发送成功===========" + (extraInfor != null ? extraInfor.toString() : ""));

            }

            @Override
            public void onFailed(MQTTException exception) {

            }
        });
    }


    @Override
    public void onPush(Object message) {

    }
}

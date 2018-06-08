package com.example.im.imdemo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.protobuf.ByteString;
import com.jd.im.client.MessageCallBack;
import com.jd.im.client.MqttClient;
import com.jd.im.converter.ProtobufferConverterFactory;
import com.jd.im.message.ClientPublishMessage;
import com.jd.im.mqtt.IMProtocalExtraPart;
import com.jd.im.mqtt.MQTTException;
import com.jd.im.mqtt.MQTTVersion;
import com.jd.im.mqtt.MqttConnectOptions;

import java.util.ArrayList;

import paho.mqtt.java.example.R;

import static com.jd.im.mqtt.MQTTConstants.QOS_2;

public class PahoExampleActivity extends AppCompatActivity implements MqttClient.PushCallBack<Object> {

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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


        connect();
    }

    private void connect() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions()
                .setAutomaticReconnect(true)
                .setCleanSession(false)
                .setClientId(clientId)
                .setKeepAliveInterval(60)
                .setServerURIs(new String[]{serverUri})
                .setUserName("222")
                .setPassword("sire")
                .setProtocalName(MQTTVersion.VERSION_311)
//                .setProtocalName(MQTTVersion.VERSION_IM)
//                .setExtraHeaderPart(new IMProtocalExtraPart())
                ;
        MqttClient mqttClient = new MqttClient.Builder()
                .context(this)
                .converter(ProtobufferConverterFactory.create())
                .pushCallBack(this)
                .qos(QOS_2)
                .openLog()
                .build();
        mqttClient.connect(mqttConnectOptions, new MessageCallBack() {
            @Override
            public void onSuccess() {
                System.out.println("==============onSuccess");
//                subscribe();
            }

            @Override
            public void onFailed(MQTTException exception) {
                System.out.println("==============onFailed:"+exception.getMessage());
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
//                public void messageArrived(String topic, MqttMessage message) throws Exception {
//                    // message Arrived!
//                    addToHistory(new String(message.getPayload()));
//                    System.out.println("Message: " + topic + " : " + new String(message.getPayload()));
//                }
//            });

        } catch (Exception ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public void publishMessage() {
//        connect();
        publish();
//        disconnect();
//        subscribe();
//        unSubscribe();
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

    private void unSubscribe() {
        MqttClient.getInstance(this).unSubscribe(subscriptionTopic, new MessageCallBack() {
            @Override
            public void onSuccess() {
                System.out.println("=========取消订阅成功");
            }

            @Override
            public void onFailed(MQTTException exception) {
                System.out.println("==============onFailed:"+exception.getMessage());

            }
        });
    }

    private void subscribe() {
        MqttClient.getInstance(this).subscribe(subscriptionTopic, new MessageCallBack() {
            @Override
            public void onSuccess() {
                System.out.println("订阅"+subscriptionTopic+"成功。。");
            }

            @Override
            public void onFailed(MQTTException exception) {
                System.out.println("==============onFailed:"+exception.getMessage());

            }
        });
    }

    private void disconnect() {
        MqttClient.getInstance(this).disconnect();
    }

    private void publish() {
        //uidL:xxx   与用户相同
        String contentStr = "你好世界";
        ClientPublishMessage.Content content = ClientPublishMessage.Content.newBuilder()
                .setUuid("222222222222")
                .setFromUri("uid:222")
                .setFromEpid(clientId)
                .setFromNickname("sire")
                .setContentType("text/plain")
                .setContentBuffer(ByteString.copyFrom(contentStr.getBytes()))
                .build();
        ClientPublishMessage.Receiver receiver = ClientPublishMessage.Receiver.newBuilder()
                .setToUri("uid:111111")
                .setToEpid("PC;version=2.0.1.0430;uuid=111111")
                .setExceptThisEpid(false)
                .build();
        ClientPublishMessage.MessageRequst result = ClientPublishMessage.MessageRequst.newBuilder()
                .setMessageType(ClientPublishMessage.MessageType.CHAT)
                .setOptions("1")
                .setContent(content)
                .addReceivers(receiver)
                .setPeerUri("uid:111111")
                .setTopic("RT").build();

//        String topic = "RT";
//        Object payload = result;
        String topic = publishTopic;
        Object payload = publishMessage;
        MqttClient.getInstance(this).publish(topic, payload, new MessageCallBack() {
            @Override
            public void onSuccess() {
                System.out.println("发布成功。。。");
            }

            @Override
            public void onFailed(MQTTException exception) {
                System.out.println("发布失败。。。");
                System.out.println("==============onFailed:"+exception.getMessage());

            }
        });

    }


    @Override
    public void onPush(Object message) {

    }
}

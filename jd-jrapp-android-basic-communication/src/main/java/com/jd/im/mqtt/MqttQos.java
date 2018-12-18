package com.jd.im.mqtt;

import com.jd.im.mqtt.messages.MQTTDisconnect;
import com.jd.im.utils.Log;

import com.jd.im.IMQTTMessage;
import com.jd.im.mqtt.messages.MQTTConnack;
import com.jd.im.mqtt.messages.MQTTPuback;
import com.jd.im.mqtt.messages.MQTTPubcomp;
import com.jd.im.mqtt.messages.MQTTPublish;
import com.jd.im.mqtt.messages.MQTTPubrec;
import com.jd.im.mqtt.messages.MQTTPubrel;
import com.jd.im.mqtt.messages.MQTTSuback;
import com.jd.im.mqtt.messages.MQTTUnsuback;

import static com.jd.im.mqtt.MQTTConnectionConstants.STATE_CONNECTED;
import static com.jd.im.mqtt.MQTTConnectionConstants.STATE_CONNECTION_FAILED;
import static com.jd.im.mqtt.MQTTConstants.AT_LEAST_ONCE;
import static com.jd.im.mqtt.MQTTConstants.AT_MOST_ONCE;
import static com.jd.im.mqtt.MQTTConstants.CONNACK;
import static com.jd.im.mqtt.MQTTConstants.CONNECT;
import static com.jd.im.mqtt.MQTTConstants.CONNECTION_ACCEPTED;
import static com.jd.im.mqtt.MQTTConstants.CONNECTION_REFUSED_AUTH;
import static com.jd.im.mqtt.MQTTConstants.CONNECTION_REFUSED_IDENTIFIER;
import static com.jd.im.mqtt.MQTTConstants.CONNECTION_REFUSED_SERVER;
import static com.jd.im.mqtt.MQTTConstants.CONNECTION_REFUSED_USER;
import static com.jd.im.mqtt.MQTTConstants.CONNECTION_REFUSED_VERSION;
import static com.jd.im.mqtt.MQTTConstants.DISCONNECT;
import static com.jd.im.mqtt.MQTTConstants.EXACTLY_ONCE;
import static com.jd.im.mqtt.MQTTConstants.PINGREQ;
import static com.jd.im.mqtt.MQTTConstants.PINGRESP;
import static com.jd.im.mqtt.MQTTConstants.PUBACK;
import static com.jd.im.mqtt.MQTTConstants.PUBCOMP;
import static com.jd.im.mqtt.MQTTConstants.PUBLISH;
import static com.jd.im.mqtt.MQTTConstants.PUBREC;
import static com.jd.im.mqtt.MQTTConstants.PUBREL;
import static com.jd.im.mqtt.MQTTConstants.SUBACK;
import static com.jd.im.mqtt.MQTTConstants.SUBSCRIBE;
import static com.jd.im.mqtt.MQTTConstants.UNSUBACK;
import static com.jd.im.mqtt.MQTTConstants.UNSUBSCRIBE;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/23
 * Author:wangkai
 * Description:MQTT协议操作行为
 * =====================================================
 */
public class MqttQos {
    private static String TAG = "MqttQos";
    /**
     * 消息id跟踪类
     */
    private final MQTTIdentifierHelper identifierHelper;
    private final QosCallBack qosCallBack;

    public MqttQos(QosCallBack qosCallBack) {
        identifierHelper = new MQTTIdentifierHelper();
        this.qosCallBack = qosCallBack;
    }

    public void operate(byte[] data) {
        byte type = MQTTHelper.decodeType(data);
        switch (type) {
            case CONNACK:
                connectState(data);
                break;
            case PUBLISH:
                MQTTPublish publish = MQTTPublish.fromBuffer(data);
                qosPublish(publish);
                break;
            case PUBACK:
                MQTTPuback puback = MQTTPuback.fromBuffer(data);
                int pubAckIdentifier = puback.getPackageIdentifier();
                identifierHelper.removeSentPackage(puback.getPackageIdentifier());
                notifyTimeOutRelease(pubAckIdentifier);
                notifyOperationResult(true, pubAckIdentifier,puback.getPayload());
                break;
            case PUBREC:
                //删除PUBLISH，发送PUBLISHREL并保存
                MQTTPubrec pubRec = MQTTPubrec.fromBuffer(data);
                int packageIdentifier = pubRec.getPackageIdentifier();
                MQTTPubrel clientPubRel = MQTTPubrel.newInstance(packageIdentifier);
                sendMessage(clientPubRel);
                identifierHelper.addSentPackage(clientPubRel);
                notifyTimeOutRelease(packageIdentifier);
                break;
            case PUBREL:
                //分发PUSH消息，删除PUSH消息以及PUSHREC,发送PUSHCOMP消息
                MQTTPubrel serverPubRel = MQTTPubrel.fromBuffer(data);
                int pubIdentifier = serverPubRel.getPackageIdentifier();
                IMQTTMessage mqttMessage = identifierHelper.getMessageFromReceivedPackages(pubIdentifier);
                if (mqttMessage != null && mqttMessage instanceof MQTTPublish) {
                    //消费移除
                    final boolean consumed = dispatchPush(mqttMessage);
                    if(consumed){
                        identifierHelper.removeReceivedPackage(pubIdentifier);
                        identifierHelper.removeSentPackage(pubIdentifier);
                        MQTTPubcomp pubComp = MQTTPubcomp.newInstance(pubIdentifier);
                        sendMessage(pubComp);
                    }
                } else {
                    //内存上的数据丢失主要是程序异常退出，服务器默认之前的会话继续交互，为了使得丢失数据的会话终止，需要发送PUBLISHCOM消息
                    Log.w(TAG, "memory lost push message,id is " + pubIdentifier);
                    identifierHelper.removeSentPackage(pubIdentifier);
                    MQTTPubcomp pubComp = MQTTPubcomp.newInstance(pubIdentifier);
                    sendMessage(pubComp);
                }

                break;
            case PUBCOMP:
                //删除PUBLISHREL,通知发送成功
                MQTTPubcomp serverPubComp = MQTTPubcomp.fromBuffer(data);
                int pubRelIdentifier = serverPubComp.getPackageIdentifier();
                identifierHelper.removeReceivedPackage(pubRelIdentifier);
                notifyOperationResult(true, pubRelIdentifier,serverPubComp.getPayload());
                break;
            case SUBACK:
                MQTTSuback suback = MQTTSuback.fromBuffer(data);
                notifyTimeOutRelease(suback.getPackageIdentifier());
                notifyOperationResult(true, suback.getPackageIdentifier(),null);
                break;
            case UNSUBACK:
                MQTTUnsuback unsuback = MQTTUnsuback.fromBuffer(data);
                notifyTimeOutRelease(unsuback.getPackageIdentifier());
                notifyOperationResult(true, unsuback.getPackageIdentifier(),null);
                break;
            case PINGRESP:
                if (qosCallBack != null) {
                    qosCallBack.onPinReceived();
                }
                break;
            case DISCONNECT:
                MQTTDisconnect mqttDisconnect = MQTTDisconnect.fromBuffer(data);
                dispatchPush(mqttDisconnect);
                break;
             // 客户端不会接受此类消息
            case CONNECT:
            case SUBSCRIBE:
            case UNSUBSCRIBE:
            case PINGREQ:
                break;
        }
    }

    private void qosPublish(MQTTPublish publish) {
        switch (publish.getQoS()) {
            case AT_MOST_ONCE:
                // 直接分发消息
                dispatchPush(publish);
                break;
            case AT_LEAST_ONCE:
                // 分发消息，发送确认
                boolean consumed = dispatchPush(publish);
                //若客户端没有消费此消息，服务器一段时间会再次发送
                if(consumed){
                    MQTTPuback pubAck = MQTTPuback.newInstance(publish.getPackageIdentifier());
                    sendMessage(pubAck);
                }
                break;
            case EXACTLY_ONCE:
                //存储PUBLISH消息,发送PUBREC
                identifierHelper.addReceivedPackage(publish);
                MQTTPubrec pubRec = MQTTPubrec.newInstance(publish.getPackageIdentifier());
                sendMessage(pubRec);
                identifierHelper.addSentPackage(pubRec);
                break;
        }
    }

    private void connectState(byte[] data) {
        switch (MQTTConnack.fromBuffer(data).getReturnCode()) {
            case CONNECTION_ACCEPTED:
                notifyConnectState(STATE_CONNECTED);
                break;
            case CONNECTION_REFUSED_VERSION:
                notifyConnectState(STATE_CONNECTION_FAILED);
                break;
            case CONNECTION_REFUSED_IDENTIFIER:
                notifyConnectState(STATE_CONNECTION_FAILED);
                break;
            case CONNECTION_REFUSED_SERVER:
                notifyConnectState(STATE_CONNECTION_FAILED);
                break;
            case CONNECTION_REFUSED_USER:
                notifyConnectState(STATE_CONNECTION_FAILED);
                break;
            case CONNECTION_REFUSED_AUTH:
                notifyConnectState(STATE_CONNECTION_FAILED);
                break;
        }
    }

    private boolean dispatchPush(IMQTTMessage publish) {
        boolean consumed = true;
        if (qosCallBack != null) {
            consumed =  qosCallBack.dispatchPush(publish);
        }
        return consumed;
    }

    private void notifyOperationResult(boolean success, int id,byte[] extraInfor) {
        if (qosCallBack != null) {
            qosCallBack.notifyOperationResult(success, id,extraInfor);
        }
    }

    private void notifyTimeOutRelease(int id) {
        if (qosCallBack != null) {
            qosCallBack.notifyTimeOutRelease(id);
        }
    }

    private boolean isLocalServiceAvailable() {
        return qosCallBack != null && qosCallBack.isLocalServiceAvailable();
    }

    private void notifyConnectState(int state) {
        if (qosCallBack != null) {
            qosCallBack.notifyConnectState(state);
        }
    }

    private void sendMessage(IMQTTMessage message) {
        if (qosCallBack != null) {
            qosCallBack.sendMessage(message);
        }
    }


    public MQTTIdentifierHelper getIdentifierHelper() {
        return identifierHelper;
    }


    public interface QosCallBack {
        /**
         * 通知连接状态
         *
         * @param state
         */
        void notifyConnectState(int state);

        /**
         * 消息发送操作
         *
         * @param message
         */
        void sendMessage(IMQTTMessage message);

        /**
         * 只有服务端下发的push消息才会向前分发
         * @param publish
         * @return  true表示分发成功，false表示分发失败
         */
        boolean dispatchPush(IMQTTMessage publish);

        /**
         * 收到服务端Pin
         */
        void onPinReceived();

        /**
         * 前后端服务是否处于连接状态
         *
         * @return
         */
        boolean isLocalServiceAvailable();

        /**
         * 通知客户端操作结果
         *
         * @param success
         * @param id
         */
        void notifyOperationResult(boolean success, int id,byte[] extraInfor);

        /**
         * 数据正确，删除超时
         *
         * @param id
         */
        void notifyTimeOutRelease(int id);

    }
}

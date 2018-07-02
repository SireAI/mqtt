package com.jd.jrapp.bm.message.model;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.google.protobuf.nano.MessageNano;
import com.jd.im.client.MessageCallBack;
import com.jd.im.client.MqttClient;
import com.jd.im.converter.MsgContentResolve;
import com.jd.im.heartbeat.net.NetStatusUtil;
import com.jd.im.message.nano.Ack;
import com.jd.im.message.nano.ClientPublishMessage;
import com.jd.im.message.nano.ImageMessage;
import com.jd.im.mqtt.MQTTConnectException;
import com.jd.im.mqtt.MQTTException;
import com.jd.jrapp.bm.message.manager.UploadInfor;
import com.jd.jrapp.bm.message.manager.UploadManager;
import com.jd.jrapp.bm.message.manager.UploadState;
import com.jd.jrapp.bm.message.manager.UploadTask;
import com.jd.jrapp.bm.message.adapter.TasksExecutor;
import com.jd.jrapp.bm.message.bean.Talker;
import com.jd.jrapp.bm.message.constant.Constant;
import com.jd.jrapp.bm.message.db.IMMessage;
import com.jd.jrapp.bm.message.db.Message;
import com.jd.jrapp.bm.message.db.MessageDao;
import com.jd.jrapp.bm.message.db.MessageDb;
import com.jd.jrapp.bm.message.repository.MessageRepository;
import com.jd.jrapp.bm.message.utils.CommonUtils;
import com.jd.jrapp.bm.message.utils.JSONUtils;
import com.jd.jrapp.bm.message.utils.UUIDUtils;

import java.util.Date;
import java.util.List;

import static com.jd.im.message.nano.ClientPublishMessage.CHAT;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/12
 * Author:wangkai
 * Description:
 * =====================================================
 */
public class MessageModel extends ViewModel {
    /**
     * 消息时间显示阈值
     */
    public static final int TIME_THRESHOLDE = 10 * 60 * 1000;
    private static MessageModel instance;
    private MessageRepository messageRepository;
    private String userName = "";
    private String password = "";
    private Talker talker;
    private String clientId;
    private LiveData<List<IMMessage>> uploadMessageData;
    private Context app;

    private MessageModel() {
    }

    public static MessageModel getInstance() {
        if (instance == null) {
            instance = new MessageModel();
        }
        return instance;
    }

    public void init(Context context) {
        if (TextUtils.isEmpty(this.clientId)) {
            Context app = context.getApplicationContext();
            this.app = app;
            this.clientId = generateClientId(app);
            messageRepository = new MessageRepository(MessageDb.getInstance(app).MessageDao());
            uploadMessageData = messageRepository.listenUploadMessage(MsgContentResolve.FILE_TYPE);
            resumeUploadMessage();
        }
    }

    /**
     * 恢复并监听文件上传消息,比如图片，一般文本文件
     */
    private void resumeUploadMessage() {
        uploadMessageData.observeForever(new Observer<List<IMMessage>>() {
            @Override
            public void onChanged(@Nullable final List<IMMessage> imMessages) {
                TasksExecutor.getInstance().executeOnDiskIO(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < imMessages.size(); i++) {
                            final IMMessage message = imMessages.get(i);
                            if (UploadManager.getInstance().isAlreadyInTask(message.getMessageId())) {
                                continue;
                            }
                            final UploadTask uploadTask = new UploadTask(message.getMessageId());
                            if (MsgContentResolve.IMAGE.equals(message.getContentType())) {
                                UploadInfor uploadInfor = JSONUtils.jsonString2Bean(new String(message.getContent()), UploadInfor.class);
                                UploadState uploadState = uploadInfor.getUploadState();
                                if (uploadInfor == null || uploadState == null) {
                                    continue;
                                }
                                if (uploadState.isUploaded()) {
                                    //文件上传成功，发送文本消息
                                    sendFileMessage(message, uploadInfor);
                                    continue;
                                }
                                uploadTask.setUploadInfor(uploadInfor);
                            }
                            uploadTask.setUploadMsgInfor(message);
                            UploadManager.getInstance().addUploadTask(uploadTask);
                        }
                    }
                });
            }
        });
    }

    private void sendFileMessage(final IMMessage message, UploadInfor uploadInfor) {
        sendIMMessage(message, generateImageBodyContent(uploadInfor), uploadInfor.getFromClientId(), uploadInfor.getToClientId(), Constant.TOPIC_OFF_LINE, new MessageCallBack<Ack.MessageAck>() {
            @Override
            public void onSuccess(Ack.MessageAck extraInfor) {
                if (message == null) return;
                message.setTransportState(Constant.T_SUCCESS);
                updateIMMessageState(message);
            }

            @Override
            public void onFailed(MQTTException exception) {
                if (message == null) return;
                checkConnect(exception, message);
                message.setTransportState(Constant.T_FAILED);
                updateIMMessageState(message);
            }
        });
    }

    public boolean checkConnectOK(MQTTException exception, IMMessage message) {
        boolean disConnect = exception instanceof MQTTConnectException;
        if (disConnect) {
            message.setFailedInfor(Constant.NET_ERROR);
        }
        return !disConnect;
    }

    public void checkConnect(MQTTException exception, IMMessage message) {
        checkNetOK(message);
        checkConnectOK(exception, message);
    }

    public byte[] generateImageBodyContent(UploadInfor uploadInfor) {
        ImageMessage.Image image = new ImageMessage.Image();
        image.url = uploadInfor.getUploadedUrl();
        image.id = UUIDUtils.generateSingleId();
        image.file = uploadInfor.getFileName();
        byte[] bytes = null;
        try {
            bytes = MessageNano.toByteArray(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String generateClientId(Context context) {
        String phoneSign = CommonUtils.getPhoneSign(context);
        String versionName = CommonUtils.getVersionName(context);
        return "ANDROID;version=" + versionName + ";uuid=" + phoneSign;
    }

    public Talker getTalker() {
        return talker;
    }

    public void setTalker(Talker talker) {
        if (talker != null) {
            talker.setClientId(clientId);
        }
        this.talker = talker;
    }

    public void sendRealIMMessage(IMMessage imMessage, String fromClientId, String toClientId, MessageCallBack messageCallBack) {
        sendIMMessage(imMessage, fromClientId, toClientId, Constant.TOPIC_REAL_TIME, messageCallBack);
    }

    public void sendOffLineIMMessage(IMMessage imMessage, byte[] content, String fromClientId, String toClientId, MessageCallBack messageCallBack) {
        sendIMMessage(imMessage, content, fromClientId, toClientId, Constant.TOPIC_OFF_LINE, messageCallBack);

    }

    public void sendOffLineIMMessage(IMMessage imMessage, String fromClientId, String toClientId, MessageCallBack messageCallBack) {
        sendIMMessage(imMessage, fromClientId, toClientId, Constant.TOPIC_OFF_LINE, messageCallBack);
    }

    public boolean checkNetOK(IMMessage imMessage) {
        boolean connected = NetStatusUtil.isConnected(app);
        if (!connected) {
            imMessage.setFailedInfor(Constant.NET_ERROR);
        }
        return connected;
    }


    private void sendIMMessage(final IMMessage imMessage, String fromClientId, String toClientId, String requestTopic, final MessageCallBack messageCallBack) {
        byte[] bytesContent = imMessage.getContent();
        sendIMMessage(imMessage, bytesContent, fromClientId, toClientId, requestTopic, messageCallBack);
    }

    private void sendIMMessage(final IMMessage imMessage, byte[] contents, String fromClientId, String toClientId, String requestTopic, final MessageCallBack messageCallBack) {
        ClientPublishMessage.Content content = new ClientPublishMessage.Content();
        content.uuid = UUIDUtils.generateSingleId();
        content.fromUri = Constant.ID_PRIFIX + imMessage.getFromAuthorId();
        content.fromEpid = fromClientId;
        content.fromNickname = imMessage.getFromAuthorName();
        content.contentType = imMessage.getContentType();
        content.contentBuffer = contents;
        ClientPublishMessage.Receiver receiver = new ClientPublishMessage.Receiver();
        receiver.toUri = Constant.ID_PRIFIX + imMessage.getToAuthorId();
        receiver.toEpid = toClientId;
        receiver.exceptThisEpid = false;
        final ClientPublishMessage.MessageRequst messageRequst = new ClientPublishMessage.MessageRequst();
        messageRequst.messageType = CHAT;
        messageRequst.options = "1";
        messageRequst.content = content;
        messageRequst.receivers = new ClientPublishMessage.Receiver[]{receiver};
        messageRequst.peerUri = Constant.ID_PRIFIX + imMessage.getToAuthorId();
        messageRequst.topic = requestTopic;
        sendMessage(requestTopic, messageRequst, messageCallBack);
    }


    private void sendMessage(String requestTopic, MessageNano message, MessageCallBack messageCallBack) {
        MqttClient.getInstance().publish(requestTopic, message, messageCallBack);
    }

    public MessageDao getMessageDao() {
        return messageRepository.getMessageDao();
    }

    /**
     * 消息入库
     *
     * @param message
     * @param time
     */
    @WorkerThread
    public void onReceiveMessage(ClientPublishMessage.Message message, long time) {
        ClientPublishMessage.Content content = message.content;
        if (message.messageType == CHAT) {
            IMMessage imMessage = new IMMessage();
            imMessage.setMessageId(content.uuid);
            String fromAuthorId = "";
            if (content.fromUri != null) {
                fromAuthorId = content.fromUri.replace(Constant.ID_PRIFIX, "");
            }
            imMessage.setFromAuthorId(fromAuthorId);
            imMessage.setFromDeviceId(content.fromEpid);
            imMessage.setFromAuthorName(content.fromNickname);
            imMessage.setToAuthorId(message.ownerId);
            if (talker != null) {
                imMessage.setToAuthorName(talker.getUserName());
            }
            imMessage.setContentType(content.contentType);
            imMessage.setContent(content.contentBuffer);
            imMessage.setTransportState(Constant.T_SUCCESS);
            // TODO: 2018/6/21 改回来时间 
            imMessage.setMessageCreateTime(new Date(time / 10000));
            messageTimeShow(imMessage);
            messageRepository.saveIMMessage(imMessage);
        } else {
            Message commonMessage = new Message();
            commonMessage.setMessageId(content.uuid);
            commonMessage.setMessageType(message.messageType);
            commonMessage.setFromUri(content.fromUri);
            commonMessage.setFromDeviceId(content.fromEpid);
            commonMessage.setFromNickname(content.fromNickname);
            commonMessage.setContent(new String(content.contentBuffer));
            commonMessage.setMessageCreateTime(new Date(time));
            messageRepository.localSave(commonMessage);
        }
    }

    public void messageTimeShow(IMMessage imMessage) {
        Date latestMessageTime = messageRepository.getLatestIMMessageTime();
        if (latestMessageTime != null && ((imMessage.getMessageCreateTime().getTime() - latestMessageTime.getTime()) < TIME_THRESHOLDE)) {
            imMessage.setShowTime(false);
        } else {
            imMessage.setShowTime(true);
        }
    }


    public void saveIMMessage(IMMessage data) {
        messageRepository.saveIMMessage(data);
    }


    public void updateIMMessageState(IMMessage imMessage) {
        messageRepository.updateIMMessage(imMessage);
    }

    public void updateIMMessageSuccess(final String messageId) {
        TasksExecutor.getInstance().executeOnDiskIO(new Runnable() {
            @Override
            public void run() {
                IMMessage imMessage = messageRepository.queryIMMessageById(messageId);
                if (imMessage != null) {
                    imMessage.setTransportState(Constant.T_SUCCESS);
                    messageRepository.updateIMMessage(imMessage);
                }
            }
        });
    }

    public void updateImageTransportState(int state, UploadInfor uploadInfor, IMMessage uploadMsgInfor) {
        UploadState uploadState = uploadInfor.getUploadState();
        uploadState.setState(state);
        uploadMsgInfor.setContent(JSONUtils.bean2JsonString(uploadInfor).getBytes());
        MessageModel.getInstance().updateIMMessageState(uploadMsgInfor);
    }

    public String getClientId() {
        return clientId;
    }


}

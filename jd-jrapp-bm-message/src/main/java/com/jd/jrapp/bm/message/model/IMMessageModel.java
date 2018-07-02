package com.jd.jrapp.bm.message.model;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.jd.im.client.MessageCallBack;
import com.jd.im.converter.MsgContentResolve;
import com.jd.im.message.nano.Ack;
import com.jd.im.message.nano.ImageMessage;
import com.jd.im.mqtt.MQTTException;
import com.jd.jrapp.bm.message.manager.UploadInfor;
import com.jd.jrapp.bm.message.manager.UploadState;
import com.jd.jrapp.bm.message.bean.content.FileContent;
import com.jd.jrapp.bm.message.bean.content.IMultiContent;
import com.jd.jrapp.bm.message.adapter.TasksExecutor;
import com.jd.jrapp.bm.message.bean.MessageRequestInfor;
import com.jd.jrapp.bm.message.bean.Talker;
import com.jd.jrapp.bm.message.constant.Constant;
import com.jd.jrapp.bm.message.db.IMMessage;
import com.jd.jrapp.bm.message.utils.JSONUtils;
import com.jd.jrapp.bm.message.utils.UUIDUtils;

import java.io.File;
import java.util.Date;
import java.util.List;

import static com.jd.jrapp.bm.message.manager.UploadState.UPLOADING;
import static com.jd.jrapp.bm.message.constant.Constant.FILE_MESSAGE_IMAGE;


/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/13
 * Author:wangkai
 * Description: IM消息逻辑业务实现
 * =====================================================
 */
public class IMMessageModel extends ViewModel {
    private static final String TAG = "IMMessageModel";
    private final LiveData<List<IMMessage>> messageData;
    private MutableLiveData<MessageRequestInfor> messageRequestInforData = new MutableLiveData<>();
    private Talker talker;

    public IMMessageModel() {
        messageData = Transformations.map(Transformations.switchMap(messageRequestInforData, new Function<MessageRequestInfor, LiveData<List<IMMessage>>>() {
            @Override
            public LiveData<List<IMMessage>> apply(MessageRequestInfor input) {
                return fetchData(input);
            }
        }), new Function<List<IMMessage>, List<IMMessage>>() {
            @Override
            public List<IMMessage> apply(List<IMMessage> imMessages) {
                return deserializeContent(imMessages);
            }
        });
    }

    /**
     * 解析内容
     * @param imMessages
     * @return
     */
    private List<IMMessage> deserializeContent(List<IMMessage> imMessages) {
        try {
            if(imMessages!=null && imMessages.size()>0){
                for (int i = 0; i < imMessages.size(); i++) {
                    IMMessage imMessage = imMessages.get(i);
                    if(MsgContentResolve.isImage(imMessage.getContentType())){
                        IMultiContent multiContent ;
                        if(isOutMessage(imMessage.getFromAuthorId())){
                            //上行消息
                            UploadInfor uploadInfor = JSONUtils.jsonString2Bean(new String(imMessage.getContent()), UploadInfor.class);
                            multiContent = new FileContent(uploadInfor.getFilePath());
                        }else {
                            //下行消息
                            ImageMessage.Image imageMessage = (ImageMessage.Image) MsgContentResolve.resolve(imMessage.getContentType(),imMessage.getContent());
                            multiContent = new FileContent(imageMessage.url);
                        }
                        imMessage.setContentObj(multiContent);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return imMessages;
    }

    /**
     * * 判断消息的上下行
     * @param fromAuthorId 消息发送者id
     * @return true 表示上行消息，false表示下行消息
     */
    public boolean isOutMessage(String fromAuthorId){
        return talker.getUserId().equals(fromAuthorId);
    }

    public LiveData<List<IMMessage>> getMessageData() {
        return messageData;
    }

    private static void updateSendedMessage(final IMMessage imMessage, final int state) {
        TasksExecutor.getInstance().executeOnDiskIO(new Runnable() {
            @Override
            public void run() {
                if (imMessage != null) {
                    imMessage.setTransportState(state);
                    MessageModel.getInstance().updateIMMessageState(imMessage);
                }
            }
        });
    }


    private LiveData<List<IMMessage>> fetchData(MessageRequestInfor messageRequestInfor) {
        MessageModel messageModel = MessageModel.getInstance();
        return messageModel.getMessageDao().queryIMMessagesByTimeLine(messageRequestInfor.getFromUserId()
                , messageRequestInfor.getToUserId()
                , messageRequestInfor.getTimeLine().getTime()
                , messageRequestInfor.getPageSize());
    }

    public void setRequest(MessageRequestInfor messageRequestInfor) {
        messageRequestInfor.setFromUserId(talker.getUserId());
        messageRequestInfor.setToUserId(talker.getPeerTalker().getUserId());
        messageRequestInforData.setValue(messageRequestInfor);
    }

    /**
     * 获取用户Id
     *
     * @return
     */
    public String getUserId() {
        return talker.getUserId();
    }

    public Talker getTalker() {
        return talker;
    }

    /**
     * 设置对话者
     *
     * @param peerTalker
     */
    public void setTalker(Talker peerTalker) {
        Talker talker = MessageModel.getInstance().getTalker();
        talker.setPeerTalker(peerTalker);
        this.talker = talker;
    }

    /**
     * 发送实时消息
     *
     * @param context
     * @param message
     */
    public void sendRealTimeMessage(final Context context, final String message) {
        final IMMessage imMessage = createTextMessage(message);
        if (imMessage == null) return;
        MessageModel.getInstance().sendRealIMMessage(imMessage, talker.getClientId(), talker.getPeerTalker().getClientId(), new MessageCallBack<Ack.MessageAck>() {
            @Override
            public void onSuccess(final Ack.MessageAck extraInfor) {
                if (imMessage != null) {
                    updateSendedMessage(imMessage, Constant.T_SUCCESS);

                }
            }

            @Override
            public void onFailed(MQTTException exception) {
                if (imMessage != null) {
                    MessageModel.getInstance().checkConnect(exception, imMessage);

                    updateSendedMessage(imMessage, Constant.T_FAILED);
                }
            }
        });

        TasksExecutor.getInstance().executeOnDiskIO(new Runnable() {
            @Override
            public void run() {
                imMessage.setTransportState(Constant.T_SENDING);
                MessageModel instance = MessageModel.getInstance();
                instance.messageTimeShow(imMessage);
                instance.saveIMMessage(imMessage);
            }
        });
    }

    @NonNull
    private IMMessage createImageMessage(File imageFile) {
        if (talker == null || talker.getPeerTalker() == null) return null;
        final IMMessage imMessage = new IMMessage();
        imMessage.setContentType(MsgContentResolve.IMAGE);
        imMessage.setMessageId(UUIDUtils.generateSingleId());

        UploadInfor uploadInfor = new UploadInfor(imageFile.getPath(), imageFile.getName(), -1 + "", "");
        uploadInfor.setUploadMessageId(imMessage.getMessageId());
        uploadInfor.setFromClientId(talker.getClientId());
        uploadInfor.setToClientId(talker.getPeerTalker().getClientId());
        uploadInfor.setUploadState(new UploadState(0, UPLOADING));
        String content = JSONUtils.bean2JsonString(uploadInfor);

        commonInfor(imMessage, content);
        return imMessage;
    }

    private void commonInfor(IMMessage imMessage, String content) {
        imMessage.setMessageCreateTime(new Date());
        imMessage.setContent(content.getBytes());
        imMessage.setFromDeviceId(talker.getClientId());
        imMessage.setRead(true);
        imMessage.setFromAuthorId(talker.getUserId());
        imMessage.setFromAuthorName(talker.getUserName());
        imMessage.setFromAuthorImg(talker.getUserImg());
        imMessage.setToAuthorId(talker.getPeerTalker().getUserId());
        imMessage.setToAuthorName(talker.getPeerTalker().getUserName());
        imMessage.setToAuthorImg(talker.getPeerTalker().getUserImg());
    }

    @NonNull
    private IMMessage createTextMessage(String message) {
        if (talker == null || talker.getPeerTalker() == null) return null;
        final IMMessage imMessage = new IMMessage();
        imMessage.setContentType(MsgContentResolve.TEXT_PLAIN);
        imMessage.setMessageId(UUIDUtils.generateSingleId());
        commonInfor(imMessage, message);
        return imMessage;
    }

    /**
     * 发送离线消息
     *
     * @param message
     */
    public void sendOffLineTextMessage(final String message) {
        final IMMessage imMessage = createTextMessage(message);
        if (imMessage == null) return;
        MessageModel.getInstance().sendOffLineIMMessage(imMessage, talker.getClientId(), talker.getPeerTalker().getClientId(), new MessageCallBack<Ack.MessageAck>() {
            @Override
            public void onSuccess(final Ack.MessageAck extraInfor) {
                if (imMessage != null) {
                    updateSendedMessage(imMessage, Constant.T_SUCCESS);
                }
            }

            @Override
            public void onFailed(MQTTException exception) {
                if (imMessage != null) {
                    MessageModel.getInstance().checkConnect(exception, imMessage);
                    updateSendedMessage(imMessage, Constant.T_FAILED);
                }

            }
        });

        TasksExecutor.getInstance().executeOnDiskIO(new Runnable() {
            @Override
            public void run() {
                imMessage.setTransportState(Constant.T_SENDING);
                MessageModel instance = MessageModel.getInstance();
                instance.messageTimeShow(imMessage);
                instance.saveIMMessage(imMessage);
            }
        });
    }

    public void sendOffLineFileMessage(final File file, int fileMessageType) {
        if (file.exists()) {
            if (fileMessageType == FILE_MESSAGE_IMAGE) {
                final IMMessage imageMessage = createImageMessage(file);
                if (imageMessage == null) return;
                TasksExecutor.getInstance().executeOnDiskIO(new Runnable() {
                    @Override
                    public void run() {
                        imageMessage.setTransportState(Constant.T_SENDING);
                        MessageModel instance = MessageModel.getInstance();
                        instance.messageTimeShow(imageMessage);
                        instance.saveIMMessage(imageMessage);
                    }
                });
            }
        } else {
            Log.e(TAG, "文件不存在");
        }
    }

    /**
     * 重新发送
     *
     * @param imMessage
     */
    public void reSendOffLineMessage(final IMMessage imMessage) {
        if (imMessage == null) return;
        imMessage.setFailedInfor("");
        imMessage.setMessageCreateTime(new Date());
        imMessage.setTransportState(Constant.T_SENDING);
        MessageModel.getInstance().sendOffLineIMMessage(imMessage, talker.getClientId(), talker.getPeerTalker().getClientId(), new MessageCallBack<Ack.MessageAck>() {
            @Override
            public void onSuccess(final Ack.MessageAck extraInfor) {
                if (imMessage != null) {
                    updateSendedMessage(imMessage, Constant.T_SUCCESS);

                }
            }

            @Override
            public void onFailed(MQTTException exception) {
                if (imMessage != null) {
                    MessageModel.getInstance().checkConnect(exception, imMessage);
                    updateSendedMessage(imMessage, Constant.T_FAILED);
                }

            }
        });

        TasksExecutor.getInstance().executeOnDiskIO(new Runnable() {
            @Override
            public void run() {
                imMessage.setTransportState(Constant.T_SENDING);
                MessageModel instance = MessageModel.getInstance();
                instance.messageTimeShow(imMessage);
                instance.saveIMMessage(imMessage);
            }
        });
    }


    public void reSendOffLineFileMessage(final IMMessage imMessage) {
        if (imMessage == null) return;
        TasksExecutor.getInstance().executeOnDiskIO(new Runnable() {
            @Override
            public void run() {
                imMessage.setFailedInfor("");
                imMessage.setMessageCreateTime(new Date());
                imMessage.setTransportState(Constant.T_SENDING);
                MessageModel instance = MessageModel.getInstance();
                instance.messageTimeShow(imMessage);
                instance.saveIMMessage(imMessage);
            }
        });
    }
}

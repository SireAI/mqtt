package com.jd.jrapp.bm.message.Repository;

import android.arch.lifecycle.LiveData;


import com.jd.jrapp.bm.message.db.MessageDao;

import java.util.Date;


/**
 * ==================================================
 * All Right Reserved
 * Date:2018/01/24
 * Author:Sire
 * Description:
 * ==================================================
 */
public class MessageRepository {
    private final MessageDao messageDao;
    private final MessagePushWebService messagePushWebService;

    @Inject
    public MessageRepository(MessageDao messageDao, MessagePushWebService messagePushWebService) {
        this.messageDao = messageDao;
        this.messagePushWebService = messagePushWebService;
    }

    public Flowable<Response<JsonResponse>> messageBind(String userId, String clientId) {
        return messagePushWebService.messageBind(userId,clientId);
    }

    public void localSave(Message message){
        messageDao.savePushMessage(message);
    }

    public LiveData<DataResource<List<Message>>> observerMessageDbDataChange(String userId, Long timeLine, int pageSize) {
        return new DataSourceStrategy.Builder()
                .appDataFromStrategy(DataSourceStrategy.DataFromStrategy.CACHE)
                .listenDB()
                .build()
                .apply(new DataSourceStrategy.DataDecision<JsonResponse<List<Message>>, List<Message>>() {
                    @Override
                    public LiveData<List<Message>> loadFromDb() {
                        return messageDao.queryMessagesByTimeLine(userId,timeLine,pageSize);
                    }
                });
    }

    public void setMessageRead(Message message) {
        messageDao.updateMessage(message);
    }

    public void saveIMMessage(IMMessage data) {
        messageDao.saveIMMessage(data);
    }

    public Message findMessageByID(String messageId) {
        return messageDao.findMessageById(messageId);
    }

    public void updateContent(Message message) {
        messageDao.updateMessage(message);
    }

    public LiveData<DataResource<List<IMMessage>>> observerIMMessageDbDataChange(String currentUserId, String talkUserId, Date timeLine, int pageSize) {
        return new DataSourceStrategy.Builder()
                .appDataFromStrategy(DataSourceStrategy.DataFromStrategy.CACHE)
                .listenDB()
                .build()
                .apply(new DataSourceStrategy.DataDecision<JsonResponse<List<IMMessage>>, List<IMMessage>>() {
                    @Override
                    public LiveData<List<IMMessage>> loadFromDb() {
                        return messageDao.queryIMMessagesByTimeLine(currentUserId,talkUserId,timeLine.getTime(),pageSize);
                    }
                });
    }

    public LiveData<DataResource<IMMessage>> sendMessage(IMMessage imMessage) {
        return new DataSourceStrategy.Builder()
                .appDataFromStrategy(DataSourceStrategy.DataFromStrategy.NET)
                .build()
                .apply(new DataSourceStrategy.DataDecision<JsonResponse<IMMessage>, IMMessage>() {
                    @Override
                    public LiveData<Response<JsonResponse<IMMessage>>> makeNetCall() {
                        return messagePushWebService.imtalk(imMessage);
                    }

                    @Override
                    public void saveData2DB(IMMessage imMessage) {
                        messageDao.saveIMMessage(imMessage);
                    }
                });
    }

    public LiveData<DataResource<Integer>> observerIMMessageCount(String fromAuthorId) {
        return new DataSourceStrategy.Builder()
                .appDataFromStrategy(DataSourceStrategy.DataFromStrategy.CACHE)
                .listenDB()
                .build()
                .apply(new DataSourceStrategy.DataDecision<JsonResponse<Integer>, Integer>() {
                    @Override
                    public LiveData<Integer> loadFromDb() {
                        return messageDao.queryIMMessagesCount(fromAuthorId);
                    }
                });
    }

    public void setIMMessageRead(IMMessage item) {
        messageDao.updateIMMessage(item);
    }

    public Date getLatestIMMessageTime() {
        return messageDao.findLatestIMMessageTime();
    }
}

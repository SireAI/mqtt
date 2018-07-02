package com.jd.jrapp.bm.message.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;


import com.jd.jrapp.bm.message.Manager.UploadState;

import java.util.Date;
import java.util.List;

@Dao
public interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void savePushMessage(Message... messages);
    @Query("SELECT * FROM Message WHERE messageId = :messageId")
    Message findMessageById(String messageId);
    @Query("SELECT * FROM Message WHERE messageCreateTime < :timeLine AND userId = :userId ORDER BY messageCreateTime DESC LIMIT :pageSize")
    LiveData<List<Message>> queryMessagesByTimeLine(String userId, Long timeLine, int pageSize);
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateMessage(Message message);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveIMMessage(IMMessage... messages);
    @Query("SELECT * FROM IMMessage WHERE messageCreateTime < :timeLine AND ((fromAuthorId = :fromAuthorId AND toAuthorId = :toAuthorId) OR (fromAuthorId = :toAuthorId AND toAuthorId = :fromAuthorId)) ORDER BY messageCreateTime DESC LIMIT :pageSize")
    LiveData<List<IMMessage>> queryIMMessagesByTimeLine(String fromAuthorId,String toAuthorId, Long timeLine, int pageSize);
    @Query("SELECT COUNT(*) FROM IMMessage WHERE fromAuthorId = :fromAuthorId ")
    LiveData<Integer> queryIMMessagesCount(String fromAuthorId);
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateIMMessage(IMMessage item);
    @Query("SELECT MAX(messageCreateTime) FROM IMMessage")
    Date findLatestIMMessageTime();
    @Query("SELECT * FROM IMMessage WHERE transportState = 1 AND contentType IN (:uploadMessages)")
    LiveData<List<IMMessage>> queryUploadMessage(String[] uploadMessages);
    @Query("SELECT * FROM IMMessage WHERE messageId = :messageId")
    IMMessage queryByMessageId(String messageId);
    @Query("SELECT * FROM IMMessage WHERE messageId = :messageId")
    IMMessage queryIMMessagesById(String messageId);
}

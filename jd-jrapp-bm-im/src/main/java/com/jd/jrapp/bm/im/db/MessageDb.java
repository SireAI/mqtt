package com.jd.jrapp.bm.im.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

@Database(entities = {IMMessage.class
}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class MessageDb extends RoomDatabase {
    abstract public MessageDao MessageDao();
}
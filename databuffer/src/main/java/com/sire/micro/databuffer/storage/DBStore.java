package com.sire.micro.databuffer.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import com.sire.micro.databuffer.CacheEntry;
import com.sire.micro.databuffer.Log;
import com.sire.micro.databuffer.cache.Topic;

import java.util.ArrayList;
import java.util.List;


import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/10
 * Author:wangkai
 * Description:数据库实现数据存储
 * =====================================================
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class DBStore implements IStore {

    private static final String TAG = "DBStore";


    private static final String CACHE_TIME = "CACHE_TIME";

    private static final String ID = "ID";
    private static final String TOPIC = "TOPIC";
    private static final String MAXSIZE = "MAXSIZE";
    private static final String EXPIRED_TIME = "EXPIRED_TIME";
    private static final String KEY = "KEY";
    private static final String VALUE_TYPE = "VALUE_TYPE";
    private static final String VALUE = "VALUE";
    private static final String DATA_CACHE_TABLE = "DATA_CACHE_TABLE";
    private static final String DATA_TOPIC = "DATA_TOPIC";
    private SQLiteDatabase db = null;
    private DataCacheHelper dataCacheHelper = null;


    public DBStore(Context context) {
        dataCacheHelper = new DataCacheHelper(context);
    }


    @Override
    public void close() {
        if (this.db != null) {
            this.db.close();
        }
    }

    @Override
    public void insert(int topic, int maxSize, long expiredTime) {
        Log.d(TAG, "insert{ topic = " + topic + ", maxSize = " + maxSize + "}, {expiredTime = " + expiredTime + "}");
        db = getDB();
        if(db!=null && db.isOpen()){
            ContentValues values = new ContentValues();
            values.put(TOPIC, topic);
            values.put(MAXSIZE, maxSize);
            values.put(EXPIRED_TIME, expiredTime);
            db.insertWithOnConflict(DATA_TOPIC, null, values, CONFLICT_REPLACE);
        }
    }

    private SQLiteDatabase getDB() {
        try {
            return dataCacheHelper.getWritableDatabase();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void insert(CacheEntry cacheEntry) {
        Log.d(TAG, "insert{" + cacheEntry.getTopic() + ":" + cacheEntry.getKey() + "}, {" + cacheEntry.toString() + "}");
        db = getDB();
        if(db!=null&& db.isOpen()){
            ContentValues values = new ContentValues();
            values.put(ID, cacheEntry.getTopic() + cacheEntry.getKey());
            values.put(TOPIC, cacheEntry.getTopic());
            values.put(KEY, cacheEntry.getKey());
            values.put(VALUE_TYPE, cacheEntry.getValueType());
            values.put(VALUE, cacheEntry.getValue());
            values.put(CACHE_TIME, System.currentTimeMillis());
            db.insertWithOnConflict(DATA_CACHE_TABLE, null, values, CONFLICT_REPLACE);
        }
    }

    @Override
    public void delete(int topic, String key) {
        Log.d(TAG, "delete{" + topic + ":" + key + "}");
        db = getDB();
        if(db!=null&& db.isOpen()){
            String[] selectionArgs = {topic + key};
            int rows = 0;
            try {
                rows = db.delete(DATA_CACHE_TABLE,
                        ID + "=? ",
                        selectionArgs);
            } catch (SQLException e) {
                Log.e(TAG, "delete", e);
            }
            if (rows != 1) {
                Log.e(TAG,
                        "deleteArrived - Error deleting message {" + key
                                + "} from database: Rows affected = " + rows);
            }
        }

    }

    @Override
    public void delete(final int topic) {
        Log.d(TAG, "delete{" +  "topic:" + topic + "}");
        db = getDB();
        if(db!=null&& db.isOpen()){
            TransitionUtils.transition(db, new Runnable() {
                @Override
                public void run() {
                    db.delete(DATA_TOPIC, TOPIC + "=?", new String[]{topic + ""});
                    db.delete(DATA_CACHE_TABLE, TOPIC + "=?", new String[]{topic + ""});
                }
            });
        }

    }

    @Override
    public void update(CacheEntry cacheEntry) {
        db = getDB();
        //TODO
    }

    @Override
    public List<CacheEntry> query(int topic) {
        Log.d(TAG, "queryAllEnties{" + "topic:" + topic + "}");
        db = getDB();
        if(db!=null&& db.isOpen()){
            Cursor cursor = db.query(DATA_CACHE_TABLE,
                    null,
                    TOPIC + "=?",
                    new String[]{topic + ""},
                    null,
                    null,
                    CACHE_TIME + " ASC");
            return getCacheEntriesFromCursor(cursor);
        }
        return new ArrayList<>();

    }

    @NonNull
    private List<CacheEntry> getCacheEntriesFromCursor(Cursor cursor) {
        List<CacheEntry> cacheEntries = new ArrayList<>();
        if (cursor != null) {
            try {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    int topic = cursor.getInt(cursor.getColumnIndex(TOPIC));
                    String key = cursor.getString(cursor.getColumnIndex(KEY));
                    String valueType = cursor.getString(cursor.getColumnIndex(VALUE_TYPE));
                    byte[] value = cursor.getBlob(cursor.getColumnIndex(VALUE));
                    long cacheTime = cursor.getLong(cursor.getColumnIndex(CACHE_TIME));
                    CacheEntry cacheEntry = new CacheEntry(topic, key, valueType, value, cacheTime);
                    cacheEntries.add(cacheEntry);
                }
            } finally {
                cursor.close();
            }
        }
        return cacheEntries;
    }

    @Override
    public List<CacheEntry> queryAllEnties() {
        Log.d(TAG, "queryAllEnties{" + "all data" + "}");
        db = getDB();
        if(db!=null&& db.isOpen()){
            Cursor cursor = db.query(DATA_CACHE_TABLE,
                    null,
                    null,
                    null,
                    null,
                    null,
                    CACHE_TIME + " ASC");
            return getCacheEntriesFromCursor(cursor);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Topic> queryAllTopics() {
        Log.d(TAG, "queryAllTopics{" + "all data" + "}");
        db = getDB();
        if(db!=null&& db.isOpen()){
            Cursor cursor = db.query(DATA_TOPIC,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
            return getTopicsFromCursor(cursor);
        }
        return new ArrayList<>();

    }

    @Override
    public List[] queryAll() {
        db = getDB();
        if(db!=null&& db.isOpen()){
            final List[] datas = new List[2];
            TransitionUtils.transition(db, new Runnable() {
                @Override
                public void run() {
                    List<Topic> topics = queryAllTopics();
                    List<CacheEntry> cacheEntries = queryAllEnties();
                    datas[0] = topics;
                    datas[1] = cacheEntries;
                }
            });
            return datas;
        }
        return new List[0];
    }

    private List<Topic> getTopicsFromCursor(Cursor cursor) {
        List<Topic> topics = new ArrayList<>();
        if (cursor != null) {
            try {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    int topic = cursor.getInt(cursor.getColumnIndex(TOPIC));
                    int expiredTime = cursor.getInt(cursor.getColumnIndex(EXPIRED_TIME));
                    int maxSize = cursor.getInt(cursor.getColumnIndex(MAXSIZE));
                    Topic topicEntity = new Topic(topic, maxSize, expiredTime);
                    topics.add(topicEntity);
                }
            } finally {
                cursor.close();
            }
        }
        return topics;
    }


    private static class DataCacheHelper extends SQLiteOpenHelper {
        private static final String TAG = "DataCacheHelper";

        private static final String DATABASE_NAME = "databuffer.db";

        private static final int DATABASE_VERSION = 1;


        public DataCacheHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(final SQLiteDatabase database) {
            final String createCacheTableStatement = "CREATE TABLE "
                    + DATA_CACHE_TABLE + "("
                    + ID + " TEXT PRIMARY KEY, "
                    + TOPIC + " INTEGER , "
                    + KEY + " TEXT , "
                    + VALUE_TYPE + " TEXT, "
                    + VALUE + " BLOB, "
                    + CACHE_TIME + " INTEGER" + ");";

            Log.d(TAG, "init {" + createCacheTableStatement + "}");
            final String createTopTableStatement = "CREATE TABLE "
                    + DATA_TOPIC + "("
                    + TOPIC + " INTEGER PRIMARY KEY, "
                    + MAXSIZE + " INTEGER, "
                    + EXPIRED_TIME + " LONG  "
                    + ");";
            try {
                TransitionUtils.transition(database, new Runnable() {
                    @Override
                    public void run() {
                        database.execSQL(createCacheTableStatement);
                        database.execSQL(createTopTableStatement);
                        Log.d(TAG, "created the table");
                    }
                });
            } catch (SQLException e) {
                Log.d(TAG, "init", e);
                throw e;
            }
        }

        /**
         * 更新数据库,先删除在重新创建
         *
         * @param db
         * @param oldVersion
         * @param newVersion
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "onUpgrade");
            try {
                db.execSQL("DROP TABLE IF EXISTS " + DATA_CACHE_TABLE);
            } catch (SQLException e) {
                Log.e(TAG, "onUpgrade", e);
                throw e;
            }
            onCreate(db);
            Log.d(TAG, "onUpgrade complete");
        }
    }


}
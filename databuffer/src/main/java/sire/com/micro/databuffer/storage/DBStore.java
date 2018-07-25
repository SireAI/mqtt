package sire.com.micro.databuffer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

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
public class DBStore implements IStore {

    private static final String TAG = "DBStore";


    private static final String MTIMESTAMP = "TIME_STAMP";

    private static final String ID = "ID";
    private static final String TOPIC = "TOPIC";
    private static final String KEY = "KEY";
    private static final String VALUE_TYPE = "VALUE_TYPE";
    private static final String VALUE = "VALUE";
    private static final String DATA_CACHE_TABLE = "DATA_CACHE_TABLE";
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
    public void insert(CacheEntry cacheEntry) {
        Log.d(TAG, "insert{" + cacheEntry.getTopic() + ":" + cacheEntry.getKey() + "}, {" + cacheEntry.toString() + "}");
        db = dataCacheHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, cacheEntry.getTopic() + cacheEntry.getKey());
        values.put(TOPIC, cacheEntry.getTopic());
        values.put(KEY, cacheEntry.getKey());
        values.put(VALUE_TYPE, cacheEntry.getValueType());
        values.put(VALUE, cacheEntry.getValue());
        values.put(MTIMESTAMP, System.currentTimeMillis());
        db.insertWithOnConflict(DATA_CACHE_TABLE, null, values, CONFLICT_REPLACE);
    }

    @Override
    public void delete(int topic, String key) {
        Log.d(TAG, "delete{" + topic + ":" + key + "}");
        db = dataCacheHelper.getWritableDatabase();
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

    @Override
    public void update(CacheEntry cacheEntry) {
        db = dataCacheHelper.getWritableDatabase();

    }

    @Override
    public List<CacheEntry> query(int topic) {
        Log.d(TAG, "queryAll{" + "topic:"+topic + "}");
        db = dataCacheHelper.getWritableDatabase();
        Cursor cursor = db.query(DATA_CACHE_TABLE,
                null,
                TOPIC + "=?",
                new String[]{topic + ""},
                null,
                null,
                MTIMESTAMP + " ASC");
        return getCacheEntriesFromCursor(cursor);
    }

    @NonNull
    private List<CacheEntry> getCacheEntriesFromCursor(Cursor cursor) {
        List<CacheEntry> cacheEntries = new ArrayList<>();
        if(cursor!=null){
            try {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    int topic = cursor.getInt(cursor.getColumnIndex(TOPIC));
                    String key = cursor.getString(cursor.getColumnIndex(KEY));
                    String valueType = cursor.getString(cursor.getColumnIndex(VALUE_TYPE));
                    byte[] value = cursor.getBlob(cursor.getColumnIndex(VALUE));
                    long cacheTime = cursor.getLong(cursor.getColumnIndex(MTIMESTAMP));
                    CacheEntry cacheEntry = new CacheEntry(topic, key, valueType, value, cacheTime);
                    cacheEntries.add(cacheEntry);
                }
            }finally {
                cursor.close();
            }
        }
        return cacheEntries;
    }

    @Override
    public List<CacheEntry> queryAll() {
        Log.d(TAG, "queryAll{" + "all data" + "}");
        db = dataCacheHelper.getWritableDatabase();
        Cursor cursor = db.query(DATA_CACHE_TABLE,
                null,
                null,
                null,
                null,
                null,
                MTIMESTAMP + " ASC");
        return getCacheEntriesFromCursor(cursor);
    }


    private static class DataCacheHelper extends SQLiteOpenHelper {
        private static final String TAG = "DataCacheHelper";

        private static final String DATABASE_NAME = "databuffer.db";

        private static final int DATABASE_VERSION = 1;


        public DataCacheHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase database) {
            String createArrivedTableStatement = "CREATE TABLE "
                    + DATA_CACHE_TABLE + "("
                    + ID + " TEXT PRIMARY KEY, "
                    + TOPIC + " TEXT , "
                    + KEY + " TEXT , "
                    + VALUE_TYPE + " TEXT, "
                    + VALUE + " BLOB, "
                    + MTIMESTAMP + " INTEGER" + ");";

            Log.d(TAG, "init {" + createArrivedTableStatement + "}");
            try {
                database.execSQL(createArrivedTableStatement);
                Log.d(TAG, "created the table");
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
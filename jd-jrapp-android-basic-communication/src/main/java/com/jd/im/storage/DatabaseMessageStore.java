package com.jd.im.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.RemoteException;
import android.support.annotation.RestrictTo;

import com.jd.im.utils.Log;


import com.jd.im.IMQTTMessage;
import com.jd.im.mqtt.messages.MQTTPublish;
import com.jd.im.mqtt.messages.MQTTSubscribe;
import com.jd.im.mqtt.messages.MQTTUnsubscribe;

import java.util.Iterator;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;
import static com.jd.im.mqtt.MQTTConstants.PUBLISH;
import static com.jd.im.mqtt.MQTTConstants.SUBSCRIBE;
import static com.jd.im.mqtt.MQTTConstants.UNSUBACK;
import static com.jd.im.mqtt.MQTTConstants.UNSUBSCRIBE;


/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/10
 * Author:wangkai
 * Description:数据库实现数据存储
 * =====================================================
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class DatabaseMessageStore implements MessageStore {

    private static final String TAG = "DatabaseMessageStore";


    private static final String MTIMESTAMP = "TIME_STAMP";

    private static final String MQTT_MESSAGE_TABLE = "MQTT_MESSAGE_TALBLE";
    private static final String ID = "ID";
    private static final String CLIENT_HANDLE = "CLIENT_HANDLE";
    private static final String TYPE = "TYPE";
    private static final String CONTENTS = "CONTENTS";
    private SQLiteDatabase db = null;
    private MQTTDatabaseHelper mqttDb = null;


    public DatabaseMessageStore(Context context) {
        mqttDb = new MQTTDatabaseHelper(context);
    }


    @Override
    public String storeArrived(String clientHandle, IMQTTMessage message) {
        Log.d(TAG, "storeArrived{" + clientHandle + "}, {" + message.toString() + "}");
        db = mqttDb.getWritableDatabase();
        String id = null;
        try {
            ContentValues values = new ContentValues();
            values.put(ID, id = message.getPackageIdentifier() + clientHandle);
            values.put(CLIENT_HANDLE, clientHandle);
            values.put(TYPE, (int) message.getType());
            values.put(CONTENTS, message.get());
            values.put(MTIMESTAMP, System.currentTimeMillis());
            db.insertWithOnConflict(MQTT_MESSAGE_TABLE, null, values, CONFLICT_REPLACE);
        } catch (RemoteException e) {
            e.printStackTrace();
            id = null;
        }
        return id;
    }


    @Override
    public boolean discardArrived(String clientHandle, String id) {
        Log.d(TAG, "discardArrived{" + clientHandle + "}, {" + id + "}");
        db = mqttDb.getWritableDatabase();
        String[] selectionArgs = {id + clientHandle};
        int rows;
        try {
            rows = db.delete(MQTT_MESSAGE_TABLE,
                    ID + "=? ",
                    selectionArgs);
        } catch (SQLException e) {
            Log.e(TAG, "discardArrived", e);
            throw e;
        }
        if (rows != 1) {
            Log.e(TAG,
                    "discardArrived - Error deleting message {" + id
                            + "} from database: Rows affected = " + rows);
            return false;
        }
        return true;
    }


    @Override
    public Iterator<IMQTTMessage> getAllArrivedMessages(final String clientHandle) {
        return new Iterator<IMQTTMessage>() {
            private final String[] selectionArgs = {clientHandle};
            private Cursor cursor;
            private boolean hasNext;

            {
                db = mqttDb.getWritableDatabase();
                if (clientHandle == null) {
                    cursor = db.query(MQTT_MESSAGE_TABLE,
                            null,
                            null,
                            null,
                            null,
                            null,
                            MTIMESTAMP+" ASC");
                } else {
                    cursor = db.query(MQTT_MESSAGE_TABLE,
                            null,
                            CLIENT_HANDLE + "=?",
                            selectionArgs,
                            null,
                            null,
                            MTIMESTAMP+" ASC");
                }
                hasNext = cursor.moveToFirst();
            }

            @Override
            public boolean hasNext() {
                if (!hasNext) {
                    cursor.close();
                }
                return hasNext;
            }

            @Override
            public IMQTTMessage next() {
                int type = cursor.getInt(cursor.getColumnIndex(TYPE));
                byte[] content = cursor.getBlob(cursor.getColumnIndex(CONTENTS));
                IMQTTMessage message = null;
                if (type == PUBLISH) {
                    MQTTPublish mqttPublish = MQTTPublish.fromBuffer(content);
                    mqttPublish.setDup();
                    message = mqttPublish;
                } else if (type == SUBSCRIBE) {
                    message = MQTTSubscribe.fromBuffer(content);
                } else if (type == UNSUBSCRIBE) {
                    message = MQTTUnsubscribe.fromBuffer(content);
                } else {
                    Log.w(TAG, "message not support resume");
                }
                hasNext = cursor.moveToNext();
                return message;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            protected void finalize() throws Throwable {
                cursor.close();
                super.finalize();
            }

        };
    }

    @Override
    public void clearArrivedMessages(String clientHandle) {

        db = mqttDb.getWritableDatabase();
        String[] selectionArgs = {clientHandle};
        int rows = 0;
        if (clientHandle == null) {
            Log.d(TAG, "clearArrivedMessages: clearing the table");
            rows = db.delete(MQTT_MESSAGE_TABLE, null, null);
        } else {
            Log.d(TAG, "clearArrivedMessages: clearing the table of " + clientHandle + " messages");
            rows = db.delete(MQTT_MESSAGE_TABLE,
                    CLIENT_HANDLE + "=?",
                    selectionArgs);

        }
        Log.d(TAG, "clearArrivedMessages: rows affected = " + rows);
    }

    @Override
    public void close() {
        if (this.db != null) {
            this.db.close();
        }
    }


    private static class MQTTDatabaseHelper extends SQLiteOpenHelper {
        private static final String TAG = "MQTTDatabaseHelper";

        private static final String DATABASE_NAME = "mqttAndroidService.db";

        private static final int DATABASE_VERSION = 1;


        public MQTTDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase database) {
            String createArrivedTableStatement = "CREATE TABLE "
                    + MQTT_MESSAGE_TABLE + "("
                    + ID + " TEXT PRIMARY KEY, "
                    + CLIENT_HANDLE + " TEXT , "
                    + TYPE + " INTEGER, "
                    + CONTENTS + " BLOB, "
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
                db.execSQL("DROP TABLE IF EXISTS " + MQTT_MESSAGE_TABLE);
            } catch (SQLException e) {
                Log.e(TAG, "onUpgrade", e);
                throw e;
            }
            onCreate(db);
            Log.d(TAG, "onUpgrade complete");
        }
    }


}
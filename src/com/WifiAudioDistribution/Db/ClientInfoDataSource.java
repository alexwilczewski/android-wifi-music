package com.WifiAudioDistribution.Db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.WifiAudioDistribution.Networking.ClientInfo;

public class ClientInfoDataSource {
    private static final String TAG = "MYAPP:ClientInfoDataSource";

    private SQLiteDatabase database;
    private SqliteHelper dbHelper;
    private String[] allColumns = {
        SqliteHelper.ClientInfo.C_ID,
        SqliteHelper.ClientInfo.C_HOSTNAME,
        SqliteHelper.ClientInfo.C_PORT,
        SqliteHelper.ClientInfo.C_SERVICENAME,
        SqliteHelper.ClientInfo.C_POD_ID
    };

    public ClientInfoDataSource(Context context) {
        dbHelper = new SqliteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public boolean isOpen() {
        return (database != null && database.isOpen());
    }

    public List<ClientInfo> getAll() {
        List<ClientInfo> list = new ArrayList<ClientInfo>();

        Cursor cursor = database.query(SqliteHelper.ClientInfo.TABLE,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ClientInfo item = toObject(cursor);
            list.add(item);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return list;
    }

    public ClientInfo find(long id) {
        Cursor cursor = database.query(SqliteHelper.ClientInfo.TABLE,
                allColumns, SqliteHelper.ClientInfo.C_ID + " = " + id, null,
                null, null, null);
        cursor.moveToFirst();
        if(cursor.getCount() <= 0) {
            return ClientInfo.getEmpty();
        }
        ClientInfo item = toObject(cursor);
        cursor.close();
        return item;
    }

    public List<ClientInfo> findByPodId(long podId) {
        List<ClientInfo> list = new ArrayList<ClientInfo>();

        Cursor cursor = database.query(SqliteHelper.ClientInfo.TABLE,
                allColumns, SqliteHelper.ClientInfo.C_POD_ID + " = " + podId, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ClientInfo item = toObject(cursor);
            list.add(item);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return list;
    }

    public boolean save(ClientInfo store) {
        ContentValues values = new ContentValues();
        values.put(SqliteHelper.ClientInfo.C_HOSTNAME, store.host);
        values.put(SqliteHelper.ClientInfo.C_PORT, store.port);
        values.put(SqliteHelper.ClientInfo.C_SERVICENAME, store.name);
        values.put(SqliteHelper.ClientInfo.C_POD_ID, store.pod_id);

        boolean success;
        if(ClientInfo.isEmpty(store)) {
            long insertId = create(values);

            if(insertId == -1) {
                // Error occured
                success = false;
            } else {
                store.id = insertId;
                success = true;
            }
        } else {
            int count = update(store.id, values);
            success = (count > 0);
        }

        return success;
    }

    private long create(ContentValues values) {
        return database.insert(SqliteHelper.ClientInfo.TABLE, null, values);
    }

    private int update(long id, ContentValues values) {
        return database.update(SqliteHelper.ClientInfo.TABLE,
                values, SqliteHelper.ClientInfo.C_ID + " = " + id, null);
    }

    public boolean delete(long id) {
        int count = database.delete(SqliteHelper.ClientInfo.TABLE,
                SqliteHelper.ClientInfo.C_ID + " = " + id, null);
        return (count > 0);
    }

    private ClientInfo toObject(Cursor cursor) {
        ClientInfo item = new ClientInfo();
        item.id = cursor.getLong(0);
        item.host = cursor.getString(1);
        item.port = cursor.getInt(2);
        item.name = cursor.getString(3);
        item.pod_id = cursor.getInt(4);

        return item;
    }
}

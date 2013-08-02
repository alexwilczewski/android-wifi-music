package com.WifiAudioDistribution.Db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.WifiAudioDistribution.ClientInfo;

public class ClientInfoDataSource {
    private static final String TAG = "MYAPP:ClientInfoDataSource";
    // Database fields
    private SQLiteDatabase database;
    private ClientInfoSqliteHelper dbHelper;
    private String[] allColumns = { ClientInfoSqliteHelper.C_ID,
            ClientInfoSqliteHelper.C_HOSTNAME, ClientInfoSqliteHelper.C_PORT,
            ClientInfoSqliteHelper.C_SERVICENAME };

    public ClientInfoDataSource(Context context) {
        dbHelper = new ClientInfoSqliteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long create(ClientInfo store) {
        ContentValues values = new ContentValues();
        values.put(ClientInfoSqliteHelper.C_HOSTNAME, store.host);
        values.put(ClientInfoSqliteHelper.C_PORT, store.port);
        values.put(ClientInfoSqliteHelper.C_SERVICENAME, store.name);

        long insertId = database.insert(ClientInfoSqliteHelper.TABLE, null, values);
        return insertId;
    }

    public List<ClientInfoDbWrapper> getAll() {
        List<ClientInfoDbWrapper> list = new ArrayList<ClientInfoDbWrapper>();

        Cursor cursor = database.query(ClientInfoSqliteHelper.TABLE,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ClientInfoDbWrapper item = toObject(cursor);
            list.add(item);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return list;
    }

    public ClientInfoDbWrapper find(long id) {
        Cursor cursor = database.query(ClientInfoSqliteHelper.TABLE,
                allColumns, ClientInfoSqliteHelper.C_ID + " = " + id, null,
                null, null, null);
        cursor.moveToFirst();
        if(cursor.getCount() <= 0) {
            return ClientInfoDbWrapper.getEmpty();
        }
        ClientInfoDbWrapper item = toObject(cursor);
        cursor.close();
        return item;
    }

    public boolean save(long id, ClientInfo store) {
        ContentValues values = new ContentValues();
        values.put(ClientInfoSqliteHelper.C_HOSTNAME, store.host);
        values.put(ClientInfoSqliteHelper.C_PORT, store.port);
        values.put(ClientInfoSqliteHelper.C_SERVICENAME, store.name);

        int count = database.update(ClientInfoSqliteHelper.TABLE,
                values, ClientInfoSqliteHelper.C_ID + " = " + id, null);
        return (count > 0);
    }

    public boolean delete(long id) {
        int count = database.delete(ClientInfoSqliteHelper.TABLE,
                ClientInfoSqliteHelper.C_ID + " = " + id, null);
        return (count > 0);
    }

    private ClientInfoDbWrapper toObject(Cursor cursor) {
        ClientInfoDbWrapper item = new ClientInfoDbWrapper();
        item.id = cursor.getLong(0);
        item.host = cursor.getString(1);
        item.port = cursor.getInt(2);
        item.name = cursor.getString(3);

        return item;
    }
}

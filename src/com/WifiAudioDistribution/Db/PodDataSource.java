package com.WifiAudioDistribution.Db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.WifiAudioDistribution.Pod;

import java.util.ArrayList;
import java.util.List;

public class PodDataSource {
    private static final String TAG = "MYAPP:PodDataSource";
    // Database fields
    private SQLiteDatabase database;
    private SqliteHelper dbHelper;
    private String[] allColumns = {
        SqliteHelper.Pod.C_ID,
        SqliteHelper.Pod.C_NAME
    };

    public PodDataSource(Context context) {
        dbHelper = new SqliteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public List<Pod> getAll() {
        List<Pod> list = new ArrayList<Pod>();

        Cursor cursor = database.query(SqliteHelper.Pod.TABLE,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Pod item = toObject(cursor);
            list.add(item);
            cursor.moveToNext();
        }

        // Make sure to close the cursor
        cursor.close();
        return list;
    }

    public Pod find(long id) {
        Cursor cursor = database.query(SqliteHelper.Pod.TABLE,
                allColumns, SqliteHelper.Pod.C_ID + " = " + id, null,
                null, null, null);
        cursor.moveToFirst();

        if(cursor.getCount() <= 0) {
            return null;
        }

        Pod item = toObject(cursor);
        cursor.close();
        return item;
    }

    public boolean save(Pod store) {
        ContentValues values = new ContentValues();
        values.put(SqliteHelper.Pod.C_NAME, store.getName());

        boolean success;
        if(store.isNew()) {
            long insertId = create(values);

            if(insertId == -1) {
                // Error occurred
                success = false;
            } else {
                store.setId(insertId);
                success = true;
            }
        } else {
            int count = update(store.getId(), values);
            success = (count > 0);
        }

        return success;
    }

    private long create(ContentValues values) {
        return database.insert(SqliteHelper.Pod.TABLE, null, values);
    }

    private int update(long id, ContentValues values) {
        return database.update(SqliteHelper.Pod.TABLE,
                values, SqliteHelper.Pod.C_ID + " = " + id, null);
    }

    public boolean delete(long id) {
        int count = database.delete(SqliteHelper.Pod.TABLE,
                SqliteHelper.Pod.C_ID + " = " + id, null);
        return (count > 0);
    }

    private Pod toObject(Cursor cursor) {
        long id = cursor.getLong(0);
        String name = cursor.getString(1);

        return new Pod(id, name);
    }
}

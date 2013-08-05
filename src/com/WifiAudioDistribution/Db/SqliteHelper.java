package com.WifiAudioDistribution.Db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SqliteHelper extends SQLiteOpenHelper {
    private static final String TAG = "MYAPP:SqliteHelper";

    private static final String DATABASE_NAME = "wifiaudio.db";
    private static final int DATABASE_VERSION = 2;

    public static class ClientInfo {
        public static final String TABLE          = "clientinfo";
        public static final String C_ID           = "_id";
        public static final String C_HOSTNAME     = "hostname";
        public static final String C_PORT         = "port";
        public static final String C_SERVICENAME  = "servicename";
        public static final String C_POD_ID       = "pod_id";
    }

    public static class Pod {
        public static final String TABLE          = "pods";
        public static final String C_ID           = "_id";
        public static final String C_NAME         = "name";
    }

    public SqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // Create ClientInfo table
        database.execSQL("CREATE TABLE " + SqliteHelper.ClientInfo.TABLE + "("
                + SqliteHelper.ClientInfo.C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SqliteHelper.ClientInfo.C_HOSTNAME + " TEXT NOT NULL, "
                + SqliteHelper.ClientInfo.C_PORT + " INTEGER NOT NULL, "
                + SqliteHelper.ClientInfo.C_SERVICENAME + " TEXT NOT NULL, "
                + SqliteHelper.ClientInfo.C_POD_ID + " INTEGER NOT NULL DEFAULT 0);");

        // Create Pods table
        database.execSQL("CREATE TABLE " + SqliteHelper.Pod.TABLE + "("
                + SqliteHelper.Pod.C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SqliteHelper.Pod.C_NAME + " TEXT NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");

        db.execSQL("DROP TABLE IF EXISTS " + SqliteHelper.ClientInfo.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SqliteHelper.Pod.TABLE);

        onCreate(db);
    }
}

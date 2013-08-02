package com.WifiAudioDistribution.Db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ClientInfoSqliteHelper extends SQLiteOpenHelper {
    private static final String TAG = "MYAPP:ClientInfoSqliteHelper";

    private static final String DATABASE_NAME = "wifiaudio.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE          = "clientinfo";
    public static final String C_ID           = "_id";
    public static final String C_HOSTNAME     = "hostname";
    public static final String C_PORT         = "port";
    public static final String C_SERVICENAME  = "servicename";

    public ClientInfoSqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " + TABLE + "("
                + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + C_HOSTNAME + " TEXT NOT NULL, "
                + C_PORT + " INTEGER NOT NULL, "
                + C_SERVICENAME + " TEXT NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }
}

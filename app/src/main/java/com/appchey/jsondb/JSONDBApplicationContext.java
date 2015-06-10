package com.appchey.jsondb;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

public class JSONDBApplicationContext extends Application
{
    private static JSONDBApplicationContext instance = null;
    private SQLiteDatabase db;

    public JSONDBApplicationContext()
    {
        instance = this;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        DBManager manager = new DBManager(this);

        db = manager.getWritableDatabase();
    }

    public static JSONDBApplicationContext getContext()
    {
        return instance;
    }

    public SQLiteDatabase getDatabase()
    {
        return db;
    }
}

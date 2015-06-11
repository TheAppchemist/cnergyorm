package com.appchemy.cnergyorm;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

public class CnergyApplicationContext extends Application
{
    private static CnergyApplicationContext instance = null;
    private SQLiteDatabase db;

    public CnergyApplicationContext()
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

    public static CnergyApplicationContext getContext()
    {
        return instance;
    }

    public SQLiteDatabase getDatabase()
    {
        return db;
    }
}

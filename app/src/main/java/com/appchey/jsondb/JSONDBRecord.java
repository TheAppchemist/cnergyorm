package com.appchey.jsondb;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class JSONDBRecord <T extends JSONDBRecord> implements BaseColumns
{
    public int _id;
    private String table_name;
    private static Context contxt;

    public JSONDBRecord()
    {
        //ApplicationInfo ai = context.getApplicationInfo();
        //ai.metaData.getString("jsondb_database");

        table_name = getClass().getSimpleName().toLowerCase();
    }

    public JSONDBRecord(JSONObject json)
    {

    }

    public static void init(Context context)
    {
        contxt = context;
    }

    public static ArrayList<JSONDBRecord> all(Class c)
    {
        ArrayList<JSONDBRecord> all = new ArrayList<>();

        return all;
    }

    public void save()
    {
        ArrayList<Field> columns = new ArrayList<>();
        Field[] fields = getClass().getDeclaredFields();
        SQLiteDatabase db = new DBManager(contxt).getWritableDatabase();

        for (Field field : fields)
        {
            if (Modifier.isPublic(field.getModifiers()))
            {
                columns.add(field);
            }


        }

        if (!tableExists(table_name, db))
        {
            createTable(columns, db);
        }

        db.close();
    }

    private void createTable(ArrayList<Field> fields, SQLiteDatabase db)
    {
        String table_name = getClass().getSimpleName().toLowerCase();

        String query = "CREATE TABLE " + table_name + " (" +
                _ID + " INTEGER PRIMARY KEY, ";

        for (Field field : fields)
        {
            query += field.getName() + " " + fieldType(field) + ",";
        }

        query = query.substring(0, query.length() - 1) + ")";

        db.execSQL(query);
    }

    private boolean tableExists(String tableName, SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    private void drop()
    {
        String table_name = getClass().getSimpleName().toLowerCase();
        String query = "DROP TABLE IF EXISTS " + table_name;
    }

    private String fieldType(Field field)
    {
        Class<?> type = field.getType();
        if (type.isAssignableFrom(Integer.TYPE) ||
                type.isAssignableFrom(Short.TYPE) ||
                type.isAssignableFrom(Long.TYPE) ||
                type.isAssignableFrom(Byte.TYPE)) {
            return "INTEGER";
        }
        else if (type.isAssignableFrom(String.class))
        {
            return "TEXT";
        }

        return null;
    }
}

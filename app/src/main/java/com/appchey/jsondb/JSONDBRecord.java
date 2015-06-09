package com.appchey.jsondb;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class JSONDBRecord <T extends JSONDBRecord> implements BaseColumns, Serializable
{
    public long _id;
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

    public static <T> ArrayList<T> all(Class c)
    {
        ArrayList<Field> columns = new ArrayList<>();
        Field[] fields = c.getDeclaredFields();

        ArrayList<T> all = new ArrayList<>();

        SQLiteDatabase db = new DBManager(contxt).getWritableDatabase();

        for (Field field : fields)
        {
            if (Modifier.isPublic(field.getModifiers()))
            {
                columns.add(field);
            }
        }

        // _id is not returned by getDeclaredFields because it's declared
        // higher up the hierarchy
        String[] projection = new String[columns.size() + 1];
        projection[projection.length - 1] = "_id";
        for (int i = 0; i < projection.length - 1; i++)
        {
            projection[i] = columns.get(i).getName();
        }

        Cursor cursor = db.query(c.getSimpleName().toLowerCase(),
            projection,
            null,
            null,
            null,
            null,
            null);

        cursor.moveToFirst();
        T record;
        try
        {
            while (!cursor.isAfterLast())
            {
                record = (T) c.newInstance();
                int count = cursor.getColumnCount();
                for (int i = 0; i < count; i++)
                {
                    if (cursor.getColumnName(i).equals(_ID))
                    {

                        c.getField("_id").setLong(record, cursor.getInt(i));
                    }
                    else if (cursor.getType(i) == Cursor.FIELD_TYPE_INTEGER)
                    {
                        c.getField(cursor.getColumnName(i)).setInt(record, cursor.getInt(i));
                    }
                    else if (cursor.getType(i) == Cursor.FIELD_TYPE_STRING)
                    {
                        c.getField(cursor.getColumnName(i)).set(record, cursor.getString(i));
                    }
                }

                all.add(record);

                cursor.moveToNext();
            }
            cursor.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        db.close();

        return all;
    }

    public long save()
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

        ContentValues values = new ContentValues();
        for (Field field : columns)
        {
            try
            {
                String type = fieldType(field);

                if (type != null)
                {
                    if (type.equals("INTEGER"))
                    {
                        values.put(field.getName(), field.getInt(this));
                    }
                    else
                    {
                        values.put(field.getName(), (String)field.get(this));
                    }
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (_id == -1)
        {
            _id = db.insert(
                    table_name,
                    null,
                    values);
        }
        else
        {
            db.update(table_name,
                    values,
                    _ID+"=?",
                    new String[]{""+_id});
        }

        db.close();

        return _id;
    }

    public void delete()
    {
        SQLiteDatabase db = new DBManager(contxt).getWritableDatabase();
        db.delete(table_name,
                _ID + "=?",
                new String[] {""+_id});
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

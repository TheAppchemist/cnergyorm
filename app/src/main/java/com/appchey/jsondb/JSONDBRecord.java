package com.appchey.jsondb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class JSONDBRecord <T extends JSONDBRecord> implements BaseColumns, Serializable
{
    private static final long serialVersionUID = 6349676001008456136L;
    public static final String INTEGER = "INTEGER";
    public static final String TEXT = "TEXT";
    public static final String NULL = "NULL";
    public static final String REAL = "REAL";
    public static final String BLOB = "BLOB";
    public static final String UNSUPPORTED = "UNSUPPORTED";

    public long _id = -1;

    private static Context contxt;
    private static ArrayList<Field> columns;
    private static boolean fieldsGenerated;
    private static String table_name;

    static
    {
        columns = new ArrayList<>();
        fieldsGenerated = false;
    }

    private static void generateFields(Class c)
    {
        Field[] fields = c.getDeclaredFields();
        for (Field field : fields)
        {
            if (Modifier.isPublic(field.getModifiers()))
            {
                try
                {
                    Class<?> componentType = field.getType().getComponentType();
                    componentType.newInstance();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                columns.add(field);
            }
        }

        table_name = c.getSimpleName().toLowerCase();
    }

    public JSONDBRecord()
    {
        table_name = getClass().getSimpleName().toLowerCase();

        generateFields(getClass());
    }

    public JSONDBRecord(JSONObject json)
    {
        this();

        Iterator<?> keys = json.keys();

        String key;
        Class c = getClass();
        while (keys.hasNext())
        {
            key = (String)keys.next();
            try {
                Field field = c.getField(key);
                if (fieldType(field).equals(INTEGER))
                {
                    field.setInt(this, json.getInt(key));
                }
                else if (fieldType(field).equals(TEXT))
                {
                    field.set(this, json.getString(key));
                }
            }
            catch (NoSuchFieldException e)
            {
                // don't do anything if field is not found (ignore)
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void init(Context context)
    {
        contxt = context;
    }

    public static <T> Query query(Class c)
    {
        return new <T>Query(c);
    }

    public static <T> ArrayList<T> list(Class c)
    {
        return list(c, null, null);
    }

    public static <T> ArrayList<T> list(Class c,
                                        String selection,
                                        String[] selectionArgs)
    {
        return list(c,
                selection,
                selectionArgs,
                null,
                null,
                null);
    }

    public static <T> ArrayList<T> list(Class c,
                                        String selection,
                                        String[] selectionArgs,
                                        String groupBy)
    {
        return list(c,
                selection,
                selectionArgs,
                groupBy,
                null,
                null);
    }

    public static <T> ArrayList<T> list(Class c,
                                        String selection,
                                        String[] selectionArgs,
                                        String groupBy,
                                        String having)
    {
        return list(c,
                selection,
                selectionArgs,
                groupBy,
                having,
                null);
    }

    public static <T> ArrayList<T> list(Class c,
                                        String selection,
                                        String[] selectionArgs,
                                        String groupBy,
                                        String having,
                                        String orderBy)
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

        if (!tableExists(c.getSimpleName().toLowerCase(), db))
        {
            Log.i("Creating table", c.getSimpleName().toLowerCase());
            createTable(c, columns, db);
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
            selection,
            selectionArgs,
            groupBy,
            having,
            orderBy);

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
                        Field field = c.getField(cursor.getColumnName(i));
                        if (field.getType().isAssignableFrom(Date.class))
                        {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            field.set(record, sdf.parse(cursor.getString(i)));
                        }
                        else
                        {
                            c.getField(cursor.getColumnName(i)).set(record, cursor.getString(i));
                        }
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
        SQLiteDatabase db = new DBManager(contxt).getWritableDatabase();
        //createTableIfNotExist(columns, db);

        ArrayList<Field> columns = new ArrayList<>();
        Field[] fields = getClass().getDeclaredFields();

        for (Field field : fields)
        {
            if (Modifier.isPublic(field.getModifiers()))
            {
                try
                {
                    Class<?> componentType = field.get(this).getClass().getComponentType();
                    componentType.newInstance();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

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

                if (!type.equals(UNSUPPORTED))
                {
                    if (type.equals(INTEGER))
                    {
                        values.put(field.getName(), field.getInt(this));
                    }
                    else if (type.equals(REAL))
                    {
                        values.put(field.getName(), field.getDouble(this));
                    }
                    else
                    {
                        if (field.get(this) != null)
                        {
                            if (field.getType().isAssignableFrom(Date.class))
                            {
                                Date date = (Date) field.get(this);
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                values.put(field.getName(), sdf.format(date));
                            }
                            else
                            {
                                values.put(field.getName(), (String) field.get(this));
                            }
                        }
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
        createTableIfNotExist(columns, db);

        db.delete(table_name,
                _ID + "=?",
                new String[]{"" + _id});
    }

    public static void deleteAll(Class c)
    {
        deleteAll(c,
                null,
                null);
    }

    public static void deleteAll(Class c, String where, String[] selectionArgs)
    {
        SQLiteDatabase db = new DBManager(contxt).getWritableDatabase();
        createTableIfNotExist(columns, db);

        String table_name = c.getSimpleName().toLowerCase();
        db.delete(table_name,
                where,
                selectionArgs);

        db.close();
    }

    private static void createTable(Class c, ArrayList<Field> fields, SQLiteDatabase db)
    {
        String table_name = c.getSimpleName().toLowerCase();

        String query = "CREATE TABLE " + table_name + " (" +
                _ID + " INTEGER PRIMARY KEY, ";

        for (Field field : fields)
        {
            query += field.getName() + " " + fieldType(field) + ",";
        }

        query = query.substring(0, query.length() - 1) + ")";

        db.execSQL(query);
    }

    private static void createTableIfNotExist(Class c, ArrayList<Field> fields, SQLiteDatabase db)
    {
        if (!tableExists(table_name, db))
        {
            createTable(fields, db);
        }
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

    private static boolean tableExists(String tableName, SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor != null)
        {
            if(cursor.getCount() > 0)
            {
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

    private static String fieldType(Field field)
    {
        Class<?> type = field.getType();
        if (type.isAssignableFrom(Integer.TYPE) ||
                type.isAssignableFrom(Short.TYPE) ||
                type.isAssignableFrom(Long.TYPE) ||
                type.isAssignableFrom(Byte.TYPE)) {
            return INTEGER;
        }
        else if (type.isAssignableFrom(String.class))
        {
            return TEXT;
        }
        else if (type.isAssignableFrom(Float.TYPE) ||
                type.isAssignableFrom(Double.TYPE))
        {
            return REAL;
        }
        else if (type.isAssignableFrom(Date.class))
        {
            return TEXT;
        }

        return UNSUPPORTED;
    }
}

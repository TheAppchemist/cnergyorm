package com.appchey.jsondb;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.appchey.jsondb.tableinfo.Column;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
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
    public static final String FOREIGN_KEY = "FOREIGN_KEY";
    public static final String UNSUPPORTED = "UNSUPPORTED";

    public long _id = -1;

    public JSONDBRecord()
    {
        SQLiteDatabase db = JSONDBApplicationContext.getContext().getDatabase();
        setup(getClass(), db);
    }

    public JSONDBRecord(JsonParser parser) throws IOException
    {
        if (parser.nextToken() != JsonToken.START_OBJECT)
        {
            throw new IOException("Jackson IO Error");
        }

        ArrayList<Column> columns = generateFields(getClass());

        while (parser.nextToken() != JsonToken.END_OBJECT)
        {
            String fieldName = parser.getCurrentName();
            parser.nextToken();

            String value = parser.getText();

            for (Column column : columns)
            {
                if (fieldName.equals(column.getName()))
                {
                    try
                    {
                        Field field = getClass().getField(column.getName());
                        Class <?> type = field.getType();
                        if (type == Integer.TYPE)
                        {
                            if (TextUtils.isDigitsOnly(value)) {
                                field.setInt(this, Integer.parseInt(value));
                            }
                            break;
                        }
                    }
                    catch (Exception e)
                    {

                    }
                }
            }
        }
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

    public static String getTableName(Class c)
    {
        return c.getSimpleName().toLowerCase();
    }

    private static ArrayList<Column> generateFields(Class c)
    {
        ArrayList<Column> columns = new ArrayList<>();
        Field[] fields = c.getDeclaredFields();
        for (Field field : fields)
        {
            if (Modifier.isPublic(field.getModifiers()))
            {
                if (field.getType().isAssignableFrom(ArrayList.class))
                {
                    try {
                        Type type = field.getGenericType();
                        if (type instanceof ParameterizedType)
                        {
                            ParameterizedType paramType = (ParameterizedType)type;
                            Type[] types = paramType.getActualTypeArguments();
                            if (types.length > 0)
                            {
                                String classname = types[0].toString();
                                if (classname.length() > 6) // start of class name
                                {
                                    classname = classname.substring(6);
                                    Class.forName(classname).newInstance();
                                }
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    continue;
                }

                String name;
                Class<?> type = field.getType();
                if (type == Long.TYPE ||
                        type == Integer.TYPE ||
                        type == Short.TYPE ||
                        type == Byte.TYPE ||
                        type == Double.TYPE ||
                        type == Float.TYPE ||
                        type == Date.class ||
                        type == String.class)
                {
                    name = field.getName();
                    columns.add(new Column(name, fieldType(field)));
                }
                else
                {
                    try
                    {
                        if (field.getType().newInstance() instanceof JSONDBRecord)
                        {
                            name = getTableName(field.getType());
                            columns.add(new Column(name, fieldType(field), true));
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        return columns;
    }

    private static void setup(Class c, SQLiteDatabase db)
    {
        if (!tableExists(getTableName(c), db))
        {
            createTable(c, db);
        }
        else
        {

        }
    }

    public static <T> Query query(Class c)
    {
        return new <T>Query(c);
    }

    public static <T> T findById(Class c, int id)
    {
        SQLiteDatabase db = JSONDBApplicationContext.getContext().getDatabase();
        ArrayList<Column> columns = generateFields(c);
        String[] projection = new String[columns.size() + 1];
        projection[projection.length - 1] = "_id";
        for (int i = 0; i < projection.length - 1; i++)
        {
            projection[i] = columns.get(i).getName();
        }

        Cursor cursor = db.query(getTableName(c),
                projection,
                _ID+"=?",
                new String[] {""+id},
                null,
                null,
                null);

        if (cursor.moveToFirst())
        {
            try
            {
                return setValues(c, cursor);
            }
            catch (Exception e)
            {
                e.printStackTrace();

                return null;
            }
        }

        return null;
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
        ArrayList<Column> columns = generateFields(c);

        ArrayList<T> all = new ArrayList<>();

        SQLiteDatabase db = JSONDBApplicationContext.getContext().getDatabase();
        setup(c, db);

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
                record = setValues(c, cursor);

                all.add(record);

                cursor.moveToNext();
            }
            cursor.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return all;
    }

    private static <T> T setValues(Class c, Cursor cursor) throws Exception
    {
        T record = (T) c.newInstance();
        int count = cursor.getColumnCount();
        for (int i = 0; i < count; i++)
        {
            if (cursor.getColumnName(i).equals(_ID))
            {
                c.getField("_id").setLong(record, cursor.getInt(i));
            }
            else if (cursor.getType(i) == Cursor.FIELD_TYPE_INTEGER)
            {
                String name = cursor.getColumnName(i);
                Field field = c.getField(name);
                if (isPrimitive(c.getField(name)))
                {
                    field.setInt(record, cursor.getInt(i));
                }
                else if (c.newInstance() instanceof JSONDBRecord)
                {
                    SQLiteDatabase db = JSONDBApplicationContext.getContext().getDatabase();
                    Cursor foreign_cursor = db.query(getTableName(field.getType()), null, _ID + "=?",
                            new String[]{"" + cursor.getInt(i)}, null, null, null);

                    if (foreign_cursor.moveToFirst())
                    {
                        T foreign_record = setValues(field.getType(), foreign_cursor);
                        field.set(record, foreign_record);
                    }
                }
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

        return record;
    }

    public long save()
    {
        SQLiteDatabase db = JSONDBApplicationContext.getContext().getDatabase();
        setup(getClass(), db);

        ArrayList<Column> columns = generateFields(getClass());

        ContentValues values = new ContentValues();
        for (Column column : columns)
        {
            try
            {
                String type = column.getType();
                String name = column.getName();

                if (!type.equals(UNSUPPORTED))
                {
                    Field field = getClass().getField(name);
                    if (column.isPrimaryKey())
                    {
                        long id = ((JSONDBRecord)field.get(this)).save();
                        values.put(name, id);
                        continue;
                    }

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
                    getTableName(getClass()),
                    null,
                    values);
        }
        else
        {
            db.update(getTableName(getClass()),
                    values,
                    _ID+"=?",
                    new String[]{""+_id});
        }

        return _id;
    }

    public void delete()
    {
        SQLiteDatabase db = JSONDBApplicationContext.getContext().getDatabase();
        setup(getClass(), db);

        db.delete(getTableName(getClass()),
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
        SQLiteDatabase db = JSONDBApplicationContext.getContext().getDatabase();
        setup(c, db);

        String table_name = c.getSimpleName().toLowerCase();
        db.delete(table_name,
                where,
                selectionArgs);
    }

    private static void createTable(Class c, SQLiteDatabase db)
    {
        String table_name = getTableName(c);
        ArrayList<Column> columns = generateFields(c);

        String query = "CREATE TABLE " + table_name + " (" +
                _ID + " INTEGER PRIMARY KEY, ";

        for (Column column : columns)
        {
            query += column.getName() + " " + column.getType() + ",";
        }

        query = query.substring(0, query.length() - 1) + ")";

        db.execSQL(query);
    }

    private static boolean tableExists(String tableName, SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
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
        else
        {
            try
            {
                if (type.newInstance() instanceof JSONDBRecord)
                {
                    return INTEGER;
                }
            }
            catch (Exception e)
            {
                // TODO ignore
                e.printStackTrace();
            }
        }

        return UNSUPPORTED;
    }

    private static boolean isPrimitive(Field field)
    {
        Class <?> type = field.getType();
        if (type == Long.TYPE ||
                type == Integer.TYPE ||
                type == Short.TYPE ||
                type == Byte.TYPE ||
                type == Double.TYPE ||
                type == Float.TYPE)
        {
            return true;
        }

        return false;
    }
}

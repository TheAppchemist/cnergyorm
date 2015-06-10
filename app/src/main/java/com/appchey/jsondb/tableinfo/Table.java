package com.appchey.jsondb.tableinfo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class Table
{
    private String name;
    private ArrayList<Column> columns;

    public Table(String name)
    {
        this.name = name;
        columns = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public ArrayList<Column> getColumns()
    {
        return columns;
    }
}

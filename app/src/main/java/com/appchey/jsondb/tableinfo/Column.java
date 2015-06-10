package com.appchey.jsondb.tableinfo;

public class Column
{
    private String name;
    private String type;
    private boolean primaryKey;

    public Column(String name, String type, boolean primaryKey)
    {
        this.name = name;
        this.type = type;
        this.primaryKey = primaryKey;
    }

    public Column(String name, String type)
    {
        this(name, type, false);
    }

    public String getType() {
        return type;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public String getName()
    {
        return name;
    }
}

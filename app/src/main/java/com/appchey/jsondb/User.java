package com.appchey.jsondb;

import java.util.ArrayList;
import java.util.Date;

public class User extends JSONDBRecord <User>
{
    private static final long serialVersionUID = -4621290116388742213L;
    public String name;
    public int age;
    public Date created = new Date();
    public ArrayList<Car> cars;
    public Profile profile;
}

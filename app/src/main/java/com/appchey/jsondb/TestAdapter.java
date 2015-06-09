package com.appchey.jsondb;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by melvin on 15/06/08.
 */
public class TestAdapter extends BaseAdapter
{
    private ArrayList<User> users = new ArrayList<>();
    public TestAdapter()
    {

    }

    public void setList(ArrayList<User> users)
    {
        this.users = users;
    }

    @Override
    public int getCount()
    {
        return users.size();
    }

    @Override
    public User getItem(int i)
    {
        return users.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent)
    {
        View view;
        if (convertView == null)
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_user, null);
        }
        else {
            view = convertView;
        }

        TextView txt_name = (TextView)view.findViewById(R.id.txt_name);
        TextView txt_age = (TextView)view.findViewById(R.id.txt_age);
        TextView txt_id = (TextView)view.findViewById(R.id.txt_id);

        User user = getItem(i);
        txt_name.setText(user.name);
        txt_age.setText(""+user.age);
        txt_id.setText("Date: " + user.created.toString());

        return view;
    }
}

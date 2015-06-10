package com.appchey.jsondb;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
{
    private String[] menu =
            {
                    "Edit",
                    "Delete"
            };
    private ArrayList<User> users;
    private TestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView list = (ListView)findViewById(R.id.list);
        adapter = new TestAdapter();
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = User.findById(User.class, position+1);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Info")
                        .setMessage(user.name);

                builder.create().show();
            }
        });
        registerForContextMenu(list);

        //Car.list(Car.class);
        users = User.query(User.class)
                //.where("age>?", new String[] {"1"})
                .orderAsc("created")
                .list();
        adapter.setList(users);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        if (v.getId() == R.id.list)
        {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle("Options");
            for (int i = 0; i < this.menu.length; i++)
            {
                menu.add(Menu.NONE, i, i, this.menu[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int index = item.getItemId();
        User user = users.get(info.position);

        switch (index)
        {
            case 0:
            {
                Intent intent = new Intent(this, EditActivity.class);
                intent.putExtra("user", user);
                startActivityForResult(intent, 1234);
                break;
            }
            case 1:
            {
                user.delete();

                users = User.list(User.class);
                adapter.setList(users);
                adapter.notifyDataSetChanged();
                break;
            }
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234)
        {
            if (resultCode == RESULT_OK)
            {
                users = User.list(User.class);
                adapter.setList(users);
                adapter.notifyDataSetChanged();
            }
        }
        else if (requestCode == 1235)
        {
            if (resultCode == RESULT_OK)
            {
                users = User.list(User.class);
                adapter.setList(users);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add)
        {
            Intent intent = new Intent(this, EditActivity.class);
            startActivityForResult(intent, 1235);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.appchey.jsondb;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JSONDBRecord.init(getApplicationContext());

        ListView list = (ListView)findViewById(R.id.list);
        TestAdapter adapter = new TestAdapter();
        list.setAdapter(adapter);

        String[] names = {
            "Khutha",
            "Mel",
            "Tshepiso",
            "Yvonne",
            "Musehani",
            "Working"
        };

        /*User user;
        Random random = new Random(System.currentTimeMillis());
        for (String s : names)
        {
            user = new User();
            user.age = random.nextInt(40);
            user.name = s;
            user.save();
        }*/

        ArrayList<User> users = User.all(User.class);
        adapter.setList(users);
        adapter.notifyDataSetChanged();
        Log.i("Users count:", users.size() + "");
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

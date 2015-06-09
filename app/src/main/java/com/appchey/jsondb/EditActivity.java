package com.appchey.jsondb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


public class EditActivity extends AppCompatActivity
{
    private User user;
    private EditText edt_name;
    private EditText edt_age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        user = (User)getIntent().getSerializableExtra("user");
        edt_name = (EditText)findViewById(R.id.edt_name);
        edt_age = (EditText)findViewById(R.id.edt_age);

        edt_name.setText(""+user.name);
        edt_age.setText(""+user.age);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save)
        {
            user.name = edt_name.getText().toString();
            user.age = Integer.parseInt(edt_age.getText().toString());
            user.save();
            setResult(RESULT_OK);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.example.myronlg.pulltorefresh;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView testListView = (ListView) findViewById(R.id.list);
        List<String> testTitleStrList = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            testTitleStrList.add(i+"");
        }
        BaseAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, testTitleStrList);
        testListView.setAdapter(adapter);


        PTRFrameLayout ptrFrameLayout = (PTRFrameLayout) findViewById(R.id.ptr);
        ptrFrameLayout.setPTRListener(new PTRFrameLayout.PTRListener() {
            @Override
            public void onTrigger() {
                Toast.makeText(getApplicationContext(), "mua~", Toast.LENGTH_LONG).show();
            }
        });
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

package com.sealbluetoothtoothpasteapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;


public class DataDisplayActivity extends ActionBarActivity {

    private List<BrushData> records;
    private String dataName;

    public static final String TAG = "DataDisplayActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);
        Intent intent = getIntent();
        dataName = intent.getStringExtra(InitialActivity.LOGDATA);

    }

    @Override
    protected void onStart(){
        super.onStart();
        records = new ArrayList<BrushData>();

        File weights = new File(this.getFilesDir(), dataName);

        try {
            Scanner input = new Scanner(weights);
            while(input.hasNextLine()){
                String line = input.nextLine();
                Log.d(TAG, line);
                String[] tokens = line.split(":");
                Log.d(TAG,"tokens length:" + tokens.length);
                if(tokens.length >= 2) {
                    ByteBuffer dateBytes = ByteBuffer.wrap(tokens[1].getBytes());
                    Date date = new Date(dateBytes.getLong());
                    String formattedDate = DateFormat.getDateTimeInstance().format(date);
                    Log.d(TAG, formattedDate + ": " + tokens[0]);

                    BrushData toAdd = new BrushData(date, Float.parseFloat(tokens[0]));
                    records.add(toAdd);
                } else{
                    Log.e(TAG,"invalid entry found in data! skipping for next entry");
                }
            }
        } catch(FileNotFoundException e){
            Log.e(TAG, "file not found");
            e.printStackTrace();
        }


        ListView contents = (ListView) findViewById(R.id.data_list);
        ArrayListAdapter adapter = new ArrayListAdapter(this, R.layout.row_layout, records);

        contents.setAdapter(adapter);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        Log.d(TAG, "exiting view");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_data_display, menu);
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

    //Wrapper class for data so that Adapter can be easily extended

    private class ArrayListAdapter extends ArrayAdapter<BrushData> {
        Context context;
        int viewId;
        List<BrushData> data;

        public ArrayListAdapter(Context context, int textViewResourceId, List<BrushData> data) {
            super(context, textViewResourceId, data);

            this.context = context;
            this.viewId = textViewResourceId;
            this.data = data;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.row_layout, parent, false);
            TextView dateView = (TextView) rowView.findViewById(R.id.row_layout_date);
            TextView weightView = (TextView) rowView.findViewById(R.id.row_layout_weight);

            DateFormat df = DateFormat.getDateTimeInstance();

            dateView.setText(df.format(data.get(position).date));
            weightView.setText(data.get(position).brushAmount + " g");


            return rowView;
        }
    }
}



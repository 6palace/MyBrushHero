package com.sealbluetoothtoothpasteapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DataDisplayActivity extends ActionBarActivity {

    private List<BrushData> records;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);
        Intent intent = getIntent();
        Bundle loggedData = intent.getBundleExtra(InitialActivity.LOGDATA);
        ArrayList<String> dataArray = loggedData.getStringArrayList(InitialActivity.LOGDATACONTENT);

        records = new ArrayList<BrushData>();

        for(int i = 0; i < dataArray.size(); i++){
            String line = dataArray.get(i);
            String[] tokens = line.split(":");
            ByteBuffer dateBytes = ByteBuffer.wrap(tokens[1].getBytes());
            Date date = new Date(dateBytes.getLong());
//                String formattedDate = DateFormat.getDateTimeInstance().format(date);
//                Log.d(TAG, formattedDate + ": " + tokens[0]);

            BrushData toAdd = new BrushData(date, Integer.parseInt(tokens[0]));
            records.add(toAdd);
        }

        ListView contents = (ListView) findViewById(R.id.data_list);
        ArrayListAdapter adapter = new ArrayListAdapter(this, R.layout.row_layout, records);

        contents.setAdapter(adapter);
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

            dateView.setText(DateFormat.getDateTimeInstance().format(data.get(position).date));
            weightView.setText(data.get(position).brushAmount + " oz");


            return rowView;
        }
    }
}



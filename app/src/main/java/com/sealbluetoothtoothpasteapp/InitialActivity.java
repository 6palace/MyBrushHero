package com.sealbluetoothtoothpasteapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


//TODO finish some preliminary layouts for Thursday, hook up bluetooth input system, begin implementing some features
public class InitialActivity extends AppCompatActivity {

    private static final String TAG = "InitialActivity";
    public static final String LOGDATA = "com.sealbluetoothtoothpasteapp.LOGDATA";
    public static final String LOGDATACONTENT = "dataInStrings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
    }

    @Override
    protected void onStart(){
        super.onStart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_initial, menu);
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
        if (id == R.id.action_connect) {
            blueToothConnect();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //TODO: connect with bluetooth device, establish communication.
    public void blueToothConnect(){

    }

    //Acquire a toothpaste weight measurement somehow and save it into user data
    public void logWeight(View view){
        File output = new File(this.getFilesDir(), "output");
        EditText input = (EditText) findViewById(R.id.testInput);

        long time = System.currentTimeMillis();
        byte[] timeBuffer = ByteBuffer.allocate(8).putLong(time).array();

        try {
            FileOutputStream outputStream = new FileOutputStream(output, true);
            outputStream.write(input.getText().toString().getBytes());
            outputStream.write(':');
            outputStream.write(timeBuffer);
            outputStream.write('\n');
            outputStream.close();
        } catch(Exception e){
            e.printStackTrace();
        }

        input.setText("");
    }

    public void displayWeights(View view){
        File weights = new File(this.getFilesDir(), "output");

        ArrayList<String> sentDataStrings = new ArrayList<String>();
        try {
            Scanner input = new Scanner(weights);
            while(input.hasNextLine()){
                String line = input.nextLine().trim();
                String[] tokens = line.split(":");
                ByteBuffer dateBytes = ByteBuffer.wrap(tokens[1].getBytes());
                Date date = new Date(dateBytes.getLong());
//                String formattedDate = DateFormat.getDateTimeInstance().format(date);
//                Log.d(TAG, formattedDate + ": " + tokens[0]);

                sentDataStrings.add(line);
            }
        } catch(Exception e){
            Log.e(TAG, "file not found");
            e.printStackTrace();
        }

        Bundle sentBundle = new Bundle();
        sentBundle.putStringArrayList(LOGDATACONTENT, sentDataStrings);

        Intent moveActivity = new Intent(this, DataDisplayActivity.class);
        moveActivity.putExtra(LOGDATA, sentBundle);
        startActivity(moveActivity);
    }
}

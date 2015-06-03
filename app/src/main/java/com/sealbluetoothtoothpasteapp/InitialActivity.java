package com.sealbluetoothtoothpasteapp;

import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.UUID;


//TODO finish some preliminary layouts for Thursday, hook up bluetooth input system, begin implementing some features
public class InitialActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback{

    private static final String TAG = "InitialActivity";
    public static final String LOGDATA = "com.sealbluetoothtoothpasteapp.LOGDATA";
    public static final String LOGDATACONTENT = "dataInStrings";

    private static final int BUFFER_CAPACITY = 4;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private RFduinoService rfduinoService;

    private FragmentManager fragmentManager;


    private boolean boundToDevice;

    private Float beforeBrushWeight;

    private boolean brushing;

    private DataBuffer dataBuff;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        fragmentManager = getFragmentManager();
        brushing = false;

    }

    @Override
    protected void onStart(){
        super.onStart();
        registerReceiver(rfduinoReceiver, RFduinoService.getIntentFilter());

    }

    @Override
    protected void onStop(){
        super.onStop();

        bluetoothAdapter.stopLeScan(this);


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
        if (id == R.id.action_scan) {
            blueToothScan();
            return true;
        }
        if(id == R.id.action_connect) {
            bluetoothBind();
        }

        return super.onOptionsItemSelected(item);
    }

    //binds the rfduino to the rfduinoservice? I'm not even sure what to do right now
    public void bluetoothBind(){
        Log.d(TAG,"scanning...");
        bluetoothAdapter.startLeScan(new UUID[]{BluetoothHelper.sixteenBitUuid(0x2220)},
                InitialActivity.this);

    }

    //Private ServiceConnection that monitors the rfduino connection service
    private final ServiceConnection rfduinoServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            rfduinoService = ((RFduinoService.LocalBinder) service).getService();
            if (rfduinoService.initialize()) {
                if (rfduinoService.connect(bluetoothDevice.getAddress())) {
                    Log.d(TAG,"connected with rfduino");
                }
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            rfduinoService = null;
            Log.d(TAG,"disconnected with rfduino");
        }
    };

    //Recieves broadcasts from the rfduinoservice for stuff
    private final BroadcastReceiver rfduinoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (RFduinoService.ACTION_CONNECTED.equals(action)) {
                Log.d(TAG,"rfduino is connected");
            } else if (RFduinoService.ACTION_DISCONNECTED.equals(action)) {
                Log.d(TAG,"rfduino is disconnected");
            } else if (RFduinoService.ACTION_DATA_AVAILABLE.equals(action)) {
                addData(intent.getByteArrayExtra(RFduinoService.EXTRA_DATA));
                //TODO: process the data.
            }
        }
    };

    //Takes byte array and processes
    //TODO: turn array into useful data, then integrate with writeData
    private void addData(byte[] data){
//        String asBytes = "[";
//        for(int i = data.length - 1; i > 0; i--){
//            asBytes = asBytes.concat(Integer.toString(data[i]) + " ");
//        }
//        asBytes += "]";
        //turn little-endian 4 byte into float, apparently
        float asFloat = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getFloat();

        if(dataBuff == null) {
            dataBuff = new DataBuffer(BUFFER_CAPACITY);
        }
        dataBuff.put(asFloat);
        Log.d(TAG, dataBuff.toString());


//        Log.d(TAG,"received RFduino data as float: " + asFloat);
    }

    //Rolling queue with a last option, could have used dequeue, forgot.
    private class DataBuffer{
        private float[] data;
        private int getIndex;
        private int putIndex;
        private int capacity;

        public DataBuffer(int capacity){
            data = new float[capacity];
            getIndex = 0;
            putIndex = 0;
            this.capacity = capacity;
        }

        public void put(float putIn){
            data[putIndex] = putIn;
            putIndex = (putIndex + 1) % capacity;

            if(putIndex == getIndex){
                getIndex = (getIndex + 1) % capacity;
            }
        }

        public Float get(){
            if(getIndex == putIndex){
                return null;
            } else{
                float result = data[getIndex];
                getIndex = (getIndex + 1) % capacity;
                return result;
            }
        }

        @Override
        public String toString(){
            String result = "start:";
//            for(int i = getIndex; i != (getIndex - 1) % capacity; i = (i + 1) % capacity){
            for(int i = 0; i < data.length; i++){
                result += data[i] + ",\t";
            }
            result += ".";
            return result;
        }
    }


    //Scans for bluetooth devices, sets bluetoothDevice to the matching rfduino device.
    public void blueToothScan(){
        //Enable bluetooth if not enabled already
        if(!bluetoothAdapter.isEnabled()){
            boolean enabled = bluetoothAdapter.enable();
            if(enabled){
                Log.d(TAG, "bluetooth enable");
            } else {
                Log.e(TAG, "enable failed");
            }
        } else{
            Log.d(TAG, "bluetooth already enabled");
        }



    }

    //Acquire a toothpaste weight measurement somehow and save it into user data
    public void logWeight(View view){
        //Debug code using the debug dialog
//        File output = new File(this.getFilesDir(), "output");
//        EditText input = (EditText) findViewById(R.id.testInput);
        //        input.setText("");
//        recordWeight(input.getText().toString());


        Button target = (Button) view;

        if(!brushing) {
            beforeBrushWeight = dataBuff.get();
            Log.d(TAG, "try before: " + Float.toString(beforeBrushWeight));
            brushing = true;
            target.setText(R.string.after_squeeze);
        } else{
            brushing = false;
            float afterBrushWeight = dataBuff.get();
            Log.d(TAG, "try after: " + Float.toString(afterBrushWeight) + ". total used: " + (beforeBrushWeight - afterBrushWeight));
            target.setText(R.string.before_squeeze);
            recordWeight(Float.toString(beforeBrushWeight - afterBrushWeight));

            brushSuccessDialog confirmDia = new brushSuccessDialog();
            confirmDia.show(fragmentManager, "dialog");
        }

    }


    private void recordWeight(String weight){
        File output = new File(this.getFilesDir(), "output");
        long time = System.currentTimeMillis();
        byte[] timeBuffer = ByteBuffer.allocate(8).putLong(time).array();

        try {
            FileOutputStream outputStream = new FileOutputStream(output, true);
            outputStream.write(weight.getBytes());
            outputStream.write(':');
            outputStream.write(timeBuffer);
            outputStream.write('\n');
            outputStream.close();
        } catch(Exception e){
            e.printStackTrace();
        }
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

    //TODO: determine if changing min api to 18 is necessary
    public void onLeScan(BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        bluetoothAdapter.stopLeScan(this);
        bluetoothDevice = device;


        InitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String text = BluetoothHelper.getDeviceInfoText(bluetoothDevice, rssi, scanRecord);
                Log.d(TAG,"Found device!");
                Log.d(TAG,text);
                TextView debugspace = (TextView) findViewById(R.id.debug_bluetooth_name);
                debugspace.setText("rfduino found!");
            }
        });


        Log.d(TAG, "pairing...");
        Intent rfduinoIntent = new Intent(InitialActivity.this, RFduinoService.class);

        boundToDevice = bindService(rfduinoIntent, rfduinoServiceConnection, BIND_AUTO_CREATE);


    }

    @Override
    public void onResume(){
        super.onResume();

        registerReceiver(rfduinoReceiver, RFduinoService.getIntentFilter());
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(boundToDevice){
            boundToDevice = false;
            unbindService(rfduinoServiceConnection);
        }


        unregisterReceiver(rfduinoReceiver);
    }
}

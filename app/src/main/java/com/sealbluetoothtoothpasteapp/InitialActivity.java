package com.sealbluetoothtoothpasteapp;

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
import android.widget.EditText;
import android.widget.TextView;

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
import java.util.UUID;


//TODO finish some preliminary layouts for Thursday, hook up bluetooth input system, begin implementing some features
public class InitialActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback{

    private static final String TAG = "InitialActivity";
    public static final String LOGDATA = "com.sealbluetoothtoothpasteapp.LOGDATA";
    public static final String LOGDATACONTENT = "dataInStrings";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private RFduinoService rfduinoService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

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

        unregisterReceiver(rfduinoReceiver);

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
        Log.d(TAG, "pairing...");
        Intent rfduinoIntent = new Intent(InitialActivity.this, RFduinoService.class);
        bindService(rfduinoIntent, rfduinoServiceConnection, BIND_AUTO_CREATE);

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
        String asHex = HexAsciiHelper.bytesToHex(data);
        String asAscii = HexAsciiHelper.bytesToAsciiMaybe(data);

        Log.d(TAG,"received RFduino data as hex: " + asHex);
        if(asAscii != null){
            Log.d(TAG, "received RFduino data as Ascii: " + asAscii);
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

        bluetoothAdapter.startLeScan(new UUID[] { BluetoothHelper.sixteenBitUuid(0x2220) },
                InitialActivity.this);

    }

    //Acquire a toothpaste weight measurement somehow and save it into user data
    public void logWeight(View view){
        File output = new File(this.getFilesDir(), "output");
        EditText input = (EditText) findViewById(R.id.testInput);

        recordWeight(input.getText().toString());

        input.setText("");
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
                debugspace.setText(text);
            }
        });


    }
}

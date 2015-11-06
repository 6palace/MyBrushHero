package com.sealbluetoothtoothpasteapp;

import android.content.Context;
import android.content.ContextWrapper;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

/**
 * Created by Henry on 11/5/2015.
 */
//TODO implement as parcelable to directly send the data structure between activities
public class BrushProfile{

    public static final String TAG = "BrushProfile";
    public static final String PROFILEDATANAME = "profile";
    public int profileId;
    public float requireWeight;
    public boolean hasRequireWeight;
//        public int dailyBrush;

    public int fields;
    public ArrayList<BrushData> brushRecord;

    public EditText weightStatusTarget;
    //more variables for multiple profiles

    public BrushProfile(int profileId, File dir, EditText weightTarget){
        this.profileId = profileId;
        fields = 1;
        weightStatusTarget = weightTarget;
        brushRecord = new ArrayList<BrushData>();
        initFromFile(dir);
    }

    private void getRequireWeight(Scanner input){
        hasRequireWeight = input.hasNextFloat();
        if(hasRequireWeight) {
            setWeight(input.nextFloat());
        } else{
            setWeight((float) -1.0);
        }
        input.nextLine();
    }

    //TODO create blank profile
    public void initBlankProfile(){
        hasRequireWeight = false;
        setWeight((float) -1.0);

    }

    public void initFromFile(File dir){
        File profileFile = new File(dir, PROFILEDATANAME + Integer.toString(profileId));

        Scanner profileInput;
        try{
            if(profileFile.createNewFile()){
                Log.d(TAG, "profile created");
                initBlankProfile();
            } else{
                Log.d(TAG, "profile exists already");
                profileInput = new Scanner(profileFile);
                getRequireWeight(profileInput);
                getBrushHistory(profileInput);
            }
        } catch(IOException e){
            Log.e(TAG, "Profile IO Exception!");
            e.printStackTrace();
            initBlankProfile();
        }
    }

    //Assumes file is just plain brush history
    public void getBrushHistory(Scanner input){
        //refresh and reinterpret
        brushRecord.clear();

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
                brushRecord.add(toAdd);
            } else{
                Log.e(TAG,"invalid entry found in data! skipping for next entry");
            }
        }
    }

    public void addBrush(float weight, Date date){
        brushRecord.add(new BrushData(date, weight));
        Log.d(TAG, "brush stored in profile!");
    }

    public void setWeight(){
        try {
            float weightAsFloat = Float.parseFloat(weightStatusTarget.getText().toString());
            setWeight(weightAsFloat);
        } catch(NumberFormatException e){
            Log.e(TAG,"no float entered!");
        }
    }

    //TODO figure out hasRequireWeight
    public void setWeight(float setTo){
        weightStatusTarget.setText("");
        Log.d(TAG, "set weight as float: " + setTo);
        if(setTo > 0.0){
            requireWeight = setTo;
            weightStatusTarget.setHint(String.format("Currently set to %.1f grams", requireWeight));
        } else{
            weightStatusTarget.setHint("You inputted an invalid weight! Try again!");
        }
    }

    //writes profile into file system
    //TODO implement replace and append instead of remaking file every time
    public void writeProfile(File dir) throws IOException{
        File profileFile = new File(dir, PROFILEDATANAME + Integer.toString(profileId));
        profileFile.delete();
        profileFile.createNewFile();
        FileWriter outputWriter = new FileWriter(profileFile, false);
        outputWriter.write(Float.toString(requireWeight)+"\n");
        outputWriter.close();

        FileOutputStream outputStream = new FileOutputStream(profileFile, true);
        for(BrushData data : brushRecord){
            byte[] weightAsBytes = Float.toString(data.brushAmount).getBytes();
            byte[] timeBuffer = ByteBuffer.allocate(8).putLong(data.date.getTime()).array();
            outputStream.write(weightAsBytes);
            outputStream.write(':');
            outputStream.write(timeBuffer);
            outputStream.write('\n');
        }
        outputStream.close();
        Log.d(TAG, "writing requireWeight Success");
    }

    public int checkTodayBrushes(){
        int res = 0;
        Calendar nowDate = Calendar.getInstance(TimeZone.getDefault());
        int nowDay = nowDate.get(Calendar.DAY_OF_YEAR);
        int nowYear = nowDate.get(Calendar.YEAR);
        Log.d(TAG, "day of year: " + nowDay + " year: " + nowYear);

        Calendar thenDate = Calendar.getInstance(TimeZone.getDefault());
        for(BrushData data : brushRecord){
            thenDate.setTime(data.date);
            if(nowYear == thenDate.get(Calendar.YEAR) && nowDay == thenDate.get(Calendar.DAY_OF_YEAR)){
                Log.d(TAG, "found a today brush!");
                res++;
            }
        }
        Log.d(TAG, "number of brushes today: " + res);
        return res;
    }
}

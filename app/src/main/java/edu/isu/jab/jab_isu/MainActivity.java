package edu.isu.jab.jab_isu;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.ConsumerIrManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.widget.Toast;
import android.app.Activity;  //NOT USED
import android.os.Bundle;  //NOT USED
import android.view.MotionEvent;  //NOT USED
import android.widget.ViewFlipper;


import android.app.Activity; //NOT USED
import android.os.Bundle;  //NOT USED
import android.util.SparseArray;
import android.view.View;  //NOT USED
import android.util.Log;
import android.widget.ViewFlipper;  //NOT USED
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    Object irdaService;
    Method irWrite;
    SparseArray<String> irData; //this and previous two lines for IRDude hex2dec method

    private int[] Robosapien_Count = new int[100];
    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }*/
    private ViewFlipper viewFlipper;
    private float lastX;


    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = touchevent.getX();
                break;
            case MotionEvent.ACTION_UP:
                float currentX = touchevent.getX();

                // Handling left to right screen swap.
                if (lastX < currentX) {

                    // If there aren't any other children, just break.
                    if (viewFlipper.getDisplayedChild() == 0)
                    break;

                    // Next screen comes in from left.
                    viewFlipper.setInAnimation(this, R.anim.slide_in_from_left);

                    // Current screen goes out from right.
                    viewFlipper.setOutAnimation(this, R.anim.slide_out_to_right);

                    // Display next screen.
                    viewFlipper.showNext();
                }

                // Handling right to left screen swap.
                if (lastX > currentX) {

                    // If there is a child (to the left), kust break.
                    if (viewFlipper.getDisplayedChild() == 1)
                    break;

                    // Next screen comes in from right.
                    viewFlipper.setInAnimation(this, R.anim.slide_in_from_right);

                    // Current screen goes out from left.
                    viewFlipper.setOutAnimation(this, R.anim.slide_out_to_left);

                    // Display previous screen.
        viewFlipper.showPrevious();
                }
                break;
        }
        return false;
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

    //new code from IRDude hex2dec method

    ConsumerIrManager mCIR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        setContentView(R.layout.activity_main);

        //added from above onCreate(Bundle savedInstanceState)
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        //end from above comment

        ConsumerIrManager  mCIR = (ConsumerIrManager)getSystemService(CONSUMER_IR_SERVICE);
        Log.e(TAG,"mCIR.hasIrEmitter(): " + mCIR.hasIrEmitter());
        PackageManager pm = getPackageManager();
        Log.e(TAG,"pm.hasSystemFeature(PackageManager.FEATURE_CONSUMER_IR): " +
                pm.hasSystemFeature(PackageManager.FEATURE_CONSUMER_IR));
        FeatureInfo[] fi = pm.getSystemAvailableFeatures();
        for (int i=0;i<fi.length;i++){
            Log.e(TAG,"Feature: " + fi[i].name);
        }

        irData = new SparseArray<String>();
        irData.put(
                R.id.buttonWalkForward,
                hex2dec("0000 006A 0000 0000 0104 0082 0021 0021 0021 0021 0021 0021 0021 0021 0021 0082 0021 0082 0021 0021 0021 0fff 0000")); //0x86 or 1000 0110 in binary
        irData.put(
                R.id.buttonWalkBackward,
                hex2dec("0000 006A 0000 0000 0104 0082 0021 0021 0021 0021 0021 0021 0021 0021 0021 0082 0021 0082 0021 0082 0021 0fff 0000")); //0x87 or 1000 0111 in binary
        irData.put(
                R.id.buttonTurnRight,
                hex2dec("0000 006A 0000 0000 0104 0082 0021 0021 0021 0021 0021 0021 0021 0021 0021 0021 0021 0021 0021 0021 0021 0fff 0000")); //0x80 or 1000 0110 in binary
        irData.put(
                R.id.buttonTurnLeft,
                hex2dec("0000 006A 0000 0000 0104 0082 0021 0021 0021 0021 0021 0021 0021 0082 0021 0021 0021 0021 0021 0021 0021 0fff 0000")); //0x88 or 1000 1000 in binary
        irData.put(
                R.id.buttonStop,
                hex2dec("0000 006A 0000 0000 0104 0082 0021 0021 0021 0021 0021 0021 0021 0082 0021 0082 0021 0082 0021 0021 0021 0fff 0000")); //0x8E or 1000 1110 in binary

        irInit();

        // Get a reference to the ConsumerIrManager
        mCIR = (ConsumerIrManager) this.getSystemService(Context.CONSUMER_IR_SERVICE);

        setContentView(R.layout.consumer_ir);

        // Set the OnClickListener for the button so we see when it's pressed.
        findViewById(R.id.send_button).setOnClickListener(mSendClickListener);
    }

    public void irInit() {
        irdaService = this.getSystemService("irda");
        Class c = irdaService.getClass();
        Class p[] = { String.class };
        try {
            irWrite = c.getMethod("write_irsend", p);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void irSend(View view) {
        String data = irData.get(view.getId());
        if (data != null) {
            try {
                irWrite.invoke(irdaService, data);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    protected String hex2dec(String irData) {
        List<String> list = new ArrayList<String>(Arrays.asList(irData
                .split(" ")));
        list.remove(0); // dummy
        int frequency = Integer.parseInt(list.remove(0), 16); // frequency
        list.remove(0); // seq1
        list.remove(0); // seq2

        for (int i = 0; i < list.size(); i++) {
            list.set(i, Integer.toString(Integer.parseInt(list.get(i), 16)));
        }

        frequency = (int) (1000000 / (frequency * 0.241246));
        list.add(0, Integer.toString(frequency));

        irData = "";
        for (String s : list) {
            irData += s + ",";
        }
        return irData;
    }

    private static final int ROBOSAPIEN_FREQ = 39020;

    protected void setCount(String hex2dec){ //sets int array to the string that hex2dec returns 
        Scanner tempParser = new Scanner(hex2dec);
        int length = 0;
        while(tempParser.hasNextInt()){ //temp to get length for the for loop used later
            tempParser.nextInt();
            length++;
        }
        Scanner parser = new Scanner(hex2dec);
        parser.useDelimiter(",");

        if(parser.hasNextInt()) {
            parser.nextInt(); //skips first number which is frequency
        }

        for(int i=0;i<length;i++){
            Robosapien_Count[i] = parser.nextInt();
        }
    }


    View.OnClickListener mSendClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (!mCIR.hasIrEmitter()) {
                Log.e(TAG, "No IR Emitter found\n");
                return;
            }

            if (Build.VERSION.SDK_INT == 19) {
                int lastIdx = Build.VERSION.RELEASE.lastIndexOf(".");
                int VERSION_MR = Integer.valueOf(Build.VERSION.RELEASE.substring(lastIdx+1));
                if (VERSION_MR < 3) {
                    // Before version of Android 4.4.2
                    mCIR.transmit(ROBOSAPIEN_FREQ, Robosapien_Count);
                }
                /*else {
                    // Later version of Android 4.4.3
                    mCIR.transmit(SAMSUNG_FREQ, SAMSUNG_POWER_TOGGLE_DURATION);
                }*/
            }
        }
    };

    //BUTTON METHODS

    //FIRST TEMPLATE (NAVIGATION)

    //STOP
    public void onButtonClickStop(View V){
        Toast.makeText(MainActivity.this, "I AM STOPPING", Toast.LENGTH_SHORT).show();
    }
    //MOVE FORWARD
    public void onButtonClickForward(View V){
        Toast.makeText(MainActivity.this, "MOVING FORWARD", Toast.LENGTH_SHORT).show();
    }
    //MOVE BACKWARD
    public void onButtonClickBackward(View V){
        Toast.makeText(MainActivity.this, "MOVING BACKWARD", Toast.LENGTH_SHORT).show();
    }
    //MOVE LEFT
    public void onButtonClickLeft(View V){
        Toast.makeText(MainActivity.this, "MOVING LEFT", Toast.LENGTH_SHORT).show();
    }
    //MOVE RIGHT
    public void onButtonClickRight(View V){
        Toast.makeText(MainActivity.this, "MOVING RIGHT", Toast.LENGTH_SHORT).show();
    }
    //WHISTLE
    public void onButtonClickWhistle(View V){
        Toast.makeText(MainActivity.this, "*whistle*", Toast.LENGTH_SHORT).show();
    }
    //BURP
    public void onButtonClickBurp(View V){
        Toast.makeText(MainActivity.this, "*burp* EXCUSE ME!", Toast.LENGTH_SHORT).show();
    }
    //HIGH FIVE
    public void onButtonClickHighFive(View V){
        Toast.makeText(MainActivity.this, "HIGHT FIVE!", Toast.LENGTH_SHORT).show();
    }

    //SECOND TEMPLATE (STATIONARY MOVEMENT)

    // LEFT THROW
    public void onButtonClickLThrow(View V){
        Toast.makeText(MainActivity.this, "LEFT THROW", Toast.LENGTH_SHORT).show();
    }
    //RIGHT THROW
    public void onButtonClickRThrow(View V){
        Toast.makeText(MainActivity.this, "RIGHT THROW", Toast.LENGTH_SHORT).show();
    }
    //LEFT PICKUP
    public void onButtonClickLPickUp(View V){
        Toast.makeText(MainActivity.this, "LEFT PICKUP", Toast.LENGTH_SHORT).show();
    }
    //RIGHT PICKUP
    public void onButtonClickRPickUp(View V){
        Toast.makeText(MainActivity.this, "RIGHT PICKUP", Toast.LENGTH_SHORT).show();
    }
    //LEFT BUMP
    public void onButtonClickLBump(View V){
        Toast.makeText(MainActivity.this, "LEFT BUMP", Toast.LENGTH_SHORT).show();
    }
    //RIGHT BUMP
    public void onButtonClickRBump(View V){
        Toast.makeText(MainActivity.this, "RIGHT BUMP", Toast.LENGTH_SHORT).show();
    }
    //LEAN FORWARD
    public void onButtonClickLeanForward(View V){
        Toast.makeText(MainActivity.this, "LEAN FORWARD", Toast.LENGTH_SHORT).show();
    }
    //LEAN BACKWARD
    public void onButtonClickLeanBack(View V){
        Toast.makeText(MainActivity.this, "LEAN BACKWARD", Toast.LENGTH_SHORT).show();
    }

    //THIRD TEMPLATE (UTILITIES)

}

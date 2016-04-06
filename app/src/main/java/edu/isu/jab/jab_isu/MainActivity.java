package edu.isu.jab.jab_isu;

import android.annotation.TargetApi;
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
    
    private ViewFlipper viewFlipper;
    private float lastX;

    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {
            /*case MotionEvent.ACTION_DOWN:
                lastX = touchevent.getX();
//                break;*/
            case MotionEvent.ACTION_SCROLL:
                float currentX = touchevent.getX();

                // Handling left to right screen swap.
                if (lastX < currentX) {

                    // If there aren't any other children, just break.
                    if (viewFlipper.getDisplayedChild() == 0) {

                        // Next screen comes in from left.
                        viewFlipper.setInAnimation(this, R.anim.slide_in_from_left);

                        // Current screen goes out from right.
                        viewFlipper.setOutAnimation(this, R.anim.slide_out_to_right);
                    }
                    // Display next screen.
                    viewFlipper.showNext();
                }

                // Handling right to left screen swap.
                if (lastX > currentX) {

                    // If there is a child (to the left), kust break.
                    if (viewFlipper.getDisplayedChild() == 1) {

                        // Next screen comes in from right.
                        viewFlipper.setInAnimation(this, R.anim.slide_in_from_right);

                        // Current screen goes out from left.
                        viewFlipper.setOutAnimation(this, R.anim.slide_out_to_left);
                    }
                    // Display previous screen.
                    viewFlipper.showPrevious();
                }
                break;
            default:
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

    //IR code below: some taken from IRDude methods (https://github.com/rngtng/IrDude/blob/master/src/com/rngtng/irdude/MainActivity.java)
    Object irService;
    Method irWrite;
    SparseArray<String> irData;
    ConsumerIrManager mCIR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        setContentView(R.layout.activity_main);

        PackageManager pm = getPackageManager();
        FeatureInfo[] fi = pm.getSystemAvailableFeatures();

        irData = new SparseArray<String>();

        //walkForward button initialization
        String buttonWalkForwardHex = "0000 006A 0000 0000 0104 0082 0021 0021 0021 0021 0021 0021 0021 0021 0021 0082 0021 0082 0021 0021 0021 0fff 0000"; //0x86 or 1000 0110 in binary
        String buttonWalkForwardDecimal = hex2dec(buttonWalkForwardHex);
        String buttonWalkForwardDuration = count2duration(buttonWalkForwardDecimal);


        irData.put(
                R.id.buttonWalkForward,buttonWalkForwardDuration);
        irData.put(
                R.id.buttonWalkBackward,
                hex2dec("0000 006A 0000 0000 0104 0082 0021 0021 0021 0021 0021 0021 0021 0021 0021 0082 0021 0082 0021 0082 0021 0fff 0000"));
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
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void irInit() {
        // Get a reference to the ConsumerIrManager
        mCIR = (ConsumerIrManager)getSystemService(Context.CONSUMER_IR_SERVICE);
    }

    /**
     * Transmits the correct duration pulse pattern at the specific frequency for the given button press
     * @param view
     */
    public void irSend(View view) {
        String data = irData.get(view.getId());
        if (data != null) {
            String values[] = data.split(",");
            int[] pattern = new int[values.length - 1];

            for (int i = 0; i < pattern.length; i++) {
                pattern[i] = Integer.parseInt(values[i + 1]);
            }

            mCIR.transmit(Integer.parseInt(values[0]), pattern);
        }
    }

    /**
     * Converts given hex codes to decimal/count pattern
     * @param irData
     * @return
     *  Returns a new string of decimal/count pattern
     */
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

    /**
     * Converts decimal/count pattern to the duration needed for Android versions above 4.4.3
     * @param countPattern
     * @return
     *  Returns a new string of duration pattern to be transmitted on a button press
     */
    protected String count2duration(String countPattern) {
        List<String> list = new ArrayList<String>(Arrays.asList(countPattern.split(",")));
        int frequency = Integer.parseInt(list.get(0));
        int pulses = 1000000/frequency;
        int count;
        int duration;

        //removes frequency
        list.remove(0);
        //removes 1st value
        list.remove(0);

        for (int i = 0; i < list.size(); i++) {
            count = Integer.parseInt(list.get(i));
            duration = count * pulses;
            list.set(i, Integer.toString(duration));
        }

        String durationPattern = "";
        for (String s : list) {
            durationPattern += s + ",";
        }

        return durationPattern;
    }
    //End of IR code (some being from IRDude: https://github.com/rngtng/IrDude/blob/master/src/com/rngtng/irdude/MainActivity.java)


    
    //BUTTON METHODS

    //FIRST TEMPLATE (NAVIGATION)

    //STOP
    public void onButtonClickStop(View V){
        Toast.makeText(MainActivity.this, "I AM STOPPING", Toast.LENGTH_SHORT).show();
    }
    //MOVE FORWARD
    public void onButtonClickForward(View V){
        irSend(findViewById(R.id.buttonWalkForward));
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
        Toast.makeText(MainActivity.this, "LEFT THROWING", Toast.LENGTH_SHORT).show();
    }
    //RIGHT THROW
    public void onButtonClickRThrow(View V){
        Toast.makeText(MainActivity.this, "RIGHT THROWING", Toast.LENGTH_SHORT).show();
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

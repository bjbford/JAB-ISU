package edu.isu.jab.jab_isu;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import android.app.Activity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    Object irdaService;
    Method irWrite;
    SparseArray<String> irData; //this and previous two lines for IRDude hex2dec method

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

    public void onButtonClick(View V){
        Toast.makeText(MainActivity.this, "HELLO WORLD", Toast.LENGTH_SHORT).show();
    }

    //new code from IRDude hex2dec method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    private static final int ROBOSAPIEN_FREQ = 38028;


}

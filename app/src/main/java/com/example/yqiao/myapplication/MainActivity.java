package com.example.yqiao.myapplication;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ParseInstallation;
import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseAnalytics;
import com.parse.ParseQuery.CachePolicy;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener, OnClickListener {

    private TextView xText, yText, zText, time;
    private Button startButton, stopButton, FeatureExtractor;
    private Sensor accSensor;
    private SensorManager accManager;
    private static final String TAG = MainActivity.class.getName();
    private static final String NEWFILE = "File.txt";
    private ArrayList<ArrayList> data;
    private OutputStreamWriter outputStreamWriter;
    private String tuple = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Parse.initialize(this, "imlARBE", "freeLunch");
        ParseObject.registerSubclass(Task.class);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser == null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        accManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accSensor = accManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        xText = (TextView)findViewById(R.id.xText);
        yText = (TextView)findViewById(R.id.yText);
        zText = (TextView)findViewById(R.id.zText);
        time = (TextView)findViewById(R.id.Time);

        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        FeatureExtractor = (Button) findViewById(R.id.FeatureExtractor);

        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        FeatureExtractor.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view.equals(startButton)) {
            accManager.registerListener(this, accSensor, 50000);
            data = new ArrayList<ArrayList>();
        }

        if (view.equals(stopButton)) {
            accManager.unregisterListener(this);

        }

        if (view.equals(FeatureExtractor)) {
            FeatureExtractor extractor = new FeatureExtractor();
            LinkedList<TupFeat> temp = extractor.featureExtractor(data);
            writeData(temp);
            Log.e(TAG, "retrieving parse object: " + tuple);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Do something if sensor accuracy changes
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        long t = System.currentTimeMillis();

        xText.setText("X: " + x);
        yText.setText("Y: " + y);
        zText.setText("Z: " + z);
        time.setText("Time: " + t);

        ArrayList output = new ArrayList();
        output.add("21");
        output.add("NoLabel");
        output.add(t);
        output.add(x);
        output.add(y);
        output.add(z);

        //save to memory, write to file on stop
        //list array
        data.add(output);
        Log.e(TAG, "Data added to file");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * prints the data in the result set to the file
     */
    public void writeData(LinkedList<TupFeat> que) {
        Log.e(TAG, "write data function entered ");
        // Temporary tuple variables
        String tuple = "";
        TupFeat tup = null;
        float[] f = null;
        int c = 0;
        ParseObject featExtractData = new ParseObject("FeatExtractData");

        try {
            outputStreamWriter = new OutputStreamWriter(openFileOutput(NEWFILE, Context.MODE_APPEND));

            while (!que.isEmpty()) {
                tup = que.pop();
                f = tup.getFeat();
                tuple = "";
                tuple += c++;
                tuple += ",";
                char tmp = tup.getAct();

                tuple += tmp;
                tuple += ",";
                for (int i = 0; i < 43; i++) { // the data itself
                    tuple += f[i];
                    tuple += ",";
                }
                tuple += tup.getUsr(); // column 2 is userid
                Log.e(TAG, tuple);
                featExtractData.put("tuple", tuple);

                try {
                    outputStreamWriter.write(tuple + "\n");
                    Log.e(TAG, "tuple written to file");
                    tuple = null;
                } catch (IOException e) {
                    Log.e(TAG, "Failed to write tuple to file: " + e.toString());
                }
            }
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e(TAG, "failed to close");
        }

        featExtractData.saveInBackground();
    }


}

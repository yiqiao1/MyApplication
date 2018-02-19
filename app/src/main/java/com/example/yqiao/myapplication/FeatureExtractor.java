package com.example.yqiao.myapplication;

import android.app.Activity;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by yqiao on 2/8/18.
 */

public class FeatureExtractor extends Activity {

    private static final String TAG = MainActivity.class.getName();


    private static double[] bins = new double[30];
    private static String[] usrList = null;
    private static String[] actList = null;

    // windowSize = number of seconds for window frame
    private static int windowSize = 10;

    // windowSize*20 entries is this much change in timestamps
    private static long duration = windowSize*1000;

    private static LinkedList<TupFeat> que = new LinkedList<TupFeat>();

    //samplingRate = Hz (number of samples collected per second)
    //currently use 20 Hz sampling rate
    private static int samplingRate = 20;

    private static int usrCount = 0;



    /**
     * @param data
     */
    public LinkedList<TupFeat> featureExtractor(ArrayList<ArrayList> data) {
        bins[0] = -2.5;
        bins[1] = 0;
        bins[2] = 2.5;
        bins[3] = 5;
        bins[4] = 7.5;
        bins[5] = 10;
        bins[6] = 12.5;
        bins[7] = 15.0;
        bins[8] = 17.5;
        bins[9] = 20;
        bins[10] = -2.5;
        bins[11] = 0;
        bins[12] = 2.5;
        bins[13] = 5;
        bins[14] = 7.5;
        bins[15] = 10;
        bins[26] = 12.5;
        bins[17] = 15.0;
        bins[18] = 17.5;
        bins[19] = 20;
        bins[20] = -2.5;
        bins[21] = 0;
        bins[22] = 2.5;
        bins[23] = 5;
        bins[24] = 7.5;
        bins[25] = 10;
        bins[26] = 12.5;
        bins[27] = 15.0;
        bins[28] = 17.5;
        bins[29] = 20;


        usrList = new String[50];
        actList = new String[]{"NoLabel", "Walking", "Jogging", "Stairs",
                "Sitting", "Standing", "LyingDown"};


        try {
            readData(data);
        } catch (IOException e) {
            Log.e(TAG, "Error reading file. Operation aborted." + e.getMessage());
        }

        TupFeat tmp = null;
        for(int i = 0; i < que.size(); i++){
            tmp = que.get(i);
            FeatureLib.processTup(tmp, bins);

        }

        return que;
    }

    /**
     * this function is absurdly long.
     * @param data
     * @throws IOException
     */
    public void readData(ArrayList<ArrayList> data) throws IOException{

        Log.e(TAG, "Started reading file ");

        String cusr = "joeUnreal"; // user of current tuple
        String cact = null; // activity of current tuple

        float[] x = new float[(windowSize*samplingRate)]; // holds the accelerometer data for a single tuple
        float[] y = new float[(windowSize*samplingRate)];
        float[] z = new float[(windowSize*samplingRate)];

        ArrayList last = null;
        long cTime = 0, tmpt = 1, lastTime = 0; // time of start of current tuple, and temp time
        long[] t = new long[(windowSize*samplingRate)];
        int i = 0; // counter for tuple members
        int abCount = 0; //abandoned tuple count
        int savTCount = 0; //saved tuple count
        int repCount = 0;

        for (ArrayList values : data) {

            try{

                if(values.equals(last)){
                    repCount++;
                    continue; // skip repeated input
                } else{
                    last = values; // we know the line is good

                    if(!cusr.equals(values.get(0))){ // if the user changes

                        Log.e(TAG, "current user is " + values.get(0) + " starting new tuple ");

                        if (i >= windowSize*0.9*samplingRate){ // save no tuples fewer than 90% records long
                            Log.e(TAG, "saving tuple because user changed ");
                            savTCount++;
                            TupFeat tup = new TupFeat(Long.valueOf(cusr), cact.charAt(0), cTime);

                            // all arrays must be copied into new ones because java is pass by reference always
                            float[] xt = new float[(windowSize*samplingRate)], yt = new float[(windowSize*samplingRate)], zt = new float[(windowSize*samplingRate)];
                            long[] tt = new long[(windowSize*samplingRate)];
                            for(int j = 0; j<(windowSize*samplingRate); j++){
                                xt[j] = x[j];
                                yt[j] = y[j];
                                zt[j] = z[j];
                                tt[j] = t[j];
                            }

                            tup.setRaw(xt, yt, zt, tt);
                            tup.setCount(i);
                            que.add(tup);
                        } else{
                            abCount++;
                        }
                        cusr = values.get(0).toString();
                        usrList[usrCount] = cusr;
                        usrCount++;
                        cact = values.get(1).toString();
                        cTime = (long)values.get(2);
                        i = 0; // reset count

                    } else if(!cact.equals(values.get(1))){ // if the activity changes
                        if (i >= (windowSize*0.9*samplingRate)){ // save no tuples fewer than 180 records long
                            Log.e(TAG, "saving tuple because activity changed ");
                            savTCount++;
                            TupFeat tup = new TupFeat(Long.valueOf(cusr), cact.charAt(0), cTime);

                            // all arrays must be copied into new ones because java is pass by reference always
                            float[] xt = new float[(windowSize*samplingRate)], yt = new float[(windowSize*samplingRate)], zt = new float[(windowSize*samplingRate)];
                            long[] tt = new long[(windowSize*samplingRate)];
                            for(int j = 0; j<(windowSize*samplingRate); j++){
                                xt[j] = x[j];
                                yt[j] = y[j];
                                zt[j] = z[j];
                                tt[j] = t[j];
                            }

                            tup.setRaw(xt, yt, zt, tt);
                            tup.setCount(i);
                            que.add(tup);
                        } else {
                            abCount++;
                        }
                        cact = values.get(1).toString();
                        cTime = (long)values.get(2);
                        i=0; // reset count
                    } else { // if the activity and the user are both the same still
                        tmpt = (long)values.get(2);
                        // make sure it's not a repeat or null line, and also check that it's within 10 seconds of tuple start

                        if(tmpt <= (cTime + duration)){
                            Log.e(TAG, "time within 10 seconds of tuple start");
                            if (tmpt != lastTime && tmpt != 0){
                                // extract the floating point number from the string we read from the file
                                // store it as an x, y, or z value
                                x[i] = (float)values.get(3);
                                y[i] = (float)values.get(4);
                                z[i] = (float)values.get(5);
                                t[i] = tmpt;
                                lastTime = tmpt;
                                i++;
                            }
                        } else if(i >= (windowSize*0.9*samplingRate)){
                            savTCount++;
                            TupFeat tup = new TupFeat(Long.valueOf(cusr), cact.charAt(0), cTime);
                            tup.setRaw(x, y, z, t);
                            tup.setCount(i);
                            cTime = (long)values.get(2);;
                            que.add(tup);
                            i = 0; //reset count
                        } else{ // start new tuple
                            abCount++;
                            i = 0;
                            cTime = (long)values.get(2); // set time to begin next tuple
                        }
                        if(i == (windowSize*samplingRate)){ // if we reach (windowSize*samplingRate) samples, then the windowSize tuple is done and should be saved
                            savTCount++;

                            TupFeat ttup = new TupFeat(Long.valueOf(cusr), cact.charAt(0), cTime);
                            ttup.setCount(i);

                            // all arrays must be copied into new ones because java is pass by reference always
                            float[] xt = new float[(windowSize*samplingRate)], yt = new float[(windowSize*samplingRate)], zt = new float[(windowSize*samplingRate)];
                            long[] tt = new long[(windowSize*samplingRate)];
                            for(int j = 0; j<(windowSize*samplingRate); j++){
                                xt[j] = x[j];
                                yt[j] = y[j];
                                zt[j] = z[j];
                                tt[j] = t[j];
                            }

                            ttup.setRaw(xt, yt, zt, tt);
                            que.add(ttup);

                            cTime = (long)values.get(2); // set time to begin next tuple
                            i = 0; // reset count
                        } // end if
                    } // end else
                } // end else
            } catch(ArrayIndexOutOfBoundsException a){
                Log.e(TAG, "bad line found");
                continue;
            }
        } // end while

        Log.e(TAG, "Abandoned tuple count = " + abCount);
        Log.e(TAG, "saved tuple count = " + savTCount);
        Log.e(TAG, "Repeated lines: " + repCount);

    }



}

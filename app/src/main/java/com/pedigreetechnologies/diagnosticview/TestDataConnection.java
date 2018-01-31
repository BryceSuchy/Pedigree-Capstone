package com.pedigreetechnologies.diagnosticview;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Joe on 2/23/2017.
 */

public class TestDataConnection extends Thread implements IDataConnection{

    LinkedBlockingQueue<SensorMessage> messageQueue;
    String TAG = "TestDataConnection";
    BufferedReader reader;
    AssetManager manager;
    int messageDelay = 0;

    public TestDataConnection(Context context) {
        //get assests manager which has the test file stored in it
        manager = context.getResources().getAssets();
        messageQueue = new LinkedBlockingQueue<>();
    }

    public void setMessageDelay(int messageDelay) {
        this.messageDelay = messageDelay;
    }

    @Override
    public void run() {
        Log.v(TAG, "Starting Thread");

        //<SCAN>(anything or the message)</SCAN>
        Pattern pattern = Pattern.compile("(3c5343414e3e)(.*?)(3c2f5343414e3e)");
        Matcher matcher;
        String readLine;

        try {
            //Open and read the test file while it contains lines
            reader = new BufferedReader(new InputStreamReader(manager.open("test-data.txt")));
            while ((readLine = reader.readLine()) != null) {
                matcher = pattern.matcher(readLine);
                if (matcher.find()) {
                    messageQueue.put(new SensorMessage(matcher.group(2), System.currentTimeMillis()));
                }
                Thread.sleep(messageDelay);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.v("TestData", "read error");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "Thread done, QueueSize: " + messageQueue.size());
    }

    public SensorMessage readMessage() {
        try {
            return messageQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}

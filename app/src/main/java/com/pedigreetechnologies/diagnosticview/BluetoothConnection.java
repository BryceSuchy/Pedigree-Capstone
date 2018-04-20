package com.pedigreetechnologies.diagnosticview;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BluetoothConnection extends Thread implements IDataConnection {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    LinkedBlockingQueue<SensorMessage> messageQueue;
    private byte[] mmBuffer; // mmBuffer store for the stream
    private Thread heartBeatThread;
    private String TAG = "BluetoothConnection";

    public BluetoothConnection(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        messageQueue = new LinkedBlockingQueue<>();

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        //Thread sends keep alive message every 30 seconds
        heartBeatThread = new Thread() {
            public void run() {
                Log.v(TAG, "Starting Heartbeat Thread");

                while (true) {
                    try {
                        write("<SCAN>L</SCAN>".getBytes());
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public void run() {
        Log.v(TAG, "Starting Thread");

        mmBuffer = new byte[1024];
        int bytesRead; // bytes returned from read()

        //<SCAN>(anything or the message)</SCAN>
        Pattern pattern = Pattern.compile("(3c5343414e3e)(.*?)(3c2f5343414e3e)");
        Matcher matcher;
        String readStr = "";
        boolean repeat;

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                repeat = true;
                // Read from the InputStream.
                bytesRead = mmInStream.read(mmBuffer);
                byte[] messageArray = new byte[bytesRead];

                //Logging bytestream data
                String str = "";
                for (int i = 0; i < bytesRead; i++) {
                    str += String.format("%02x", mmBuffer[i]);
                    messageArray[i] = mmBuffer[i];
                }
                //Log.v(TAG, str);

                readStr += str;

                while (repeat) {
                    //Append new message an check if there is a completed message in the buffer
                    matcher = pattern.matcher(readStr);
                    if (matcher.find()) {
                        messageQueue.put(new SensorMessage(matcher.group(2), System.currentTimeMillis()));
                        int cutlen = readStr.indexOf(matcher.group(0)) + matcher.group(0).length();
                        readStr = readStr.substring(cutlen);
                    } else {
                        repeat = false;
                    }
                }

                //Converts messageArray to a string and will print it to the logs
                /*String input = new String(messageArray, "UTF-8");
                Log.v(TAG, "Message as string: " + input);*/

            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Call this from the main activity to send data to the remote device.
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);
        }
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            if (mmSocket != null && mmOutStream != null) {
                write("<SCAN>O</SCAN>".getBytes());
                mmSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }

    @Override
    public void start() {
        super.start();
        heartBeatThread.start();
    }

    @Override
    public SensorMessage readMessage() {
        try {
            return messageQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
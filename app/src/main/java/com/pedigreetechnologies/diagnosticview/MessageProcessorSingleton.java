package com.pedigreetechnologies.diagnosticview;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Joe on 4/2/2017.
 */

public class MessageProcessorSingleton extends Thread{

    IDataConnection dataConnection;
    ArrayList<DiagnosticParameter> parameterList;
    String TAG = "MessageProcessor";
    boolean hasListShortened = false;
    AllGraphDataSingleton allGraphData;

    SensorMessage readMessage;
    String readString = "";
    long readTime = 0;
    int messageDataType;
    int parameterID;
    //int source; //Currently unused
    String messageData;
    DiagnosticParameter tempParameter;
    Handler messageHandler;

    private static final MessageProcessorSingleton ourInstance = new MessageProcessorSingleton();

    public static MessageProcessorSingleton getInstance() {
        return ourInstance;
    }

    private MessageProcessorSingleton() {
        allGraphData = AllGraphDataSingleton.getInstance();
    }

    public void setDataConnection(IDataConnection dataConnection) {
        this.dataConnection = dataConnection;
    }

    public void setParameterList(ArrayList<DiagnosticParameter> parameterList) {
        this.parameterList = parameterList;
    }

    public void setMessageHandler(Handler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void run() {

        Log.v(TAG, "Starting Thread");

        //Try to shorten the fullParameterList first
        do {
            while(dataConnection == null || parameterList == null){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            readMessage = dataConnection.readMessage();

            readString = readMessage.getMessage();
            readTime = readMessage.getTimeReceived();
            //first byte of message is the data type
            messageDataType = Integer.parseInt(readString.substring(0, 2), 16);
            shortenParamterList(messageDataType);

            allGraphData.buildMapAndDataList(parameterList, readTime);

            processMessage(readString, readTime);
            //After shortening the list the handler should be changed so it is null until the new handler is applied
            //messageHandler = null;
        }while (readString.length() <= 8 && !hasListShortened);

        //Continue to process messages
        while (true) {

            //Wait until the parameter list and the message parser are assigned
            if (dataConnection == null || parameterList == null) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            readMessage = dataConnection.readMessage();
            readString = readMessage.getMessage();
            readTime = readMessage.getTimeReceived();
            processMessage(readString,readTime);
        }
    }

    public void processMessage(String message, long timeReceived){
        //If message is less than 4 bytes, length 8, it is not a complete message
        if(message.length() <= 8){
            return;
        }
        //first byte of message is the data type
        messageDataType = Integer.parseInt(message.substring(0, 2), 16);

        //ID is the next two bytes in the message
        parameterID = Integer.parseInt(message.substring(2, 6), 16);

        //source = Integer.parseInt(readString.substring(6, 8), 16); //source not used

        //Message is the remaining string
        messageData = message.substring(8, message.length());

        if(messageData.contains("3c5343414e3e") || parameterList.size() <= messageDataType){
            return;
        }

        //Log.v(TAG, "MessageData: " + messageData);
        for (int i = 0; i < parameterList.size(); i++) {
            tempParameter = parameterList.get(i);
            if (tempParameter.getDataType() == messageDataType && tempParameter.getParameterID() == parameterID) {

                //Message is composed of the label,calculatedvalue,timereceived
                float measurement = tempParameter.calcMeasurement(messageData);

                if(Float.isNaN(measurement)){
                    continue;
                }
                //Log.v(TAG, "Label: " + tempParameter.getLabel() + " Measurement: " + measurement);
                allGraphData.addGraphEntry(tempParameter.getLabel(), measurement, timeReceived);

                if(messageHandler != null){
                    Message handlerMessage = Message.obtain();
                    handlerMessage.obj = tempParameter.getLabel() + "," + measurement + "," + timeReceived;
                    messageHandler.sendMessage(handlerMessage);
                }
            }
        }
    }

    public void shortenParamterList(int dataType){
        ArrayList<DiagnosticParameter> parameters = new ArrayList<>();

        //Get only the values of the most recent data type collected as the sensor will only send one type
        for(int i = 0; i < parameterList.size(); i++){
            if(parameterList.get(i).getDataType() == dataType)
            {
                parameters.add(parameterList.get(i));
            }
        }

        parameterList = parameters;
        hasListShortened = true;
    }

    public ArrayList<DiagnosticParameter> getParameterList(){
        if(!hasListShortened){
            return null;
        }

        return parameterList;
    }
}

package com.pedigreetechnologies.diagnosticview;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by adam on 4/9/18.
 *
 * This is the activity responsible for taking the data for each metric and writing it to a CSV file. The file will then be uploaded to
 * Pedigree's servers. This activity will launch when the Export button is pushed on the app
 */

public class CsvExport {

    //Each item in this arraylist is an arrayList of all the sensor data points for a specific metric. Its the list of all sensorDataPoint lists
    private ArrayList<ArrayList<SensorDataPoints>> masterList;

    //the singleton object where all the data is stored
    private AllGraphDataSingleton allGraphDataSingleton;

    private File csvFile;
    private String fileName;
    private FileOutputStream outputStream;
    final String newLine = "\n";

    /*
    This method actually generates the CSV file in the directory.
    Default: data/user/0/com.pedigreetechnologies.diagnosticview/~~file~~
     */
    public void generateCSV(Context context, ArrayList<DiagnosticParameter> selectedParameters){


        //make file and file stream objects here
        //TODO this name should be programatically made somehow. User created or maybe a date UID format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DATE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        fileName = year + "_" + month + "_" + date + "_" + hour + "_" + minute + "_" + second + "_" + ".csv";

        allGraphDataSingleton = AllGraphDataSingleton.getInstance();
        masterList = new ArrayList<>(50);

        //for each item in the parameter list, make a list of the data and add it to the masterList
        for(int i = 0; i < selectedParameters.size(); i++){
            //set each item in the master list to be a list of sensorDataPoints taken from getGraphData(); key is the label for the data
            masterList.add(allGraphDataSingleton.getGraphData(selectedParameters.get(i).getLabel()));

        }

        FileOutputStream outputStream;
        try {
            File file = new File(context.getFilesDir(), fileName);
            PrintWriter pw = new PrintWriter(file);
            pw.write("++--------------------[Data Value]{Time Value}----------------------++");
            pw.write("\n");

            for(int i = 0; i<selectedParameters.size(); i++){
                System.out.println();
                System.out.println("MMMMMMMMMMMMAAAAAAAAAAADDDDDDDDDDDDDEEEEEEEEEE-------------------------------------------------------------------------------------------------------");
                System.out.println();
                ArrayList<SensorDataPoints> loopList = masterList.get(i);
                pw.write(selectedParameters.get(i).getLabel() + " ");

                for(int j = 0; j < loopList.size(); j++){
                    pw.write(loopList.get(j).toString());
                    pw.write(",");
                }

                pw.write("\n");

            }

            pw.write("++----------------------------------End File Writing-----------------------++");
            pw.close();
        }
        catch(IOException fnf){
            System.out.print("Error writing to File");
        }

            CharSequence messageText = "Exporting CSV file!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, messageText, duration);
            toast.show();



        }


}

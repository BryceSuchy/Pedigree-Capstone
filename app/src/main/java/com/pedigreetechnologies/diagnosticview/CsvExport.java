package com.pedigreetechnologies.diagnosticview;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by adam on 4/9/18.
 *
 * This is the activity responsible for taking the data for each metric and writing it to a CSV file. The file will then be uploaded to
 * Pedigree's servers. This activity will launch when the Export button is pushed on the app
 */

public class CsvExport {

    private ArrayList<DiagnosticParameter> selectedParameterList;

    //Each item in this arraylist is an arrayList of all the sensor data points for a specific metric. Its the list of all sensorDataPoint lists
    private ArrayList<ArrayList<SensorDataPoints>> masterList;

    private AllGraphDataSingleton allGraphDataSingleton;

    private File csvFile;
    private String fileName;
    private FileOutputStream outputStream;
    final String newLine = "\n";

    /*
    This method actually generates the CSV file in the directory
    //TODO figure out where exactly the file is being created. data/user/0/com.pedigre.../files ???
     */
    public void generateCSV(Context context){

        System.out.print("----------------------------------------------------------------------METHOD FIRED");


        //make file and file stream objects here
        //TODO this name should be programatically made somehow. User created or maybe a date UID format
        fileName = "TestFileTester.csv";

        masterList = new ArrayList<>(50);

        //TODO get all selected parameters - store in selectedParameterList
        //Get the parameters from the parent activity
//        Bundle extras = getArguments();
//        if (extras != null) {
//            selectedParameterList = extras.getParcelableArrayList("selectedParameterList");
//

        //grab an arraylist of sensor data points for each metric from the allGraphDataSingleton thing
        //each row in the file will have all the data from the sensor data points array for that metric
        //return the file
        allGraphDataSingleton = AllGraphDataSingleton.getInstance();

        //small test--------------------------//
        ArrayList<SensorDataPoints> testList1 = new ArrayList<>();
        testList1 = allGraphDataSingleton.getGraphData("Oil Temperature");
        for(int i =0; i< testList1.size(); i++){
            System.out.println(testList1.get(i).toString());
        }

        ArrayList<SensorDataPoints> testList2 = new ArrayList<>();
        testList2 = allGraphDataSingleton.getGraphData("Odometer - High Res (Engine)");

        ArrayList<SensorDataPoints> testList3 = new ArrayList<>();
        testList3 = allGraphDataSingleton.getGraphData("Total Engine Fuel Used");

        ArrayList<ArrayList<SensorDataPoints>> masterTestList = new ArrayList<>(3);
        masterTestList.add(testList1);
        masterTestList.add(testList2);
        masterTestList.add(testList3);

        FileOutputStream outputStream;
        try {
            File file = new File(context.getFilesDir(), "testFileYAAAAAAA.csv");
            PrintWriter pw = new PrintWriter(file);
            pw.write("++--------------------Beging File Writing----------------------++");
            pw.write("\n");

            for(int i = 0; i<masterTestList.size(); i++){
                ArrayList<SensorDataPoints> loopList = masterTestList.get(i);
                for(int j = 0; j < loopList.size(); j++){
                    pw.write(loopList.get(j).toString());
                    pw.write(",");
                }
                pw.write("\n");
            }
            pw.write("++----------------------------------End File Writing-----------------------++");
            pw.close();
        }
        catch(FileNotFoundException fnf){

        }
        //---------------------------------------//



        //for each item in the parameter list, make a list and add it to the masterList
//        for(int i = 0; i < selectedParameterList.size(); i++){
//
//            //set each item in the master list to be a list of sensorDataPoints taken from getGraphData(); key is the label for the data
//            masterList.set(i, allGraphDataSingleton.getGraphData(selectedParameterList.get(i).getLabel()));
//
//
//        }
//
//            try {
//                //open output stream
//                outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
//
//                ArrayList<SensorDataPoints> currentList;
//                for (int j = 0; j < masterList.size(); j++) {
//
//                    //will iterate and grab each list in the masterList (aka the list of data points for each metric)
//                    currentList = masterList.get(j);
//
//                    for (int k = 0; k < currentList.size(); k++) {
//                        //TODO consider using a printWriter or FileWriter if the bits are getting messed up
//                        String currentDataPoint = currentList.get(k).toString();
//                        /*debug */System.out.println("===========================================================\n" + "data " + currentDataPoint);
//                        String writeString = currentDataPoint + ",";
//                        outputStream.write(writeString.getBytes());
//
//                    }
//                    //after each list has been written, write a new line
//                    outputStream.write(newLine.getBytes());
//
//                }
//                outputStream.close();
//            }catch(IOException ioe)
//            {
//
//            }

        }


}

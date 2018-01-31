package com.pedigreetechnologies.diagnosticview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Joe on 2/16/2017.
 */

public class CSVReader
{
    public static ArrayList<String[]> readIgnoreHeader(InputStreamReader inputStreamReader){
        ArrayList<String[]> list = new ArrayList();
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        try{
            String line;
            bufferedReader.readLine();
            while((line = bufferedReader.readLine()) != null){
                String[] splitLine = line.split(",");
                list.add(splitLine);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

        try {
            inputStreamReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static ArrayList<String[]> read(InputStreamReader inputStreamReader){
        ArrayList<String[]> list = new ArrayList();
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        try{
            String line;
            while((line = bufferedReader.readLine()) != null){
                String[] splitLine = line.split(",");
                list.add(splitLine);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

        try {
            inputStreamReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }
}

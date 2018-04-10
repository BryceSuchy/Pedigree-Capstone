package com.pedigreetechnologies.diagnosticview;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;


import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabChartFragment extends Fragment {

    //new padding var
    public int paddingLength = 7;
    View lineView;
    View view = null;
    //Max graph scale in # of milliseconds 5 minutes * 60 seconds * 1000 to m/s
    long graphMax = 1 * 30 * 1000;
    //Issue with graph crashing when there is no data in the set, this is removed after the graph has more data
    Entry emptyEntryPlaceholder = (new Entry(0, 0));
    ScrollView scrollView;
    //Singleton containing all graph data
    private AllGraphDataSingleton allGraphDataSingleton;
    //Parameters to be displayed, selected in previous activity
    private ArrayList<DiagnosticParameter> selectedParameterList = new ArrayList(50);
    private ArrayList<DiagnosticParameter> selectedParameterListTemp;
    //Contains the location of the graphs stored in lineChartArrayList by name
    private HashMap<String, Integer> graphIndexMap;
    //Data entries for the graphs
    private ArrayList<ArrayList<Entry>> dataEntries;
    //Contains the charts so the parents don't need to be searched to find the graphs
    private ArrayList<LineChart> lineChartArrayList;
    //Parent layout that contains the graphs
    private LinearLayout graphLinearLayout;
    private long referenceTime;
    //Handler and thread to update the graphs
    private Handler timerHandler;
    // Number of updates per second ex: 1000/4 = approx 4 times a second
    private int updateTime = 1000 / 2;
    //for use in creating the csv file with the data
    private CellProcessor[] processors = null;
    private String[] headers;
    //TODO consider putting this declaration elsewhere
    private File csvFile;
    private FileWriter csvFileWriter;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }

        scrollView = (ScrollView) inflater.inflate(R.layout.tab_chart_layout, container, false);

        allGraphDataSingleton = AllGraphDataSingleton.getInstance();
        dataEntries = new ArrayList<>();
        lineChartArrayList = new ArrayList<>();
        graphLinearLayout = (LinearLayout) scrollView.findViewById(R.id.graphListLayout);
        graphIndexMap = new HashMap<>();

        timerHandler = new Handler();
        timerHandler.postDelayed(new Runnable() {
            public void run() {
                updateGraphs();
                timerHandler.postDelayed(this, updateTime);
            }
        }, updateTime);

        //Get the parameters from the parent activity
        Bundle extras = getArguments();
        if (extras != null) {
            selectedParameterListTemp = extras.getParcelableArrayList("selectedParameterList");

            for (int i = 0; i < selectedParameterListTemp.size(); i++) {
                DiagnosticParameter tempParm = selectedParameterListTemp.get(i);
                if (Double.isNaN(tempParm.getMin()) || Double.isNaN(tempParm.getMax())) {
                } else {
                    selectedParameterList.add(selectedParameterListTemp.get(i));
                }
            }

        }

        //Build the initial graphs from the selected parameters
        if (selectedParameterList != null) {
            long currentTime = System.currentTimeMillis();
            SensorDataPoints[] latestValues;
            String dataLabel = "";
            SensorDataPoints dataPoint;

            //Reset the map that keeps track of the data already displayed
            allGraphDataSingleton.resetLastIndexReadMap();

            for (int i = 0; i < selectedParameterList.size(); i++) {
                //Keep track of the graph index so you can find the data later
                graphIndexMap.put(selectedParameterList.get(i).getLabel(), i);
                dataEntries.add(new ArrayList<Entry>());

                //Keep track of the values added to the graph
                int entryCount = 0;

                //Get the data from the given graph label
                dataLabel = selectedParameterList.get(i).getLabel();
                latestValues = allGraphDataSingleton.getDataForUpdatePeriod(dataLabel, currentTime);

                //Add all values to the graphs
                for (int j = 0; j < latestValues.length; j++) {
                    dataPoint = latestValues[j];
                    dataEntries.get(i).add(new Entry(dataPoint.getTime(), dataPoint.getDataPoint()));
                    entryCount++;
                }

                //If no entries were added to the graph add a placeholder value to stop the graph from crashing
                if (entryCount == 0) {
                    dataEntries.get(i).add(emptyEntryPlaceholder);
                }
                Log.v("ToStrings", selectedParameterList.get(i).toString());
            }
            createGraphs(currentTime);

            //begin code to create the cell processors. The Cell processors are used when creating the csv report
            //make a processor for each selected parameter - using optional() for all right now
            processors = new CellProcessor[selectedParameterList.size()];
            for(int k = 0; k < selectedParameterList.size(); k++){
                processors[k] = new Optional();
            }

            headers = new String[selectedParameterList.size()];
            //make a header for each diagnostic parameter based on the label
            for(int n=0; n < selectedParameterList.size(); n++){
                headers[n] = selectedParameterList.get(n).getLabel();
            }

        }

        //create new file to write the CSV to
//        try {
           csvFile = new File(getContext().getFilesDir(), "testCSV.csv");
//            //FileWriter csvFileWriter = new FileWriter(csvFile);
//        }
//        catch(IOException ioe){
//            System.out.println("Error opening the file writer for the CSV file");
//        }


        return scrollView;
    }

    private void updateGraphs() {
        LineChart chart;
        LineData lineData;
        ILineDataSet set;
        SensorDataPoints[] latestValues;
        long currentTime = System.currentTimeMillis();
        String dataLabel = "";
        int graphIndex = 0;
        SensorDataPoints dataPoint;
        int numberOfNewValues;

        long adjustedTime = currentTime - referenceTime;

        //this is for the csv record. Store the data in a map first then after write to the file. Google supercsv csvMapWriter for more info
        final Map<String, Object> csvMap = new HashMap<String, Object>();


        //Go through every graph and update the data if it is present
        for (int i = 0; i < selectedParameterList.size(); i++) {
            //Get current graph label
            dataLabel = selectedParameterList.get(i).getLabel();
            //
            latestValues = allGraphDataSingleton.getDataForUpdatePeriod(dataLabel, currentTime);

            //Get the index of the current graph in the parent view
            graphIndex = graphIndexMap.get(dataLabel);
            //Get the graph to update
            chart = lineChartArrayList.get(graphIndex);

            //Get the data to update
            lineData = chart.getLineData();
            set = lineData.getDataSetByIndex(0);

            numberOfNewValues = latestValues.length;

            if (numberOfNewValues > 0) {
                //Remove the placeholder if it is present
                set.removeEntry(emptyEntryPlaceholder);

                //Add all of the new values to the dataset
                for (int j = 0; j < numberOfNewValues; j++) {
                    dataPoint = latestValues[j];
                    set.addEntry(new Entry(dataPoint.getTime(), dataPoint.getDataPoint()));
                }
            }

            //Update axis to move the graphs in realtime even if there are no new data points
            XAxis xAxis = chart.getXAxis();
            xAxis.setAxisMaximum(adjustedTime);
            xAxis.setAxisMinimum(adjustedTime - graphMax);

            YAxis leftYAxis = chart.getAxisLeft();

            float maxY = allGraphDataSingleton.getMaxYValue(dataLabel, currentTime, graphMax);
            //new
            maxY = formulateMaxY(maxY);

            if (!Float.isNaN(maxY) && selectedParameterList.get(i).getMin() != Double.NaN) {
                leftYAxis.setAxisMaximum(maxY);
            }

            //Tell the graph and datasets that they are updated
            lineData.notifyDataChanged();
            chart.notifyDataSetChanged();

            //csv report stuff. Putting the data for each selected parameter into the map
            //latestValues is an array of Data points, for now just grabbing the first item in the array but ultimately unsure of this
            //TODO look into this and see what data it's actually pulling
//            if(headers != null) {
//                if (headers[i] != null && latestValues != null) {
//                    csvMap.put(headers[i], latestValues[0]);
//                }
//            }
            //for each metric grab the arraylist of SensorDataPoints using the allGraphDataSingleton graphData() method
            //go through each arrayList of sensorDataPoints and make each row in the csv a list of data for a specifc metric
            //ie each row = all the data for one metric


            //Refresh graph data
            chart.invalidate();
        }

        //code for writing out the csv map to the csv file,
        //TODO check on performance. Each time updateGraphs() is called a small IO operation will be done here
        ICsvMapWriter mapWriter = null;
        try {
            mapWriter = new CsvMapWriter(new FileWriter (csvFile), CsvPreference.STANDARD_PREFERENCE);
        }
        catch(IOException ioe)
        {
            System.out.println("File writing failed");
        }
    }

    private void createGraphs(long currentTime) {
        //Create a graph for each of the chosen parameters
        referenceTime = allGraphDataSingleton.getReferenceStartTime();
        long adjustedTime = currentTime - referenceTime;
        TimeAxisValueFormatter axisValueFormatter = new TimeAxisValueFormatter(referenceTime);

        for (int i = 0; i < selectedParameterList.size(); i++) {

            LineChart lineChart = new LineChart(this.getContext());

            //Setting view width and height, will be need to be used for dynamic graph size
            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 285, r.getDisplayMetrics());
            lineChart.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) px));
            lineChart.setBackgroundColor((Color.parseColor(getColorI(i))));
            lineChart.getBackground().setAlpha(50);

            //Add graph to parent view (Linear layout)
            lineChartArrayList.add(lineChart);
            graphLinearLayout.addView(lineChart);

            // Adds horizontal line below graphs
            lineView = new View(this.getContext());
            lineView.setVisibility(View.VISIBLE);
            lineView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 80));
            lineView.setBackgroundColor(Color.WHITE);
            view = lineView;

            if (view.getParent() != null)
                ((ViewGroup) view.getParent()).removeView(view);
            graphLinearLayout.addView(view);


            //Get the variables for creating the graph
            DiagnosticParameter parameter = selectedParameterList.get(i);
            String label = parameter.getLabel();
            double min = parameter.getMin();
            double max = parameter.getMax();

            //Create a dataset for the graph
            List<Entry> entries = dataEntries.get(i);
            LineDataSet dataSet = new LineDataSet(entries, label);
            dataSet.setDrawCircles(false);
            dataSet.setLineWidth(3f);//was 1.5
            dataSet.setDrawValues(false);
            //dataSet.setColor(Color.parseColor(getColorI(i)));
            dataSet.setColor(Color.BLACK);
            LineData lineData = new LineData(dataSet);

            //Change graph parameters
            lineChart.setTouchEnabled(false);
            lineChart.setDragEnabled(false);//was false
            lineChart.setPinchZoom(false);
            lineChart.setDrawGridBackground(false);
            lineChart.setAutoScaleMinMaxEnabled(false);
            Description description = lineChart.getDescription();
            description.setEnabled(false);

            //Disabling right Y Axis labels
            YAxis rightYAxis = lineChart.getAxisRight();
            rightYAxis.setEnabled(false);

            YAxis leftYAxis = lineChart.getAxisLeft();


            //new formatter, basically a largeValueFormatter with padding added
            leftYAxis.setValueFormatter(new IAxisValueFormatter() {
                LargeValueFormatter lvf = new LargeValueFormatter();

                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    String lvfLabel = lvf.getFormattedValue(value, axis);
                    int n = lvfLabel.length();
                    if (n > paddingLength) paddingLength = n;//so other graphs aren't thrown off
                    for (int i = n; i < paddingLength; i++) {
                        lvfLabel = " " + lvfLabel;//add spaces
                    }
                    return lvfLabel;
                }

            });
            //end new

            leftYAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
            //Showing only max and min
            leftYAxis.setLabelCount(5, true);
            //If the parameter has a min and max set the Left Y Axis to the min and max
            if (!Double.isNaN(min) && !Double.isNaN(max)) {
                leftYAxis.setAxisMinimum((float) min);

                float maxY = allGraphDataSingleton.getMaxYValue(label, currentTime, graphMax);
                //new
                maxY = formulateMaxY(maxY);

                if (!Float.isNaN(maxY) && selectedParameterList.get(i).getMin() != Double.NaN) {
                    leftYAxis.setAxisMaximum(maxY);
                }
            }
            //Set the xAxis, max is the currentTime and the min is the current minus the total max displayed
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setAxisMaximum(adjustedTime);
            xAxis.setAxisMinimum(adjustedTime - graphMax);
            xAxis.setValueFormatter(axisValueFormatter);

            //After graph setup add the data
            lineChart.setData(lineData);

            //Refresh graph data
            lineChart.invalidate();
        }
    }

    private String getColorI(int i) {
        String[] colors = {"red", "blue", "green", "aqua", "fuchsia", "lime",
                "maroon", "navy", "olive", "silver", "purple", "teal"};

        int n = 12;
        return colors[i % n];
    }

    private float formulateMaxY(float max) {
        float temp = max;
        temp += max / 4;
        if (temp <= 40) return temp;
        //round to nearest multiple of 10
        temp = (float) Math.ceil(temp / 40) * 40;
        return temp;
    }
}

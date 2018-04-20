package com.pedigreetechnologies.diagnosticview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Joe on 4/6/2017.
 */

public class AllGraphDataSingleton {

    private static final AllGraphDataSingleton ourInstance = new AllGraphDataSingleton();
    long referenceStartTime = 0;
    private ArrayList<ArrayList<SensorDataPoints>> dataPoints;
    private HashMap<String, Integer> dataIndexMap;
    private HashMap<String, Integer> lastIndexMap;

    private AllGraphDataSingleton() {
        dataPoints = new ArrayList<>();
        dataIndexMap = new HashMap<>();
        lastIndexMap = new HashMap<>();
    }

    public static AllGraphDataSingleton getInstance() {
        return ourInstance;
    }

    public long getReferenceStartTime() {
        return referenceStartTime;
    }

    public Integer getIndex(String key) {
        return dataIndexMap.get(key);
    }

    public void addEntry(String key, Integer index) {
        dataIndexMap.put(key, index);
    }

    public int getIndexMapSize() {
        return dataIndexMap.size();
    }

    public SensorDataPoints getLastDataPoint(String key) {

        Integer index = dataIndexMap.get(key);

        if (index == null) {
            return null;
        }

        if (index < dataPoints.size() && index >= 0) {
            ArrayList<SensorDataPoints> graph = dataPoints.get(index);
            if (graph.isEmpty()) {
                return null;
            } else {
                return graph.get(graph.size() - 1);
            }
        } else {
            return null;
        }
    }

    public SensorDataPoints getDataPoint(String key, int itemIndex) {

        Integer index = dataIndexMap.get(key);

        if (index == null) {
            return null;
        }

        if (index < dataPoints.size() && index >= 0) {
            ArrayList<SensorDataPoints> graph = dataPoints.get(index);

            if (itemIndex < graph.size() && itemIndex >= 0) {
                return graph.get(itemIndex);
            }
        }
        return null;
    }

    public void addGraphEntry(String key, SensorDataPoints data) {
        Integer index = dataIndexMap.get(key);

        if (index == null) {
            return;
        }

        if (index < dataPoints.size() && index >= 0) {
            ArrayList<SensorDataPoints> graph = dataPoints.get(index);
            graph.add(data);
        }
    }

    public void addGraphEntry(String key, float dataPoint, long time) {
        Integer index = dataIndexMap.get(key);

        if (index == null) {
            return;
        }

        if (index < dataPoints.size() && index >= 0) {
            ArrayList<SensorDataPoints> graph = dataPoints.get(index);
            graph.add(new SensorDataPoints(dataPoint, time - referenceStartTime));
        }
    }

    public int getGraphDataSize(String key) {
        Integer index = dataIndexMap.get(key);

        if (index == null) {
            return -1;
        }

        if (index < dataPoints.size() && index >= 0) {
            return dataPoints.get(index).size();
        } else {
            return -1;
        }
    }

    public SensorDataPoints[] getLatestValues() {
        SensorDataPoints[] dataArray = new SensorDataPoints[dataPoints.size()];

        for (int i = 0; i < dataPoints.size(); i++) {

            ArrayList<SensorDataPoints> graph = dataPoints.get(i);

            if (graph.isEmpty()) {
                dataArray[i] = null;
            } else {
                dataArray[i] = graph.get(graph.size() - 1);
            }
        }

        return dataArray;
    }

    public void buildMapAndDataList(ArrayList<DiagnosticParameter> diagnosticParameters, long startTime) {
        this.referenceStartTime = startTime;
        for (int i = 0; i < diagnosticParameters.size(); i++) {
            dataIndexMap.put(diagnosticParameters.get(i).getLabel(), i);
            lastIndexMap.put(diagnosticParameters.get(i).getLabel(), -1);
            dataPoints.add(new ArrayList<SensorDataPoints>());
        }
    }

    public ArrayList<SensorDataPoints> getGraphData(String key) {
        Integer index = dataIndexMap.get(key);

        if (index == null) {
            return new ArrayList<SensorDataPoints>();
        }

        if (index < dataPoints.size() && index >= 0) {
            return dataPoints.get(index);
        }

        return new ArrayList<SensorDataPoints>();
    }

    public SensorDataPoints[] getDataForUpdatePeriod(String key, long lastTime) {
        Integer index = dataIndexMap.get(key);
        Integer lastUpdateIndex = lastIndexMap.get(key);

        if (index == null || lastUpdateIndex == null) {
            return new SensorDataPoints[0];
        }

        if (index < dataPoints.size() && index >= 0) {
            ArrayList<SensorDataPoints> graphData = dataPoints.get(index);

            //Get the data values starting at the end and working to the last index retrieved
            for (int i = graphData.size() - 1; i > lastUpdateIndex; i--) {
                if (graphData.get(i).getTime() > lastTime) {
                    continue;
                }
                SensorDataPoints[] dataArray = new SensorDataPoints[i - lastUpdateIndex];

                for (int j = 0; (j + lastUpdateIndex + 1) <= i && j < graphData.size(); j++) {
                    dataArray[j] = graphData.get(j + lastUpdateIndex + 1);
                }

                lastIndexMap.put(key, i);

                return dataArray;
            }
        }

        return new SensorDataPoints[0];
    }

    public void resetLastIndexReadMap() {
        Iterator<String> keyValue = lastIndexMap.keySet().iterator();

        while (keyValue.hasNext()) {
            lastIndexMap.put(keyValue.next(), -1);
        }
    }

    //Gets the maximum Y value for the given timeframe, used for graph Y values
    public float getMaxYValue(String key, long currentTime, long graphTimeframe) {
        Integer graphIndex = dataIndexMap.get(key);
        Integer lastUpdateIndex = lastIndexMap.get(key);

        if (graphIndex == null || lastUpdateIndex == null) {
            return Float.NaN;
        }

        if (graphIndex < dataPoints.size() && graphIndex >= 0) {
            ArrayList<SensorDataPoints> graphData = dataPoints.get(graphIndex);
            long adjustedTime = currentTime - referenceStartTime;

            if (graphData.size() <= 0 || graphData.get(graphData.size() - 1).getTime() < adjustedTime - graphTimeframe) {
                return Float.NaN;
            }

            int index = graphData.size() - 1;

            float max = graphData.get(index).getDataPoint();

            while (index >= 0 && graphData.get(index).getTime() >= adjustedTime - graphTimeframe) {
                if (max < graphData.get(index).getDataPoint()) {
                    max = graphData.get(index).getDataPoint();
                }

                index--;
            }

            return max;
        }

        return Float.NaN;
    }
}

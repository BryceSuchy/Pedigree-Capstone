package com.pedigreetechnologies.diagnosticview;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

public class TabGaugeFragment extends Fragment {
    private ArrayList<DiagnosticParameter> selectedParameterList;
    private Handler gaugeHandler;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        final ScrollView scrollView = (ScrollView)inflater.inflate(R.layout.tab_gauge_layout, container, false);

        //Create two layouts both taking up half the screen to have 2 views next to each other
        LinearLayout gaugeLayoutLeft = (LinearLayout)scrollView.findViewById(R.id.gaugeLayout1);
        LinearLayout gaugeLayoutRight = (LinearLayout)scrollView.findViewById(R.id.gaugeLayout2);

        Bundle extras = getArguments();
        if (extras != null) {
            selectedParameterList = extras.getParcelableArrayList("selectedParameterList");
        }

        //Getting the width of the device
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        for(int i = 0; i < selectedParameterList.size(); i ++){
            DiagnosticParameter tempParm = selectedParameterList.get(i);
            View view;

            //If there is not a min and a max create a text view
            if(Double.isNaN(tempParm.getMin())|| Double.isNaN(tempParm.getMax())){
                TextView textView = new TextView(this.getContext());
                textView.setLayoutParams(new ViewGroup.LayoutParams(width / 2, (int)((width / 2) * .8f)));
                textView.setText("0.0 " + tempParm.getUnits() + "\n" + tempParm.getLabel());
                textView.setMinLines(2);
                textView.setGravity(Gravity.CENTER);
                view = textView;
            }
            //If there is a min and a max create a gauge view
            else {
                GaugeView gaugeView = new GaugeView(this.getContext());
                gaugeView.setLayoutParams(new ViewGroup.LayoutParams(width / 2, (int)((width / 2) * .8f)));
                gaugeView.setGaugeMin((float) tempParm.getMin());
                gaugeView.setGaugeMax((float) tempParm.getMax());
                gaugeView.setGaugeValue(0);
                gaugeView.setGaugeUnits(tempParm.getUnits());
                gaugeView.setLabel(tempParm.getLabel());
                gaugeView.invalidate();
                view = gaugeView;
            }

            //Set a tag so you know which view is attached to what label
            view.setTag(tempParm.getLabel());

            //Add the views to the parents left to right
            if(i % 2 == 0){
                gaugeLayoutLeft.addView(view);
            }
            else{
                gaugeLayoutRight.addView(view);
            }
        }

        //Handler to update the gauges with the most recent values
        gaugeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String s = (String) msg.obj;
                String[] strings = s.split(",");

                String label = strings[0];
                String value = strings[1];

                View view = scrollView.findViewWithTag(label);

                if(view instanceof TextView){
                    DiagnosticParameter tempParm;
                    for(int i = 0; i < selectedParameterList.size(); i++){
                        tempParm = selectedParameterList.get(i);
                        if(tempParm.getLabel().equals(label)) {
                            ((TextView)view).setText(value + " " + tempParm.getUnits() + "\n" + label);
                        }
                    }
                }
                else if(view instanceof GaugeView){
                    ((GaugeView)view).updateAndRefreshValue(Float.parseFloat(value));
                }
            }
        };

        MessageProcessorSingleton.getInstance().setMessageHandler(gaugeHandler);

        return scrollView;
    }
}

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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import de.nitri.gauge.Gauge;

public class TabGaugeFragment extends Fragment {
    private ArrayList<DiagnosticParameter> selectedParameterList;
    private Handler gaugeHandler;
    View view = null;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        final ScrollView scrollView = (ScrollView)inflater.inflate(R.layout.tab_gauge_layout, container, false);
        RelativeLayout relativeLayout = scrollView.getRootView().findViewById(R.id.gaugeholder);

        //Create two layouts both taking up half the screen to have 2 views next to each other
        LinearLayout gaugeLayoutLeft = scrollView.findViewById(R.id.gaugeLayout1);
        LinearLayout gaugeLayoutRight = scrollView.findViewById(R.id.gaugeLayout2);

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


            //If there is not a min and a max create a text view
            if(Double.isNaN(tempParm.getMin())|| Double.isNaN(tempParm.getMax())){
                TextView textView = new TextView(this.getContext());
                textView.setLayoutParams(new ViewGroup.LayoutParams((int) (width / 2), (int)((width / 2))));
                textView.setText("0.0 " + tempParm.getUnits() + "\n" + tempParm.getLabel());
                textView.setMinLines(2);
                textView.setGravity(Gravity.CENTER);
                view = textView;
            }

            //If there is a min and a max create a gauge view
            else {

                String gaugeID = tempParm.getLabel().replaceAll("%\\s+","").replaceAll("\\W", "");
                int resID = getResources().getIdentifier(gaugeID, "id", getActivity().getPackageName());
                Gauge gauge = relativeLayout.findViewById(resID);
                gauge.setVisibility(Gauge.VISIBLE);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) (width / 2), (int) (width / 2));
                gauge.setLayoutParams(layoutParams);
                gauge.setValue(0);
                gauge.invalidate();
                view = gauge;
            }

            //Set a tag so you know which view is attached to what label
            view.setTag(tempParm.getLabel());

            //Add the views to the parents left to right
            if(i % 2 == 0){
                if(view.getParent()!=null)
                    ((ViewGroup)view.getParent()).removeView(view); // <- fix
                gaugeLayoutLeft.addView(view);
            }
            else{
                if(view.getParent()!=null)
                    ((ViewGroup)view.getParent()).removeView(view); // <- fix
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
                else if(view instanceof Gauge){
                    //((GaugeView)view).updateAndRefreshValue(Float.parseFloat(value));
                    ((Gauge)view).setLowerText(value);
                    ((Gauge)view).moveToValue(Float.parseFloat(value));
                }
            }
        };

        MessageProcessorSingleton.getInstance().setMessageHandler(gaugeHandler);

        return scrollView;
    }



}

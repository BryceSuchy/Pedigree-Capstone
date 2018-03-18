package com.pedigreetechnologies.diagnosticview;

import android.graphics.Color;
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
    View lineView;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        final ScrollView scrollView = (ScrollView)inflater.inflate(R.layout.tab_gauge_layout, container, false);
        RelativeLayout relativeLayout = scrollView.getRootView().findViewById(R.id.gaugeholder);

        //Create two layouts both taking up half the screen to have 2 views next to each other
        LinearLayout gaugeLayoutLeft = scrollView.findViewById(R.id.gaugeLayout1);
        LinearLayout gaugeLayoutRight = scrollView.findViewById(R.id.gaugeLayout2);

        // Layout to allow a vertical line in between gaugeLayout1 and gaugeLayout2
        LinearLayout verticalLineLayout = scrollView.findViewById(R.id.verticleLineLayout);

        // Layout to add text metrics below all gauges
        LinearLayout textMetricLayout = scrollView.findViewById(R.id.textMetricLayout);

        Bundle extras = getArguments();
        if (extras != null) {
            selectedParameterList = extras.getParcelableArrayList("selectedParameterList");
        }

        //Getting the width of the device
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        // Adds Gauges above all TextViews
        for(int i = 0; i < selectedParameterList.size(); i ++){
            DiagnosticParameter tempParm = selectedParameterList.get(i);


            //If there is not a min and a max create a text view
            if(Double.isNaN(tempParm.getMin())|| Double.isNaN(tempParm.getMax())){

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

                //Set a tag so you know which view is attached to what label
                view.setTag(tempParm.getLabel());

                //Add the views to the parents left to right
                if(i % 2 == 0){

                    // Adds gauge
                    if(view.getParent()!=null)
                        ((ViewGroup)view.getParent()).removeView(view);
                    gaugeLayoutLeft.addView(view);

                    // Adds horizontal line under gauge
                    /*RelativeLayout relLineH = (RelativeLayout)inflater.inflate(R.layout.gauge_draw, container, false);
                    lineView = relLineH.findViewById(R.id.line1);
                    RelativeLayout.LayoutParams layoutParamsLineH = new RelativeLayout.LayoutParams((int) (width / 2), (int) (width / 400));
                    lineView.setLayoutParams(layoutParamsLineH);
                    lineView.invalidate();
                    view = lineView;
                    */

                    // Add gauge name under gauge
                    TextView textView = new TextView(this.getContext());
                    textView.setLayoutParams(new ViewGroup.LayoutParams((int) (width / 2), ViewGroup.LayoutParams.WRAP_CONTENT));
                    textView.setText(tempParm.getLabel());
                    textView.setTextColor(Color.BLACK);
                    textView.setMinLines(2);
                    textView.setGravity(Gravity.CENTER);
                    textView.setPadding(5,0,0,100);
                    view = textView;

                    if(view.getParent()!=null)
                        ((ViewGroup)view.getParent()).removeView(view);
                    gaugeLayoutLeft.addView(view);

                    // Adds Vertical Line to the right of gauge
                    /*RelativeLayout relLineV = (RelativeLayout)inflater.inflate(R.layout.gauge_draw, container, false);
                    lineView = relLineV.findViewById(R.id.line1);
                    RelativeLayout.LayoutParams layoutParamsLineV = new RelativeLayout.LayoutParams((int) (width / 400), (int) (width / 2));
                    lineView.setLayoutParams(layoutParamsLineV);
                    lineView.invalidate();
                    view = lineView;
                    if(view.getParent()!=null)
                        ((ViewGroup)view.getParent()).removeView(view);
                    verticalLineLayout.addView(view);*/

                }
                else{

                    // Adds gauge
                    if(view.getParent()!=null)
                        ((ViewGroup)view.getParent()).removeView(view);
                    gaugeLayoutRight.addView(view);

                    // Adds horizontal line under gauge
                    /*RelativeLayout relLine = (RelativeLayout)inflater.inflate(R.layout.gauge_draw, container, false);
                    lineView = relLine.findViewById(R.id.line1);
                    RelativeLayout.LayoutParams layoutParamsLine = new RelativeLayout.LayoutParams((int) (width / 2), (int) (width / 400));
                    lineView.setLayoutParams(layoutParamsLine);
                    lineView.invalidate();
                    view = lineView;
                    */

                    // Add gauge name under gauge
                    TextView textView = new TextView(this.getContext());
                    textView.setLayoutParams(new ViewGroup.LayoutParams((int) (width / 2), ViewGroup.LayoutParams.WRAP_CONTENT));
                    textView.setText(tempParm.getLabel());
                    textView.setTextColor(Color.BLACK);
                    textView.setMinLines(2);
                    textView.setGravity(Gravity.CENTER);
                    textView.setPadding(5,0,0,100);
                    view = textView;


                    if(view.getParent()!=null)
                        ((ViewGroup)view.getParent()).removeView(view);
                    gaugeLayoutRight.addView(view);


                }
            }

        }



        // Adds TextViews below all gauges
        for(int i = 0; i < selectedParameterList.size(); i ++){
            DiagnosticParameter tempParm = selectedParameterList.get(i);


            //If there is not a min and a max create a text view
            if(Double.isNaN(tempParm.getMin())|| Double.isNaN(tempParm.getMax())){
                TextView textView = new TextView(this.getContext());
                textView.setLayoutParams(new ViewGroup.LayoutParams((int) (width), (int)((width / 9))));
                textView.setText("0.0 " + tempParm.getUnits() + "\n" + tempParm.getLabel());
                textView.setTextColor(Color.BLACK);
                textView.setMinLines(1);
                textView.setGravity(Gravity.LEFT);
                textView.setPadding(50,20,0,0);
                view = textView;

                //Set a tag so you know which view is attached to what label
                view.setTag(tempParm.getLabel());

                //Add the views to the textMetricLayout
                if(view.getParent()!=null)
                    ((ViewGroup)view.getParent()).removeView(view);
                textMetricLayout.addView(view);

                // Adds horizontal line under text metrics
                RelativeLayout relLine = (RelativeLayout)inflater.inflate(R.layout.gauge_draw, container, false);
                lineView = relLine.findViewById(R.id.line1);
                lineView.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams layoutParamsLine = new RelativeLayout.LayoutParams((int) (width), (int) (width / 400));
                lineView.setLayoutParams(layoutParamsLine);
                lineView.invalidate();
                view = lineView;

                if(view.getParent()!=null)
                    ((ViewGroup)view.getParent()).removeView(view);
                textMetricLayout.addView(view);

            }

        }

        // Adds Blue Divider
        RelativeLayout relLine = (RelativeLayout)inflater.inflate(R.layout.gauge_draw, container, false);
        lineView = relLine.findViewById(R.id.divider);
        RelativeLayout.LayoutParams layoutParamsLine = new RelativeLayout.LayoutParams((int) (width), (int) (width / 100));
        lineView.setLayoutParams(layoutParamsLine);
        lineView.invalidate();
        view = lineView;

        if(view.getParent()!=null)
            ((ViewGroup)view.getParent()).removeView(view);
        textMetricLayout.addView(view);


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

                    // Allows blue divider to become visible when text views exist
                    if (lineView.getVisibility() == View.GONE) {
                        lineView.setVisibility(View.VISIBLE);
                    }
                    for(int i = 0; i < selectedParameterList.size(); i++){
                        tempParm = selectedParameterList.get(i);
                        if(tempParm.getLabel().equals(label)) {
                            ((TextView)view).setText(value + " " + tempParm.getUnits() + "\n" + label);
                        }
                    }
                }
                else if(view instanceof Gauge){
                    ((Gauge)view).setLowerText(value);
                    ((Gauge)view).moveToValue(Float.parseFloat(value));
                }
            }
        };

        MessageProcessorSingleton.getInstance().setMessageHandler(gaugeHandler);

        return scrollView;
    }



}

package com.pedigreetechnologies.diagnosticview;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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

    View view = null;
    View lineView;
    int layoutValue = -1;
    private ArrayList<DiagnosticParameter> selectedParameterList;
    private Handler gaugeHandler;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        final ScrollView scrollView;

        int orientation = this.getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {

            scrollView = (ScrollView) inflater.inflate(R.layout.tab_gauge_layout, container, false);
            RelativeLayout relativeLayout = scrollView.getRootView().findViewById(R.id.gaugeholder);

            //Create two layouts both taking up half the screen to have 2 views next to each other
            LinearLayout gaugeLayoutLeft = scrollView.findViewById(R.id.gaugeLayout1);
            LinearLayout gaugeLayoutRight = scrollView.findViewById(R.id.gaugeLayout2);

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
            if (selectedParameterList == null) {

            } else {
                for (int i = 0; i < selectedParameterList.size(); i++) {
                    DiagnosticParameter tempParm = selectedParameterList.get(i);

                    //If there is not a min and a max create a text view
                    if (Double.isNaN(tempParm.getMin()) || Double.isNaN(tempParm.getMax())) {

                        // Adds horizontal line above text metrics
                        RelativeLayout relLine = (RelativeLayout) inflater.inflate(R.layout.gauge_draw, container, false);
                        lineView = relLine.findViewById(R.id.line1);
                        lineView.setVisibility(View.VISIBLE);
                        RelativeLayout.LayoutParams layoutParamsLine = new RelativeLayout.LayoutParams((int) (width), 2);
                        lineView.setLayoutParams(layoutParamsLine);
                        lineView.invalidate();
                        view = lineView;

                        if (view.getParent() != null)
                            ((ViewGroup) view.getParent()).removeView(view);
                        textMetricLayout.addView(view);

                        TextView textView = new TextView(this.getContext());
                        textView.setLayoutParams(new ViewGroup.LayoutParams((int) (width), ViewGroup.LayoutParams.WRAP_CONTENT));
                        textView.setText("0.0 " + tempParm.getUnits() + "\n" + tempParm.getLabel());
                        textView.setTextColor(Color.BLACK);
                        textView.setMinLines(1);
                        textView.setGravity(Gravity.LEFT);
                        textView.setPadding(50, 20, 0, 20);
                        view = textView;

                        //Set a tag so you know which view is attached to what label
                        view.setTag(tempParm.getLabel());

                        //Add the views to the textMetricLayout
                        if (view.getParent() != null)
                            ((ViewGroup) view.getParent()).removeView(view);
                        textMetricLayout.addView(view);
                    }

                    //If there is a min and a max create a gauge view
                    else {

                        // Converting dp to pixels for use with displaying gauges
                        Resources r = getResources();
                        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());

                    // Creating background image
                    GradientDrawable shapeG = new GradientDrawable();
                    shapeG.setShape(GradientDrawable.RECTANGLE);
                    shapeG.setCornerRadii(new float[] {100,100,100,100,0,0,0,0});
                    shapeG.setColor(Color.parseColor("#3A3A3A"));
                    shapeG.setAlpha(50);

                        layoutValue++;
                        String gaugeID = tempParm.getLabel().replaceAll("-%\\s+", "").replaceAll("\\W", "");

                        int resID = getResources().getIdentifier(gaugeID, "id", getActivity().getPackageName());
                        Gauge gauge = relativeLayout.findViewById(resID);
                        gauge.setVisibility(Gauge.VISIBLE);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(((int) ((width / 2) - px)), (int) (width / 2));
                        gauge.setLayoutParams(layoutParams);
                        gauge.setValue(0);
                        gauge.invalidate();
                        gauge.setBackground(shapeG);
                        view = gauge;

                        //Set a tag so you know which view is attached to what label
                        view.setTag(tempParm.getLabel());

                    // Creating background image
                    GradientDrawable shape = new GradientDrawable();
                    shape.setShape(GradientDrawable.RECTANGLE);
                    shape.setCornerRadii(new float[] {0,0,0,0,100,100,100,100});
                    shape.setColor(Color.parseColor("#3A3A3A"));
                    shape.setAlpha(50);

                        // Add gauge name under gauge
                        TextView textView = new TextView(this.getContext());
                        textView.setLayoutParams(new ViewGroup.LayoutParams(((int) ((width / 2) - px)), ViewGroup.LayoutParams.WRAP_CONTENT));
                        textView.setText(tempParm.getLabel());
                        textView.setTextColor(Color.BLACK);
                        textView.setMinLines(2);
                        textView.setGravity(Gravity.CENTER);
                        textView.setPadding(20, 0, 20, 0);
                        textView.setBackground(shape);


                        //Add the views to the parents left to right
                        if (layoutValue % 2 == 0) {

                            // Adds gauge
                            if (view.getParent() != null)
                                ((ViewGroup) view.getParent()).removeView(view);
                            gaugeLayoutLeft.addView(view);


                            view = textView;

                            // Adds Text
                            if (view.getParent() != null)
                                ((ViewGroup) view.getParent()).removeView(view);
                            gaugeLayoutLeft.addView(view);

                            // Adds horizontal line below graphs
                            lineView = new View(this.getContext());
                            lineView.setVisibility(View.VISIBLE);
                            lineView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 30));
                            lineView.setBackgroundColor(Color.parseColor("#fafafa"));
                            view = lineView;

                            if (view.getParent() != null)
                                ((ViewGroup) view.getParent()).removeView(view);
                            gaugeLayoutLeft.addView(view);

                        } else {

                            // Adds gauge
                            if (view.getParent() != null)
                                ((ViewGroup) view.getParent()).removeView(view);
                            gaugeLayoutRight.addView(view);

                            view = textView;

                            // Adds Text
                            if (view.getParent() != null)
                                ((ViewGroup) view.getParent()).removeView(view);
                            gaugeLayoutRight.addView(view);

                            // Adds horizontal line below graphs
                            lineView = new View(this.getContext());
                            lineView.setVisibility(View.VISIBLE);
                            lineView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 30));
                            lineView.setBackgroundColor(Color.parseColor("#fafafa"));
                            view = lineView;

                            if (view.getParent() != null)
                                ((ViewGroup) view.getParent()).removeView(view);
                            gaugeLayoutRight.addView(view);
                        }
                    }

                }
            }
        } else {


            scrollView = (ScrollView) inflater.inflate(R.layout.tab_gauge_layout_landscape, container, false);
            RelativeLayout relativeLayout = scrollView.getRootView().findViewById(R.id.gaugeholder);

            //Create two layouts both taking up half the screen to have 2 views next to each other
            LinearLayout gaugeLayoutLeft = scrollView.findViewById(R.id.gaugeLayout1);
            LinearLayout gaugeLayoutMiddle = scrollView.findViewById(R.id.gaugeLayout2);
            LinearLayout gaugeLayoutRight = scrollView.findViewById(R.id.gaugeLayout3);

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
            for (int i = 0; i < selectedParameterList.size(); i++) {
                DiagnosticParameter tempParm = selectedParameterList.get(i);

                //If there is not a min and a max create a text view
                if (Double.isNaN(tempParm.getMin()) || Double.isNaN(tempParm.getMax())) {

                    // Adds horizontal line above text metrics
                    RelativeLayout relLine = (RelativeLayout) inflater.inflate(R.layout.gauge_draw, container, false);
                    lineView = relLine.findViewById(R.id.line1);
                    lineView.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams layoutParamsLine = new RelativeLayout.LayoutParams((int) (width), 2);
                    lineView.setLayoutParams(layoutParamsLine);
                    lineView.invalidate();
                    view = lineView;

                    if (view.getParent() != null)
                        ((ViewGroup) view.getParent()).removeView(view);
                    textMetricLayout.addView(view);

                    TextView textView = new TextView(this.getContext());
                    textView.setLayoutParams(new ViewGroup.LayoutParams((int) (width), ViewGroup.LayoutParams.WRAP_CONTENT));
                    textView.setText("0.0 " + tempParm.getUnits() + "\n" + tempParm.getLabel());
                    textView.setTextColor(Color.BLACK);
                    textView.setMinLines(1);
                    textView.setGravity(Gravity.LEFT);
                    textView.setPadding(50, 20, 0, 20);
                    view = textView;

                    //Set a tag so you know which view is attached to what label
                    view.setTag(tempParm.getLabel());

                    //Add the views to the textMetricLayout
                    if (view.getParent() != null)
                        ((ViewGroup) view.getParent()).removeView(view);
                    textMetricLayout.addView(view);
                }

                //If there is a min and a max create a gauge view
                else {

                    // Converting dp to pixels for use with displaying gauges
                    Resources r = getResources();
                    float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());

                    // Creating background image
                    GradientDrawable shapeG = new GradientDrawable();
                    shapeG.setShape(GradientDrawable.RECTANGLE);
                    shapeG.setCornerRadii(new float[] {100,100,100,100,0,0,0,0});
                    shapeG.setColor(Color.parseColor("#3A3A3A"));
                    shapeG.setAlpha(50);

                    layoutValue ++;
                    String gaugeID = tempParm.getLabel().replaceAll("-%\\s+","").replaceAll("\\W", "");
                    int resID = getResources().getIdentifier(gaugeID, "id", getActivity().getPackageName());
                    Gauge gauge = relativeLayout.findViewById(resID);
                    gauge.setVisibility(Gauge.VISIBLE);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(((int)((width / 3) - px)), (int) (width / 3));
                    gauge.setLayoutParams(layoutParams);
                    gauge.setValue(0);
                    gauge.invalidate();
                    gauge.setBackground(shapeG);
                    view = gauge;

                    //Set a tag so you know which view is attached to what label
                    view.setTag(tempParm.getLabel());

                    // Creating background image for graphs
                    GradientDrawable shape = new GradientDrawable();
                    shape.setShape(GradientDrawable.RECTANGLE);
                    shape.setCornerRadii(new float[] {0,0,0,0,100,100,100,100});
                    shape.setColor(Color.parseColor("#3A3A3A"));
                    shape.setAlpha(50);

                    // Add gauge name under gauge
                    TextView textView = new TextView(this.getContext());
                    textView.setLayoutParams(new ViewGroup.LayoutParams(((int)((width / 3) - px)), ViewGroup.LayoutParams.WRAP_CONTENT));
                    textView.setText(tempParm.getLabel());
                    textView.setTextColor(Color.BLACK);
                    textView.setMinLines(2);
                    textView.setGravity(Gravity.CENTER);
                    textView.setPadding(5,0,0,100);
                    textView.setBackground(shape);

                    //Add the views to the parents left to right
                    if (layoutValue % 3 == 0) {

                        // Adds gauge
                        if (view.getParent() != null)
                            ((ViewGroup) view.getParent()).removeView(view);
                        gaugeLayoutLeft.addView(view);

                        view = textView;

                        // Adds Text
                        if(view.getParent()!=null)
                            ((ViewGroup)view.getParent()).removeView(view);
                        gaugeLayoutLeft.addView(view);

                        // Adds horizontal line below graphs
                        lineView = new View(this.getContext());
                        lineView.setVisibility(View.VISIBLE);
                        lineView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 30));
                        lineView.setBackgroundColor(Color.parseColor("#fafafa"));
                        view = lineView;

                        if(view.getParent()!=null)
                            ((ViewGroup)view.getParent()).removeView(view);
                        gaugeLayoutLeft.addView(view);

                    } else if (layoutValue % 3 == 1) {

                        // Adds gauge
                        if (view.getParent() != null)
                            ((ViewGroup) view.getParent()).removeView(view);
                        gaugeLayoutMiddle.addView(view);

                        view = textView;

                        // Adds Text
                        if(view.getParent()!=null)
                            ((ViewGroup)view.getParent()).removeView(view);
                        gaugeLayoutMiddle.addView(view);

                        // Adds horizontal line below graphs
                        lineView = new View(this.getContext());
                        lineView.setVisibility(View.VISIBLE);
                        lineView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 30));
                        lineView.setBackgroundColor(Color.parseColor("#fafafa"));
                        view = lineView;

                        if(view.getParent()!=null)
                            ((ViewGroup)view.getParent()).removeView(view);
                        gaugeLayoutMiddle.addView(view);
                      
                    } else {

                        // Adds gauge
                        if (view.getParent() != null)
                            ((ViewGroup) view.getParent()).removeView(view);
                        gaugeLayoutRight.addView(view);

                        view = textView;

                        // Adds Text
                        if(view.getParent()!=null)
                            ((ViewGroup)view.getParent()).removeView(view);
                        gaugeLayoutRight.addView(view);

                        // Adds horizontal line below graphs
                        lineView = new View(this.getContext());
                        lineView.setVisibility(View.VISIBLE);
                        lineView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 30));
                        lineView.setBackgroundColor(Color.parseColor("#fafafa"));
                        view = lineView;

                        if(view.getParent()!=null)
                            ((ViewGroup)view.getParent()).removeView(view);
                        gaugeLayoutRight.addView(view);

                    }
                }
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

                if (view instanceof TextView) {
                    DiagnosticParameter tempParm;
                    for (int i = 0; i < selectedParameterList.size(); i++) {
                        tempParm = selectedParameterList.get(i);
                        if (tempParm.getLabel().equals(label)) {
                            ((TextView) view).setText(value + " " + tempParm.getUnits() + "\n" + label);
                        }
                    }
                } else if (view instanceof Gauge) {
                    ((Gauge) view).setLowerText(value);
                    ((Gauge) view).moveToValue(Float.parseFloat(value));
                }
            }
        };

        MessageProcessorSingleton.getInstance().setMessageHandler(gaugeHandler);

        return scrollView;
    }
}

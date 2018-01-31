package com.pedigreetechnologies.diagnosticview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Joe on 4/24/2017.
 */

public class GaugeView extends View {

    private Paint arcPaint;
    private Paint textPainter;
    private TextPaint textPaint;
    private int totalNoOfPointers = 20;
    private int pointerMaxHeight = 15;
    private int pointerMinHeight = 10;
    private float gaugeMax = 0;
    private float gaugeMin = 0;
    private float gaugeValue = 0;
    private String gaugeUnits = "";
    private int textSize = 40;
    private int padding = 50;
    private String label = "";
    DynamicLayout textLayout;

    public GaugeView(Context context) {
        super(context);
        initialize();
    }

    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public GaugeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize(){
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPainter = new Paint();
        textPaint = new TextPaint();
        textPaint.setTextSize(textSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(20f);

        canvas.drawLine(width * .025f, height, width - width * .025f, height, arcPaint);

        int arcCenterX;
        int arcCenterY;

        float left = pointerMaxHeight + 50;
        float top = pointerMaxHeight + 50;
        float right;
        float bottom;

        //Change the drawable area based on the lowest height or width size
        if(width < height){
            arcCenterX = width / 2;
            arcCenterY = width / 2;
            right = width - pointerMaxHeight - padding;
            bottom = width - pointerMaxHeight - padding;
        }
        //The height can be slightly smaller than the width as the gauge uses more width than height when drawing
        else if(height < width && height * 1.8 >= width){
            arcCenterX = width / 2;
            arcCenterY = width / 2;
            right = width - pointerMaxHeight - padding;
            bottom = width - pointerMaxHeight - padding;
        }
        else {
            arcCenterX = height / 2;
            arcCenterY = height / 2;

            right = height - pointerMaxHeight - padding;
            bottom = height - pointerMaxHeight - padding;
        }

        final RectF arcBounds = new RectF(left, top, right, bottom);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);
        float secondArcDegree = 180f * (gaugeValue / gaugeMax);

        // Draw the arcs
        arcPaint.setColor(Color.parseColor("#ff33b5e5"));
        canvas.drawArc(arcBounds, 180f, 180f, false, arcPaint);
        arcPaint.setColor(Color.BLUE);
        canvas.drawArc(arcBounds, 180f, secondArcDegree, false, arcPaint);

        //Draw end circle
        arcPaint.setColor(Color.BLACK);
        float endX = (float)Math.cos(Math.toRadians(180 + secondArcDegree)) * (right - left) / 2 + arcCenterX;
        float endY = (float)Math.sin(Math.toRadians(180 + secondArcDegree)) * (bottom - top ) / 2 + arcCenterY;
        canvas.drawCircle(endX, endY, 10, arcPaint);

        //Draw Labels
        textPainter.setStyle(Paint.Style.FILL);
        textPainter.setColor(Color.BLACK);
        textPainter.setTextSize(textSize);

        //Draw currentValue and units
        float textOffset = textPainter.measureText(gaugeValue + " " + gaugeUnits);
        canvas.drawText(gaugeValue + " " + gaugeUnits, arcCenterX - textOffset / 2, arcCenterY - (arcCenterY * .2f), textPainter);

        //Draw min
        textOffset = textPainter.measureText(gaugeMin + "");
        canvas.drawText(gaugeMin + "", left - textOffset / 2, top + (bottom - top)/ 2f + textSize, textPainter);

        //Draw max
        textOffset = textPainter.measureText(gaugeMax + "");
        canvas.drawText(gaugeMax + "", right - textOffset / 2 - 10, top + (bottom - top)/ 2f + textSize, textPainter);

        //Draw metric labels
        textLayout = new DynamicLayout(label, textPaint, (int)(getWidth() *.95f), Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        canvas.save();
        canvas.translate(0, height - textLayout.getHeight() - 20);
        textLayout.draw(canvas);
        canvas.restore();

        // Draw the pointers
        int startX = (int)left - pointerMaxHeight - 10;
        int startY = arcCenterY;
        arcPaint.setStrokeWidth(5f);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);
        arcPaint.setColor(Color.BLACK);

        int pointerHeight;
        for (int i = 0; i <= totalNoOfPointers; i++) {
            if(i%5 == 0){
                pointerHeight = pointerMaxHeight;
            }else{
                pointerHeight = pointerMinHeight;
            }
            canvas.drawLine(startX, startY, startX - pointerHeight, startY, arcPaint);
            canvas.rotate(180f/totalNoOfPointers, arcCenterX, arcCenterY);
        }
    }

    public Paint getArcPaint() {
        return arcPaint;
    }

    public void setArcPaint(Paint arcPaint) {
        this.arcPaint = arcPaint;
    }

    public int getTotalNoOfPointers() {
        return totalNoOfPointers;
    }

    public void setTotalNoOfPointers(int totalNoOfPointers) {
        this.totalNoOfPointers = totalNoOfPointers;
    }

    public int getPointerMaxHeight() {
        return pointerMaxHeight;
    }

    public void setPointerMaxHeight(int pointerMaxHeight) {
        this.pointerMaxHeight = pointerMaxHeight;
    }

    public int getPointerMinHeight() {
        return pointerMinHeight;
    }

    public void setPointerMinHeight(int pointerMinHeight) {
        this.pointerMinHeight = pointerMinHeight;
    }

    public float getGaugeMax() {
        return gaugeMax;
    }

    public void setGaugeMax(float gaugeMax) {
        this.gaugeMax = gaugeMax;
    }

    public float getGaugeMin() {
        return gaugeMin;
    }

    public void setGaugeMin(float gaugeMin) {
        this.gaugeMin = gaugeMin;
    }

    public float getGaugeValue() {
        return gaugeValue;
    }

    public void setGaugeValue(float gaugeValue) {
        if(gaugeValue > gaugeMax){
            this.gaugeValue = gaugeMax;
        }
        else if(gaugeValue < gaugeMin){
            this.gaugeValue = gaugeMin;
        }
        else {
            this.gaugeValue = gaugeValue;
        }
    }

    public String getGaugeUnits() {
        return gaugeUnits;
    }

    public void setGaugeUnits(String gaugeUnits) {
        this.gaugeUnits = gaugeUnits;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void updateAndRefreshValue(float gaugeValue){
        setGaugeValue(gaugeValue);
        this.invalidate();
    }
}
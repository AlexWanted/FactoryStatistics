package ru.seveks.factorystatistics;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class GraphView extends View {

    private static final String TAG = GraphView.class.getSimpleName();
    private Paint mPaint;
    private Rect mGraphRect, mBarRect[];
    private ArrayList<Double> mBarValues = new ArrayList<>();
    private Color barColor, barBackgroundColor, graphBackgroundColor;
    private double mMaxBarValue = 140;

    public GraphView(Context context) {
        super(context);
        init();
    }

    public GraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        mPaint = new Paint();
        mGraphRect = new Rect();
        mBarRect = new Rect[getBarCount()];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(Color.parseColor("#ADADAD"));
        int left = getPaddingLeft(),
                right = getWidth()-getPaddingRight(),
                top = getPaddingTop(),
                bottom = getHeight()-getPaddingBottom();

        mGraphRect.left = left;
        mGraphRect.right = right;
        mGraphRect.top = top;
        mGraphRect.bottom = bottom;
        canvas.drawRect(mGraphRect, mPaint);

        if (getBarCount() != 0){
            for (int i=0; i<getBarCount(); i++){
                int barWidth = (right-left)/getBarCount();
                mBarRect[i].left = mGraphRect.left + i*barWidth;
                mBarRect[i].right = mBarRect[i].left+barWidth;
                mBarRect[i].top =   top + (int)((mBarValues.get(i) * (mGraphRect.bottom-mGraphRect.top))/mMaxBarValue) ;
                mBarRect[i].bottom = mGraphRect.bottom;

            }
        }
        Log.d(TAG, "mBarValues.size = "+mBarValues.size());

    }

    public int getBarCount(){
        return mBarValues.size();
    }

    public ArrayList<Double> getBarValues() {
        return mBarValues;
    }

    public void setBarValues(ArrayList<Double> mBarValues) {
        this.mBarValues = mBarValues;
        invalidate();
        requestLayout();
    }
}

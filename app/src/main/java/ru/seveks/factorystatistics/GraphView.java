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
    private Rect mGraphRect, mBarRect;
    private ArrayList<Double> mBarValues = new ArrayList<>();
    private ArrayList<Rect> mBarRects = new ArrayList<>();
    private Color barColor, barBackgroundColor, graphBackgroundColor;
    private double mMaxBarValue = 140;

    private static final int BAR_COLOR = Color.RED;

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
        mBarRect = new Rect();
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
                mBarRect.left = 0;
                mBarRect.right = 0;
                mBarRect.top = 0;
                mBarRect.bottom = 0;
                mBarRect.left = mGraphRect.left + i*barWidth;
                mBarRect.right = mGraphRect.left + i*barWidth+barWidth;
                mBarRect.top = top + (int)((mBarValues.get(i) * (mGraphRect.bottom-mGraphRect.top))/mMaxBarValue) ;
                mBarRect.bottom = mGraphRect.bottom;
                mBarRects.add(mBarRect);
                mPaint.setColor(Color.parseColor("#FF0000"));
                canvas.drawRect(mBarRect, mPaint);


            }
        }
        Log.d(TAG, "mBarValues.size = "+mBarValues.size());

    }


    /**
     * @return возвращает количество пиков
     */
    public int getBarCount(){
        return mBarValues.size();
    }

    /**
     * Ставим максимальное значение графа, относительно которого будет вычисляться размер
     * каждого отдельного пика. Если при инициализации graphView его не установить то
     * автоматически будет выбрано наибольшее значение
     * @param value
     */
    public void setMaxBarValue(double value){
        this.mMaxBarValue = value;
        invalidate();
        requestLayout();
    }

    /**
     * Устанавливает значения пиков. Если не задать во вью будет лишь фон graphView
     * @param mBarValues
     */
    public void setBarValues(ArrayList<Double> mBarValues) {
        this.mBarValues = mBarValues;
        invalidate();
        requestLayout();
    }


}

package ru.seveks.factorystatistics;


import android.content.Context;
import android.content.res.TypedArray;
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
    private ArrayList<Double> barValues = new ArrayList<>();
    private ArrayList<Rect> mBarRects = new ArrayList<>();
    private int barColor = -1;
    private int barBackgroundColor = -1;
    private int graphBackgroundColor = -1;
    private float barPadding = 0;
    private float barPaddingLeft = 0;
    private float barPaddingRight = 0;
    private float barPaddingTop = 0;
    private float barPaddingBottom = 0;
    private double maxBarValue = 0;

    private static final int DEFAULT_BAR_COLOR = Color.RED;
    private static final int DEFAULT_BAR_BACKGROUND_COLOR = Color.YELLOW;
    private static final int DEFAULT_GRAPH_BACKGROUND_COLOR = Color.GRAY;

    public GraphView(Context context) {
        super(context);
        init(context, null);
    }

    public GraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public GraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public GraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        mPaint = new Paint();
        mGraphRect = new Rect();
        mBarRect = new Rect();

        if (context != null){
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GraphView);
            barPadding = a.getDimension(R.styleable.GraphView_barPadding, 0);
            barPaddingLeft = a.getDimension(R.styleable.GraphView_barPaddingLeft, 0);
            barPaddingRight = a.getDimension(R.styleable.GraphView_barPaddingRight, 0);
            barPaddingTop = a.getDimension(R.styleable.GraphView_barPaddingTop, 0);
            barPaddingBottom = a.getDimension(R.styleable.GraphView_barPaddingBottom, 0);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(getGraphBackgroundColor());
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
            if (maxBarValue == 0) for (double value : barValues) if (value>maxBarValue) maxBarValue = value;
            for (int i=0; i<getBarCount(); i++){
                int barWidth = (right-left)/getBarCount();
                mBarRect.left = mBarRect.right = mBarRect.top = mBarRect.bottom  = 0;
                mBarRect.left = mGraphRect.left + i*barWidth;
                mBarRect.right = mGraphRect.left + i*barWidth+barWidth;
                mBarRect.top = mGraphRect.top;
                mBarRect.bottom = mGraphRect.bottom;

                if (barPadding == 0 &&
                        (barPaddingLeft != 0 || barPaddingRight != 0 ||
                                barPaddingTop != 0  || barPaddingBottom != 0)){
                    mBarRect.left += barPaddingLeft;
                    mBarRect.right -= barPaddingRight;
                    mBarRect.top += barPaddingTop;
                    mBarRect.bottom -= barPaddingBottom;
                } else {
                    mBarRect.left += barPadding;
                    mBarRect.right -= barPadding;
                    mBarRect.top += barPadding;
                    mBarRect.bottom -= barPadding;
                }

                int barHeight = mBarRect.bottom-mBarRect.top;
                double barRatio = barValues.get(i) / maxBarValue;
                int barRectBottom = mBarRect.bottom;
                mBarRect.bottom -= barHeight*barRatio;
                mPaint.setColor(getBarBackgroundColor());
                canvas.drawRect(mBarRect, mPaint);

                mBarRect.bottom = barRectBottom;
                mBarRect.top += (1-barRatio)*barHeight;

                mBarRects.add(mBarRect);
                mPaint.setColor(getBarColor());
                canvas.drawRect(mBarRect, mPaint);


            }
        }
        Log.d(TAG, "barValues.size = "+barValues.size());

    }

    private void drawBar(Canvas canvas, Rect barRect){
        if (barPadding == 0 &&
                (barPaddingLeft != 0 || barPaddingRight != 0 ||
                 barPaddingTop != 0  || barPaddingBottom != 0)){
            barRect.left += barPaddingLeft;
            barRect.right -= barPaddingRight;
            barRect.top += barPaddingTop;
            barRect.bottom -= barPaddingBottom;
        } else {
            barRect.left += barPadding;
            barRect.right -= barPadding;
            barRect.top += barPadding;
            barRect.bottom -= barPadding;
        }

    }


    /**
     * @return возвращает количество пиков
     */
    public int getBarCount(){
        return barValues.size();
    }

    public double getMaxBarValue(){
        return maxBarValue;
    }

    public int getGraphBackgroundColor() {
        return graphBackgroundColor != -1 ? graphBackgroundColor : DEFAULT_GRAPH_BACKGROUND_COLOR;
    }

    public int getBarBackgroundColor() {
        return barBackgroundColor != -1 ? barBackgroundColor : DEFAULT_BAR_BACKGROUND_COLOR;
    }

    public int getBarColor() {
        return barColor != -1 ? barColor : DEFAULT_BAR_COLOR;
    }

    /**
     * Ставим максимальное значение графа, относительно которого будет вычисляться размер
     * каждого отдельного пика. Если при инициализации graphView его не установить то
     * автоматически будет выбрано наибольшее значение
     * @param value
     */
    public void setMaxBarValue(double value){
        this.maxBarValue = value;
        invalidate();
        requestLayout();
    }

    /**
     * Устанавливает значения пиков. Если не задать во вью будет лишь фон graphView
     * @param barValues
     */
    public void setBarValues(ArrayList<Double> barValues) {
        this.barValues = barValues;
        invalidate();
        requestLayout();
    }

    public void setGraphBackgroundColor(int graphBackgroundColor) {
        this.graphBackgroundColor = graphBackgroundColor;
    }

    public void setBarBackgroundColor(int barBackgroundColor) {
        this.barBackgroundColor = barBackgroundColor;
    }

    public void setBarColor(int barColor) {
        this.barColor = barColor;
    }
}
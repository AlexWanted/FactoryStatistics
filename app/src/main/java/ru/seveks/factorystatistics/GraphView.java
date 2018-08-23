package ru.seveks.factorystatistics;


import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import java.util.ArrayList;

public class GraphView extends View {

    private static final String TAG = GraphView.class.getSimpleName();
    private Context mContext;
    private Paint mPaint;
    private Rect mGraphRect, mBarRect, mTextRect;
    private ArrayList<Float> barValues = new ArrayList<>();
    private ArrayList<Rect> mBarRects = new ArrayList<>();
    private int barColor = -1;
    private int barBackgroundColor = -1;
    private int graphBackgroundColor = -1;
    private Drawable mBar;
    private Drawable mBarBackground;
    private Drawable mGraphBackground;
    /*private int mBarResource;
    private int mBarBackgroundResource;
    private int mGraphBackgroundResource;*/
    private float barPadding = 0;
    private float barPaddingLeft = 0;
    private float barPaddingRight = 0;
    private float barPaddingTop = 0;
    private float barPaddingBottom = 0;
    private float maxBarValue = 0;

    private float textSize = 0;

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
        mContext = context;
        mPaint = new Paint();
        mGraphRect = new Rect();
        mBarRect = new Rect();
        mTextRect = new Rect();

        ArrayList<Float> values = new ArrayList<>();
        values.add(1f);
        values.add(2f);
        values.add(4f);
        values.add(8f);
        values.add(16f);
        values.add(32f);
        setBarValues(values, false);




        if (context != null && attrs != null){
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GraphView);
            barPadding = a.getDimension(R.styleable.GraphView_barPadding, 0);
            barPaddingLeft = a.getDimension(R.styleable.GraphView_barPaddingLeft, 0);
            barPaddingRight = a.getDimension(R.styleable.GraphView_barPaddingRight, 0);
            barPaddingTop = a.getDimension(R.styleable.GraphView_barPaddingTop, 0);
            barPaddingBottom = a.getDimension(R.styleable.GraphView_barPaddingBottom, 0);
            mBar = a.getDrawable(R.styleable.GraphView_bar);
            mBarBackground = a.getDrawable(R.styleable.GraphView_backgroundBar);
            mGraphBackground = a.getDrawable(R.styleable.GraphView_backgroundGraph);
            if (getBar() == null)
                setBarColor(ContextCompat.getColor(context, R.color.colorAccent));
            if (getBarBackground() == null)
                setBarBackgroundColor(ContextCompat.getColor(context, R.color.colorLightGray));
            if (getGraphBackground() == null)
                setGraphBackgroundColor(ContextCompat.getColor(context, R.color.colorTransparent));
            textSize = a.getDimension(R.styleable.GraphView_textSize, 0);

        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        int left = getPaddingLeft(),
                right = getWidth()-getPaddingRight(),
                top = getPaddingTop(),
                bottom = getHeight()-getPaddingBottom();

        mGraphRect.left = left;
        mGraphRect.right = right;
        mGraphRect.top = top;
        mGraphRect.bottom = bottom;
        getGraphBackground().setBounds(mGraphRect);
        getGraphBackground().draw(canvas);

        //canvas.drawRect(mGraphRect, mPaint);
        if (getBarCount() != 0){
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
                mBarRects.add(mBarRect);

                mTextRect.top = (int) (mBarRect.bottom-textSize);
                mTextRect.bottom = mBarRect.bottom;
                mTextRect.left = mBarRect.left;
                mTextRect.right = mBarRect.right;

                mBarRect.bottom = mTextRect.top;
                int barHeight = mBarRect.bottom-mBarRect.top;
                double barRatio = barValues.get(i) / maxBarValue;
                int barRectBottom = mBarRect.bottom;
                mBarRect.bottom -= barHeight*barRatio;
                getBarBackground().setBounds(mBarRect);
                getBarBackground().draw(canvas);


                mPaint.setColor(Color.parseColor("#101010"));
                mPaint.setTextAlign(Paint.Align.CENTER);
                mPaint.setTextSize(textSize);
                canvas.drawText(String.valueOf(i), mTextRect.centerX(), mTextRect.centerY(), mPaint);
                //canvas.drawRect(mBarRect, mPaint);

                mBarRect.bottom = barRectBottom;
                mBarRect.top += (1-barRatio)*barHeight;

                //mPaint.setColor(getBarColor());
                //canvas.drawRect(mBarRect, mPaint);
                getBar().setBounds(mBarRect);
                getBar().draw(canvas);


            }
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

    /**
     * Ставим максимальное значение пика, относительно которого будет вычисляться размер
     * каждого отдельного пика. Если при инициализации graphView его не установить то
     * автоматически будет выбрано наибольшее значение
     * @param value само значение
     * @param animate анимировать ли переход от предыдущего значения
     */
    public void setMaxBarValue(float value, boolean animate){
        if (animate) {
            float animateFrom = maxBarValue;
            float animateTo = value;
            Log.d(TAG, animateFrom + " " + animateTo);
            PropertyValuesHolder propertyMaxBarValue = PropertyValuesHolder.ofFloat("MaxBarValue", animateFrom, animateTo);
            ValueAnimator animator = new ValueAnimator();
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setValues(propertyMaxBarValue);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    maxBarValue = (float) animation.getAnimatedValue("MaxBarValue");
                    Log.d(TAG, "invalidation " + maxBarValue);
                    invalidate();
                }
            });
            animator.setDuration(4000).start();
        } else {
            this.maxBarValue = value;
            invalidate();
        }
        //
    }

    /**
     * Устанавливает значения пиков. Если не задать во вью будет лишь фон graphView
     * @param localBarValues значения пиков
     * @param animate анимировать возрастания пиков от предыдущего значения
     */
    public void setBarValues(final ArrayList<Float> localBarValues, final boolean animate) {
        float maxValue = 0;
        for (float value : localBarValues) if (value>maxValue) maxValue = value;
        maxBarValue = maxValue;
        if (animate){
            ValueAnimator animator = new ValueAnimator();
            animator.setDuration(1250);
            animator.setInterpolator(new Interpolator() {
                @Override
                public float getInterpolation(float input) {
                    return 1-((1-input)*(1-input)*(1-input)*(1-input));
                }
            });
            animator.setStartDelay(100);
            PropertyValuesHolder[] valuesHolders = new PropertyValuesHolder[localBarValues.size()];
            for (int i=0; i<localBarValues.size(); i++){
                PropertyValuesHolder valuesHolder =
                        PropertyValuesHolder.ofFloat(String.valueOf(i), 0, localBarValues.get(i));
                valuesHolders[i] = valuesHolder;
                Log.d(TAG, "Value "+i+" = "+localBarValues.get(i));
            }
            animator.setValues(valuesHolders);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    barValues.clear();
                    for (int i=0; i<localBarValues.size(); i++){
                        barValues.add((float) animation.getAnimatedValue(String.valueOf(i)));
                    }
                    invalidate();
                }
            });
            animator.start();
        } else {
            this.barValues = localBarValues;
            invalidate();
        }
        //invalidate();
    }

    public Drawable getGraphBackground() {
        return mGraphBackground;
    }

    public Drawable getBarBackground() {
        return mBarBackground;
    }

    public Drawable getBar() {
        return mBar;
    }

    private void setGraphBackground(Drawable resource){
        if (resource == mGraphBackground) return;
        mGraphBackground = resource;
        invalidate();
    }

    public void setGraphBackgroundColor(@ColorInt int graphBackgroundColor) {
        if (mGraphBackground instanceof ColorDrawable){
            ((ColorDrawable) mGraphBackground.mutate()).setColor(graphBackgroundColor);
            invalidate();
        } else {
            setGraphBackground(new ColorDrawable(graphBackgroundColor));
        }
    }

    public void setGraphBackgroundResource(@DrawableRes int id){
//        if (id != 0 && id == mGraphBackgroundResource)
//            return;
        Drawable drawable = null;
        if (id != 0){
            drawable = ContextCompat.getDrawable(mContext, id);
        }
        setGraphBackground(drawable);
    }

    private void setBar(Drawable resource){
        if (resource == mBar) return;
        mBar = resource;
        invalidate();
    }

    public void setBarColor(@ColorInt int barColor) {
        if (mBar instanceof ColorDrawable){
            ((ColorDrawable)mBar.mutate()).setColor(barColor);
            invalidate();
        } else {
            setBar(new ColorDrawable(barColor));
        }
    }

    public void setBarResource(@DrawableRes int id){
        Drawable drawable = null;
        if (id != 0){
            drawable = ContextCompat.getDrawable(mContext, id);
        }
        setBar(drawable);
    }

    private void setBarBackground(Drawable resource){
        if (resource == mBarBackground) return;
        mBarBackground = resource;
        invalidate();
    }

    public void setBarBackgroundColor(@ColorInt int barBackgroundColor) {
        if (mBarBackground instanceof ColorDrawable){
            ((ColorDrawable)mBarBackground).setColor(barBackgroundColor);
            invalidate();
        } else {
            setBarBackground(new ColorDrawable(barBackgroundColor));
        }
    }

    public void setBarBackgroundResource(@DrawableRes int resource){
        Drawable drawable = null;
        if (resource != 0){
            drawable = ContextCompat.getDrawable(mContext, resource);
        }
        setBarBackground(drawable);
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }
}
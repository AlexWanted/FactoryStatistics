package ru.seveks.factorystatistics;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class GraphView extends View {

    private static final String TAG = GraphView.class.getSimpleName();
    private Context mContext;
    private Paint mTextPaint, mShadowPaint;
    private Rect mGraphRect, mBarRect, viewRect, mTextRect;
    private ArrayList<Float> barValues = new ArrayList<>();
    private ArrayList<Rect> mBarRects = new ArrayList<>();
    private Drawable mBar;
    private Drawable mBarBackground;
    private Drawable mGraphBackground;
    private int textColor;
    /*private int mBarResource;
    private int mBarBackgroundResource;
    private int mGraphBackgroundResource;*/
    private int mSelectedBar = -1;
    private float selectedBarPadding = 0;
    private float barPadding = 0;
    private float barPaddingLeft = 0;
    private float barPaddingRight = 0;
    private float barPaddingTop = 0;
    private float barPaddingBottom = 0;
    private float maxBarValue = 0;
    private float textSize = 0;
    private float thumbTextSize = 70;
    private DecimalFormat decimalFormat;
    private Path mThumbPath;

    //Variables for onDraw();
    private float barRatio = 0;
    private String text = "";

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
        mTextPaint = new Paint();
        mShadowPaint = new Paint();
        viewRect = new Rect();
        mGraphRect = new Rect();
        mBarRect = new Rect();
        mTextRect = new Rect();
        mThumbPath = new Path();

        decimalFormat = new DecimalFormat("0.# т");

        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setFlags( Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        setLayerType(LAYER_TYPE_SOFTWARE, mShadowPaint);
        mShadowPaint.setColor(Color.WHITE);
        mShadowPaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        ArrayList<Float> values = new ArrayList<>();
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        values.add(0f);
        setBarValues(values, false, false);

        barPadding = 0;
        barPaddingLeft = 0;
        barPaddingRight = 0;
        barPaddingTop = 0;
        barPaddingBottom = 0;
        setClickable(true);

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
            if (getBar() == null) {
                setBarColor(ContextCompat.getColor(context, R.color.colorAccent));
            }
            if (getBarBackground() == null) {
                setBarBackgroundColor(ContextCompat.getColor(context, R.color.colorLightGray));
            }
            if (getGraphBackground() == null) {
                setGraphBackgroundColor(ContextCompat.getColor(context, R.color.colorTransparent));
            }
            textSize = a.getDimension(R.styleable.GraphView_textSize, 0);
            thumbTextSize = a.getDimension(R.styleable.GraphView_thumbTextSize, 70);
            mTextPaint.setTextSize(textSize);
            textColor = a.getColor(R.styleable.GraphView_textColor, ContextCompat.getColor(context, R.color.colorPrimaryDark));
            mTextPaint.setColor(textColor);
            a.recycle();
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Берём размер канваса и присваиваем его rect'y графа, применяем паддинг и рисуем фон
        canvas.getClipBounds(viewRect);
        mGraphRect = viewRect;
        mGraphRect.left += getPaddingLeft();
        mGraphRect.right -= getPaddingRight();
        mGraphRect.top += getPaddingTop();
        mGraphRect.bottom -= getPaddingBottom();
        getGraphBackground().setBounds(mGraphRect);
        getGraphBackground().draw(canvas);
        if (getBarCount() != 0){
            float barWidth = (float) mGraphRect.width()/getBarCount();
            for (int i=0; i<getBarCount(); i++){
                //Задаём rect столбца
                mBarRect.left = (int) (mGraphRect.left + i * barWidth);
                mBarRect.right = (int) (mGraphRect.left + i * barWidth + barWidth);
                mBarRect.top = mGraphRect.top;
                mBarRect.bottom = mGraphRect.bottom;
                //Сохраняем границы для того чтобы определить на какой прямоугольник навели палец в TouchEvent
                mBarRects.get(i).left = mBarRect.left;
                mBarRects.get(i).right = mBarRect.right;
                mBarRects.get(i).top = mBarRect.top;
                mBarRects.get(i).bottom = mBarRect.bottom;
                //Применяем padding к rect'y столбца
                if (barPadding == 0 && (barPaddingLeft != 0 || barPaddingRight != 0 ||
                        barPaddingTop != 0 || barPaddingBottom != 0)) {
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
                //Если размер текста больше ширины столбца (с учётом padding), размер текста
                //будет равен ширине столбца
                text = String.valueOf(i);

                if (textSize > mBarRect.width()) textSize = mBarRect.width();
                mTextPaint.setTextSize(textSize);

                //Поднимаем низ столбца к топу текста, вычисляем высоту столбца и рисуем лишь
                // ту часть фона столбца, которая видна пользователю
                mBarRect.bottom -= textSize;
                barRatio = barValues.get(i) / maxBarValue;
                int barBottom = mBarRect.bottom;
                mBarRect.bottom = mBarRect.top + (int)(mBarRect.height() * (1-barRatio));
                if (i != mSelectedBar) {
                    getBarBackground().setBounds(mBarRect);
                    getBarBackground().draw(canvas);
                }
                //Задаём размер текста краске, задаём центр отрисовки и рисуем текст
                // с флагами заданными в init
                canvas.drawText(String.valueOf(i), mBarRect.centerX(), barBottom+textSize, mTextPaint);

                //Вычисляем размер столбца и рисуем его
                mBarRect.bottom = barBottom;
                mBarRect.top += (1 - barRatio) * mBarRect.height();
                if (i != mSelectedBar && maxBarValue != 0) {
                    getBar().setBounds(mBarRect);
                    getBar().draw(canvas);
                }
            }

            if (mSelectedBar != -1){
                mBarRect.left = mBarRects.get(mSelectedBar).left;
                mBarRect.right = mBarRects.get(mSelectedBar).right;
                mBarRect.top = mBarRects.get(mSelectedBar).top;
                mBarRect.bottom = mBarRects.get(mSelectedBar).bottom;

                if (barPadding == 0 && (barPaddingLeft != 0 || barPaddingRight != 0 ||
                        barPaddingTop != 0 || barPaddingBottom != 0)) {
                    mBarRect.left += barPaddingLeft;
                    mBarRect.right -= barPaddingRight;
                    mBarRect.top += barPaddingTop;
                    mBarRect.bottom -= barPaddingBottom+textSize;
                } else {
                    mBarRect.left += barPadding;
                    mBarRect.right -= barPadding;
                    mBarRect.top += barPadding;
                    mBarRect.bottom -= barPadding+textSize;
                }

                mBarRect.left -=selectedBarPadding;
                mBarRect.right += selectedBarPadding;
                mBarRect.top -= selectedBarPadding*2;
                mBarRect.bottom += selectedBarPadding*2;

                float radius = (float) Math.pow(selectedBarPadding, 1.5);
                radius *= 0.75;

                mShadowPaint.setShadowLayer(radius,
                        0, selectedBarPadding,
                        Color.GRAY);
                canvas.drawRect(mBarRect, mShadowPaint);

                float barRatio = barValues.get(mSelectedBar) / maxBarValue;
                int barBottom = mBarRect.bottom;
                mBarRect.bottom = mBarRect.top + (int)(mBarRect.height() * (1-barRatio));

                getBarBackground().setBounds(mBarRect);
                getBarBackground().draw(canvas);

                mTextPaint.setTextSize(thumbTextSize);
                text = decimalFormat.format(barValues.get(mSelectedBar)).replace(",",".");
                mTextPaint.getTextBounds(text, 0, text.length(), mTextRect);

                int height = (int) (mTextRect.height()*1.5);
                int width = (int) (mTextRect.width()*1.5);

                int textCenterX = mBarRect.centerX();
                if (mBarRect.centerX() < viewRect.left + width/2) textCenterX = width/2;
                if (mBarRect.centerX() > viewRect.right - width/2) textCenterX = viewRect.right-width/2+getPaddingRight();

                mTextRect.left = textCenterX-width/2;
                mTextRect.right = textCenterX+width/2;
                mTextRect.top = mBarRect.top;
                mTextRect.bottom = mTextRect.top+height;


                int r = mTextRect.height()/2;
                mThumbPath.reset();
                mThumbPath.moveTo(mTextRect.right-r, mTextRect.top);
                mThumbPath.rQuadTo(r, 0, r, r);
                mThumbPath.rQuadTo(0, r, -r, r);
                mThumbPath.lineTo(mTextRect.left+r, mTextRect.bottom);
                mThumbPath.rQuadTo(-r, 0, -r, -r);//bottom-left corner
                mThumbPath.rQuadTo(0, -r, r, -r);
                mThumbPath.lineTo(mTextRect.right-r, mTextRect.top);

                mBarRect.bottom = barBottom;
                mBarRect.top += (1 - barRatio) * mBarRect.height();
                if (maxBarValue != 0) canvas.drawRect(mBarRect, mTextPaint);

                canvas.drawPath(mThumbPath, mShadowPaint);
                canvas.drawText(text, mTextRect.centerX(), (float) (mTextRect.centerY()+height/3), mTextPaint);

            }

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int mX = (int)event.getX();
        int mY = (int)event.getY();
        if (viewRect.contains(viewRect.centerX(),mY+viewRect.top)) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE: {
                    Log.d(TAG,"move");
                    for (int i = 0; i < mBarRects.size(); i++) {
                        if (mX >= mBarRects.get(i).left && mX<= mBarRects.get(i).right){
                            setSelectedBar(i, true);
                        }
                    }
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP: {
                    if (event.getEventTime()-event.getDownTime() < 80){
                        performClick();
                    }
                    setSelectedBar(-1, true);

                    break;
                }
            }
        } else {
            setSelectedBar(-1, true);
            return false;
        }
        return true;
    }

    /**
     * @return возвращает количество столбцов
     */
    public int getBarCount(){
        return barValues.size();
    }

    public double getMaxBarValue(){
        return maxBarValue;
    }

    public void setSelectedBar(final int index, boolean animate){
        long animationDuration = 50;
        if (mSelectedBar != index) {
            if (index > -1 && index < getBarCount()) {
                if (animate) {
                    if (getSelectedBar() != -1) {
                        ValueAnimator scaleAnimator = ValueAnimator.ofFloat(5, 0);
                        scaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                selectedBarPadding = (float) animation.getAnimatedValue();
                                invalidate();
                            }
                        });
                        scaleAnimator.setDuration(animationDuration);
                        scaleAnimator.start();
                        mSelectedBar = -1;
                        setSelectedBar(index, true);
                    } else {

                        mSelectedBar = index;
                        ValueAnimator scaleAnimator = ValueAnimator.ofFloat(0, 5);
                        scaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                selectedBarPadding = (float) animation.getAnimatedValue();
                                invalidate();
                            }
                        });
                        scaleAnimator.setDuration(animationDuration);
                        scaleAnimator.start();
                        Log.d(TAG,"setSelectedBar: selected bar "+index);
                    }
                } else {
                    mSelectedBar = index;
                    invalidate();
                }
            } else {
                mSelectedBar = -1;
                invalidate();
            }
        }
    }

    public int getSelectedBar(){
        return mSelectedBar;
    }

    /**
     * Ставим максимальное значение пика, относительно которого будет вычисляться размер
     * каждого отдельного пика. Если при инициализации graphView его не установить то
     * автоматически будет выбрано наибольшее значение
     * @param value само значение
     * @param animate анимировать ли переход от предыдущего значения
     */
    public void setMaxBarValue(float value, boolean animate){
        if (value == 0) value = 1;
        if (animate) {
            float animateFrom = maxBarValue;
            float animateTo = value;
            PropertyValuesHolder propertyMaxBarValue = PropertyValuesHolder.ofFloat("MaxBarValue", animateFrom, animateTo);
            ValueAnimator animator = new ValueAnimator();
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setValues(propertyMaxBarValue);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    maxBarValue = (float) animation.getAnimatedValue("MaxBarValue");
                    invalidate();
                }
            });
            animator.setDuration(1250).start();
            Log.d(TAG, "setMaxBarValue "+value);
        } else {
            this.maxBarValue = value;
            invalidate();
        }
        //
    }

    /**
     * Устанавливает значения пиков. Если не задать во вью будет лишь фон graphView
     * @param localBarValues значения пиков
     * @param animateBarChange анимировать возрастания столбцов от предыдущего значения к текущему
     * @param animateMaxBar анимировать ли изменение максимального значения
     */
    public void setBarValues(final ArrayList<Float> localBarValues, final boolean animateBarChange, boolean animateMaxBar) {
        float maxValue = 0;
        for (float value : localBarValues) {
            if (value>maxValue) maxValue = value;
            mBarRects.add(new Rect());
        }
        setMaxBarValue(maxValue, animateMaxBar);
        if (animateBarChange){
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
                float currentValue = 0;
                if (barValues.get(i) != null){
                    currentValue = barValues.get(i);
                }
                PropertyValuesHolder valuesHolder =
                        PropertyValuesHolder.ofFloat(String.valueOf(i), currentValue, localBarValues.get(i));
                valuesHolders[i] = valuesHolder;
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

    public float getTextSize() {
        return textSize;
    }

    public int getTextColor(){
        return textColor;
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

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public void setTextColor(@ColorInt int color){
        this.textColor = color;
    }
}
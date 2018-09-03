package ru.seveks.factorystatistics.Views;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
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

import java.text.DecimalFormat;
import java.util.ArrayList;

import ru.seveks.factorystatistics.SuperDecelerateInterpolator;
import ru.seveks.factorystatistics.R;

public class PieChartView extends View {

    public static class Recipe {
        String name;
        float amount;
        public Recipe(String name, float amount){
            this.name = name;
            this.amount = amount;
        }
    }

    private static final String TAG = PieChartView.class.getSimpleName();
    private Context mContext;
    private ArrayList<Recipe> pieValues = new ArrayList<>();
    private float[] mPieceStartAngles;
    private int[] mLegendTextBottoms;
    private Paint mLegendTextPaint, mPiecePaint, mSelectedPiecePaint, mThumbPaint;

    private RectF pieRect;
    private Rect availableRect, mLegendTextRect, mTitleTextRect;
    private Path mThumbPath;

    private Drawable mGraphBackground;
    private DecimalFormat decimalFormat;

    private int mSelectedPiece = -1;
    private float mSelectionValueAnimator = 0;
    private int textColor;
    private int[] pieceColorSet;
    private float mSumValue = 0;
    private float textSize = 0;
    private float thumbTextSize = 0;
    private String text = "";

    //draw time varialbes
    int pieGraphRadius, pieceHeight, thumbHeight, thumbWidth, textRectRealWidth;
    float prevAngle, sweepAngle, customTextSize, averagePadding, animatedValue, thumbCornerRadius;

    public PieChartView(Context context) {
        super(context);
        init(context, null);
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        pieValues = new ArrayList<>();
        mPieceStartAngles = new float[0];
        mLegendTextBottoms= new int[0];
        mContext = context;
        mPiecePaint = new Paint();
        mPiecePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPiecePaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        mThumbPaint = new Paint();
        mThumbPaint.setColor(Color.WHITE);
        mThumbPaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        mLegendTextPaint = new Paint();
        mLegendTextPaint.setColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        mLegendTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mLegendTextPaint.setFlags( Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        mSelectedPiecePaint = new Paint();
        mSelectedPiecePaint.setColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        mSelectedPiecePaint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        setLayerType(LAYER_TYPE_SOFTWARE, mSelectedPiecePaint);



        pieceColorSet = new int[]{
                ContextCompat.getColor(mContext, R.color.materialIndigo400),
                ContextCompat.getColor(mContext, R.color.materialIndigo200),
                ContextCompat.getColor(mContext, R.color.materialCustomA),
                ContextCompat.getColor(mContext, R.color.materialCustomB)};
        pieRect = new RectF();
        availableRect = new Rect();
        mTitleTextRect = new Rect();
        mThumbPath = new Path();
        decimalFormat = new DecimalFormat("0.### т");

        setClickable(true);

        if (context != null && attrs != null){
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomViews);

            mGraphBackground = a.getDrawable(R.styleable.CustomViews_backgroundGraph);
            if (getGraphBackground() == null) {
                setGraphBackgroundColor(ContextCompat.getColor(context, R.color.colorTransparent));
            }
            textSize = a.getDimension(R.styleable.CustomViews_textSize, 70);
            thumbTextSize = a.getDimension(R.styleable.CustomViews_thumbTextSize, 100);
            a.recycle();
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.getClipBounds(availableRect);
        availableRect.left += getPaddingLeft();
        availableRect.right -= getPaddingRight();
        availableRect.top += getPaddingTop();
        availableRect.bottom -= getPaddingBottom();

        if (getPiecesCount() > 0) {
            pieGraphRadius = availableRect.width() / 4;

            pieRect.left = availableRect.left;
            pieRect.right = availableRect.left + 2 * pieGraphRadius;
            pieRect.top = availableRect.centerY() - pieGraphRadius;
            pieRect.bottom = availableRect.centerY() + pieGraphRadius;

            mLegendTextRect = availableRect;
            mLegendTextRect.left = availableRect.centerX()+getPaddingLeft();

            prevAngle = -90;
            while (textSize * getPiecesCount() >= mLegendTextRect.height()) {
                textSize -= 0.5f;
            }
            pieceHeight = mLegendTextRect.height() / getPiecesCount();

            for (int i = 0; i < pieValues.size(); i++) {
                customTextSize = textSize;
                mLegendTextBottoms[i] = pieceHeight *i + pieceHeight;
                mPiecePaint.setColor(pieceColorSet[i % pieceColorSet.length]);

                if (i != mSelectedPiece) {
                    canvas.drawRect(
                            mLegendTextRect.left+textSize/2,
                            mLegendTextBottoms[i]-textSize,
                            mLegendTextRect.left+textSize/2+textSize,
                            mLegendTextBottoms[i], mPiecePaint);
                }
                mLegendTextPaint.setTextAlign(Paint.Align.LEFT);
                mLegendTextPaint.setTextSize(textSize);
                mLegendTextPaint.getTextBounds(pieValues.get(i).name, 0, pieValues.get(i).name.length(), mTitleTextRect);
                textRectRealWidth = (int) (mLegendTextRect.right - (mLegendTextRect.left+textSize/2+textSize));
                /*while (mTitleTextRect.width() > textRectRealWidth){
                    customTextSize -= 2.5f;
                    Log.d(TAG, "scaling down "+pieValues.get(i).name);
                    mLegendTextPaint.setTextSize(customTextSize);
                    mLegendTextPaint.getTextBounds(pieValues.get(i).name, 0, pieValues.get(i).name.length(), mTitleTextRect);
                }*/
                canvas.drawText(pieValues.get(i).name, mLegendTextRect.left+textSize*2, pieceHeight * i + pieceHeight, mLegendTextPaint);

                mPieceStartAngles[i] = prevAngle;
                sweepAngle = 360 * (pieValues.get(i).amount / mSumValue);
                mPiecePaint.setStrokeWidth((float) (pieGraphRadius *0.005));
                if (i != mSelectedPiece) {
                    canvas.drawArc(pieRect, prevAngle, sweepAngle, true, mPiecePaint);
                }
                prevAngle += sweepAngle;
            }
            getGraphBackground().setBounds(
                    (int) pieRect.left,
                    (int)pieRect.right,
                    (int)pieRect.top,
                    (int)pieRect.bottom);
            getGraphBackground().draw(canvas);

            if (mSelectedPiece != -1) {
                sweepAngle = 360 * (pieValues.get(mSelectedPiece).amount / mSumValue);
                pieRect.left -= getPaddingLeft() / 2 * Math.pow(mSelectionValueAnimator,2);
                pieRect.top -= getPaddingTop() / 2 * Math.pow(mSelectionValueAnimator,2);
                pieRect.right += getPaddingRight() / 2 * Math.pow(mSelectionValueAnimator,2);
                pieRect.bottom += getPaddingBottom() / 2 * Math.pow(mSelectionValueAnimator,2);

                averagePadding = (getPaddingLeft() + getPaddingRight() + getPaddingTop() + getPaddingBottom()) / 4;
                animatedValue = (float) (averagePadding * 0.25 * mSelectionValueAnimator * mSelectionValueAnimator);
                //mSelectedPiecePaint.setShadowLayer(animatedValue, 0, animatedValue, Color.parseColor("#50000000"));
                canvas.drawArc(pieRect, mPieceStartAngles[mSelectedPiece], sweepAngle, true, mSelectedPiecePaint);

                canvas.drawRect(
                        mLegendTextRect.left+textSize/2-animatedValue,
                        mLegendTextBottoms[mSelectedPiece]-textSize-animatedValue,
                        mLegendTextRect.left+textSize/2+textSize+animatedValue,
                        mLegendTextBottoms[mSelectedPiece]+animatedValue, mSelectedPiecePaint);

                mLegendTextPaint.setTextSize(thumbTextSize);
                text = decimalFormat.format(pieValues.get(mSelectedPiece).amount).replace(",", ".");
                mLegendTextPaint.getTextBounds(text, 0, text.length(), mTitleTextRect);

                thumbHeight = (int) (mTitleTextRect.height()*1.5);
                thumbWidth = (int) (mTitleTextRect.width()*1.25);

                mTitleTextRect.left = (int) (pieRect.centerX()-thumbWidth/2);
                mTitleTextRect.right = (int) (pieRect.centerX()+thumbWidth/2);;
                mTitleTextRect.top = availableRect.top;
                mTitleTextRect.bottom = mTitleTextRect.top+thumbHeight;

                thumbCornerRadius = mTitleTextRect.height()/2;
                mThumbPath.reset();
                mThumbPath.moveTo(mTitleTextRect.right-thumbCornerRadius, mTitleTextRect.top);
                mThumbPath.rQuadTo(thumbCornerRadius, 0, thumbCornerRadius, thumbCornerRadius);
                mThumbPath.rQuadTo(0, thumbCornerRadius, -thumbCornerRadius, thumbCornerRadius);
                mThumbPath.lineTo(mTitleTextRect.left+thumbCornerRadius, mTitleTextRect.bottom);
                mThumbPath.rQuadTo(-thumbCornerRadius, 0, -thumbCornerRadius, -thumbCornerRadius);//bottom-left corner
                mThumbPath.rQuadTo(0, -thumbCornerRadius, thumbCornerRadius, -thumbCornerRadius);
                mThumbPath.lineTo(mTitleTextRect.right-thumbCornerRadius, mTitleTextRect.top);

                //mThumbPaint.setShadowLayer(animatedValue, 0, (float) (animatedValue-animatedValue*0.15), Color.parseColor("#50000000"));
                canvas.drawPath(mThumbPath, mThumbPaint);
                mLegendTextPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(text, mTitleTextRect.centerX(), (float) (mTitleTextRect.centerY()+thumbHeight/3), mLegendTextPaint);
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int mX = (int)event.getX();
        int mY = (int)event.getY();
        if (availableRect.contains(availableRect.centerX(),mY+availableRect.top)) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE: {
                    Log.d(TAG, mLegendTextRect.left +" "+availableRect.centerX());
                    /*if (mX < mLegendTextRect.left) {
                        int cenX = (int) pieRect.centerX();
                        int cenY = (int) pieRect.centerY();
                        float angle = (float) ((Math.atan2(mY - cenY, mX - cenX) - Math.atan2(100 - cenY, 0)));
                        angle *= 180 / Math.PI;
                        angle = (angle + 360) % 360 - 90;
                        for (int i = 0; i < mPieceStartAngles.length; i++) {
                            float prev = mPieceStartAngles[i];
                            float next = i + 1 != mPieceStartAngles.length ? mPieceStartAngles[i + 1] : 360;
                            if (prev < angle && angle < next) {
                                setSelectedPiece(i, true);
                            }
                        }
                    } else {*/
                        for (int i=0; i<mLegendTextBottoms.length; i++){
                            int prev = i == 0 ? 0 : mLegendTextBottoms[i-1];
                            if (prev< mY && mY < mLegendTextBottoms[i]) setSelectedPiece(i, true);
                        }
                    //}
                    break;
                }
                case MotionEvent.ACTION_CANCEL:{
                    setSelectedPiece(-1, true);
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    if (event.getEventTime()-event.getDownTime() < 100){
                        performClick();
                    }
                    setSelectedPiece(-1, true);
                    break;
                }
            }
        } else {
            setSelectedPiece(-1, true);
            return false;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        setSelectedPiece(-1, true);
        return super.performClick();
    }

    public void setSelectedPiece(final int index, boolean animate){
        if (mSelectedPiece != index) {
            if (animate && index < getPiecesCount()) {
                mSelectedPiece = index;
                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1);
                valueAnimator.setDuration(50);
                valueAnimator.setInterpolator(new SuperDecelerateInterpolator());
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mSelectionValueAnimator = (float) animation.getAnimatedValue();
                        invalidate();
                    }
                });
                if (index != -1) valueAnimator.start();
                else valueAnimator.reverse();
            } else {
                mSelectedPiece = -1;
                invalidate();
            }
        }
    }

    /**
     * @return возвращает количество кусочков
     */
    public int getPiecesCount(){
        return pieValues.size();
    }

    public void setSumValue(float sumValue, boolean animate){
        if (animate){
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(mSumValue, sumValue);
            valueAnimator.setDuration(1250);
            valueAnimator.setInterpolator(new SuperDecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mSumValue = (float)animation.getAnimatedValue();
                    invalidate();
                }
            });
            valueAnimator.start();
        } else {
            mSumValue = sumValue;
            invalidate();
        }
    }

    /**
     * Устанавливает значения пиков. Если не задать во вью будет лишь фон graphView
     * @param recipes значения кусочков
     * @param animateValues анимировать возрастание кусочков от предыдущего значения к текущему
     * @param animateSumValue анимировать смену масштаба кусочков от предыдущего значения к текущему
     */
    public void setValues(final ArrayList<Recipe> recipes, final boolean animateValues, boolean animateSumValue) {
        mPieceStartAngles = new float[recipes.size()];
        mLegendTextBottoms= new int[recipes.size()];
        if (animateValues){
            float sumValue = 0;
            final PropertyValuesHolder[] recipesValues = new PropertyValuesHolder[recipes.size()];
            for (int i=0; i<recipes.size(); i++){
                sumValue += recipes.get(i).amount;
                float value = 0;

                if (pieValues.size() != 0 && pieValues.get(i) != null) value = pieValues.get(i).amount;
                PropertyValuesHolder holder =
                        PropertyValuesHolder.ofFloat(String.valueOf(i), value, recipes.get(i).amount);
                recipesValues[i] = holder;
            }
            setSumValue(sumValue, animateSumValue);
            ValueAnimator animator = new ValueAnimator();
            animator.setDuration(1250);
            animator.setInterpolator(new SuperDecelerateInterpolator());
            animator.setStartDelay(100);
            animator.setValues(recipesValues);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    pieValues.clear();
                    for (int i=0; i<recipes.size(); i++){
                        pieValues.add(new Recipe(recipes.get(i).name,(float)animation.getAnimatedValue(String.valueOf(i))));
                    }
                    invalidate();
                }
            });
            animator.start();
        } else {
            this.pieValues = recipes;
            invalidate();
        }
    }

    public Drawable getGraphBackground() {
        return mGraphBackground;
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


    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public void setTextColor(@ColorInt int color){
        this.textColor = color;
    }
}
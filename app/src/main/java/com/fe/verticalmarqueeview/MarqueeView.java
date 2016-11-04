package com.fe.verticalmarqueeview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

/**
 * Created by chenepengfei on 2016/11/3.
 */
public class MarqueeView extends View {

    /**
     *  当前显示的文本
     */
    private String mCurrentText;

    /**
     *  当前文本的position
     */
    private int mCurrentTextPosition;

    /**
     *  当前文本初始化距离顶部的距离
     */
    private int mCurrentTextInitMarginTop;

    /**
     * 当前文本动态移动的距离
     */
    private int mCurrentTextMoveMarginTop;

    /**
     *  下一个显示的文本
     */
    private String mNextText;

    /**
     *  下一个文本position
     */
    private int mNextTextPosition;

    /**
     *  下一个文本初始化距离顶部的距离
     */
    private int mNextTextInitMarginTop;

    /**
     * 下一个文本动态移动的距离
     */
    private int mNextTextMoveMarginTop;

    /**
     *  文本需要移动的距离
     */
    private int mTextMoveOffset;

    /**
     *  字体大小
     */
    private int mTextSize;

    /**
     *  字体颜色
     */
    private int mTextColor;

    /**
     *  文本距离左，顶，底的间距
     */
    private int mPaddingLeft, mPaddingTop, mPaddingBottom;

    /**
     *  文本集合
     */
    private String[] mTextArray;

    /**
     * 文本最大的宽度
     */
    private int mTextMaxWidth;

    /**
     *  文本高度
     */
    private int mTextHeight;

    /**
     *  画笔
     */
    private Paint mPaint;

    /**
     *  开始延迟的时间
     */
    private int startDelayTime;

    /**
     *  重复延迟的时间
     */
    private int reRepeatDelayTime;

    /**
     *  单个动画执行的时间
     */
    private int itemAnimationTime;

    /**
     *  动画
     */
    private ValueAnimator va;

    private float mProgress;

    private onItemClickListener mOnItemClickListener;

    private Handler handler = new Handler();

    public MarqueeView(Context context) {
        super(context);
    }

    public MarqueeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MarqueeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MarqueeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MarqueeViewStyle);
        mTextColor = typedArray.getColor(R.styleable.MarqueeViewStyle_textColor, Color.BLACK);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.MarqueeViewStyle_textSize, 45);

        mPaddingLeft = typedArray.getDimensionPixelSize(R.styleable.MarqueeViewStyle_paddingLeft, 15);
        mPaddingTop = mPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.MarqueeViewStyle_paddingTopBottom, 25);
        mPaddingTop = typedArray.getDimensionPixelSize(R.styleable.MarqueeViewStyle_paddingTop, mPaddingTop);
        mPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.MarqueeViewStyle_paddingBottom, mPaddingBottom);

        itemAnimationTime = typedArray.getInteger(R.styleable.MarqueeViewStyle_itemAnimationTime, 1000);
        reRepeatDelayTime = typedArray.getInteger(R.styleable.MarqueeViewStyle_reRepeatDelayTime, 1000);
        startDelayTime = typedArray.getInteger(R.styleable.MarqueeViewStyle_startDelayTime, 500);

        typedArray.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mTextSize);
        mPaint.setColor(mTextColor);
        mPaint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mTextArray == null || mTextArray.length == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            ViewGroup.LayoutParams lp = getLayoutParams();
            setMeasuredDimension(getViewWidth(lp, width), getViewHeight(lp, height));
        }
    }

    private int getViewWidth(ViewGroup.LayoutParams lp, int pWidth) {
        int width = 0;
        if(lp.width >= 0) {
            width = lp.width;
        } else if(lp.width == ViewGroup.LayoutParams.WRAP_CONTENT){
            width = mTextMaxWidth + mPaddingLeft;
        } else if(lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            width = pWidth;
        }
        return width;
    }

    private int getViewHeight(ViewGroup.LayoutParams lp, int pHeight) {
        int height = 0;
        if(lp.height >= 0) {
            height = lp.height;
        } else if(lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            height = mTextHeight  + mPaddingTop + mPaddingBottom;
        } else if(lp.height == ViewGroup.LayoutParams.MATCH_PARENT) {
            height = pHeight;
        }
        return height;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mTextArray == null || mTextArray.length == 0) {
            super.onDraw(canvas);
        } else {
            canvas.drawText(mCurrentText, mPaddingLeft, mCurrentTextMoveMarginTop, mPaint);
            canvas.drawText(mNextText, mPaddingLeft, mNextTextMoveMarginTop, mPaint);
        }
    }

    public void setTextArray(String[] textArray) {
        if(textArray == null || textArray.length <= 1) return;
        mTextArray = textArray;
        initTextRect();
        setTextCurrentOrNextStatus(0, 1, true);
        startAnimation();
    }

    private void initTextRect() {
        int size = mTextArray.length;
        for(int i = 0; i < size; i++) {
            String text = mTextArray[i];
            Rect rect = new Rect();
            mPaint.getTextBounds(text, 0, text.length(), rect);
            if(mTextHeight == 0)
                mTextHeight = rect.height();
            if(rect.width() > mTextMaxWidth)
                mTextMaxWidth = rect.width();
        }
    }

    private void setTextCurrentOrNextStatus(int currentPosition, int nextPosition, boolean init) {
        if(nextPosition >= mTextArray.length) {
            nextPosition = 0;
        }
        mCurrentTextPosition = currentPosition;
        mNextTextPosition = nextPosition;
        mCurrentText = mTextArray[mCurrentTextPosition];
        mNextText = mTextArray[mNextTextPosition];

        if(!init) return;
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mCurrentTextInitMarginTop = mCurrentTextMoveMarginTop = getFontBaseLine();
                mNextTextInitMarginTop = mNextTextMoveMarginTop = mTextHeight +  getMeasuredHeight();
                mTextMoveOffset = mNextTextInitMarginTop - mCurrentTextInitMarginTop;
            }
        });

    }

    private int getFontBaseLine() {
        Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
        return getMeasuredHeight() / 2 + (fontMetrics.descent- fontMetrics.ascent) / 2 - fontMetrics.descent;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void startAnimation() {
        va = ValueAnimator.ofFloat(0, 1);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgress = (float) animation.getAnimatedValue();
                int moveOffset = (int) (mTextMoveOffset * mProgress);
                mCurrentTextMoveMarginTop = mCurrentTextInitMarginTop - moveOffset;
                mNextTextMoveMarginTop = mNextTextInitMarginTop - moveOffset;
                postInvalidate();
            }
        });
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                va.pause();
                setTextCurrentOrNextStatus(mNextTextPosition, mNextTextPosition + 1, false);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        va.resume();
                    }
                }, reRepeatDelayTime);
            }
        });
        va.setRepeatCount(-1);
        va.setDuration(itemAnimationTime);
        va.setStartDelay(startDelayTime);
        va.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mCurrentTextPosition);
                    pause();
                }
                break;
        }
        return true;
    }

    public void destory() {
        if(va != null) {
            va.cancel();
            va = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void pause() {
        if(va != null) {
            va.pause();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void resume() {
        if(va != null) {
            va.resume();
        }
    }

    public void setOnItemClickListener(onItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface onItemClickListener {
        void onItemClick(int position);
    }



}

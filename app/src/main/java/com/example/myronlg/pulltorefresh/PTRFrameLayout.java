package com.example.myronlg.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * Created by myron.lg on 2015/7/26.
 */
public class PTRFrameLayout extends FrameLayout {

    private final static float SCROLL_RATIO = 0.35f;
    private static final long ROTATE_ANIM_DURATION = 180;

    enum STATE {GUIDE_USER_PULL, GUIDE_USER_RELEASE}

    private STATE footerViewState = STATE.GUIDE_USER_PULL;

    private View contentView;
    private View footerView;

    private float lastY = -1;

    private int footerViewHeight;
    private TextView footerTextView;
    private Animation rotateUpAnim;
    private Animation rotateDownAnim;
    private ImageView arrowImageView;

    private Scroller scroller;

    public PTRFrameLayout(Context context) {
        super(context);
        init();
    }

    public PTRFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PTRFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        scroller = new Scroller(getContext(), new DecelerateInterpolator(1.6F));

        rotateUpAnim = new RotateAnimation(0.0f, -180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        rotateUpAnim.setFillAfter(true);
        rotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        rotateDownAnim.setFillAfter(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        contentView = getChildAt(0);
        footerView = getChildAt(1);

        arrowImageView = (ImageView) findViewById(R.id.iv_ptr_arrow);
        footerTextView = (TextView) findViewById(R.id.tv_ptr_operation_hint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        footerViewHeight = footerView.getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        contentView.layout(left, top, right, bottom);
        footerView.layout(left, bottom, right, bottom + footerView.getMeasuredHeight());
    }

    /**
     * should get touch event from here, otherwise you can not scroll out the footerView closely after scroll the scrollview
     *
     * @param event
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if (lastY == -1) {
            lastY = event.getY();
        }
        float dy = event.getY() - lastY;
        lastY = event.getY();

        if (getScrollY() != 0 || (dy < 0 && !contentCanScrollUp())) {

            //content scroll end, ptr container scroll begin,
            //send a cancel event to content view, for content view's sake
            if (getScrollY() == 0) {
                MotionEvent cancelEvent = MotionEvent.obtain(event);
                cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                super.dispatchTouchEvent(cancelEvent);
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:

                    //make sure getScrollY() >= 0, aka never let top show blank area
                    int calibratedScrollY = (int) (-dy * SCROLL_RATIO);
                    if ((getScrollY() - dy * SCROLL_RATIO) < 0) {
                        calibratedScrollY = -getScrollY();
                    }

                    scrollBy(0, calibratedScrollY);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (getScrollY() >= footerViewHeight) {
                        if (PTRListener != null) {
                            PTRListener.onTrigger();
                        }
                    }
                    release();
                    break;
            }

            updateFooterViewState();

            //ptr container scroll end, content  scroll begin,
            //send a down event to content view, so it can recognize this second part of the gesture
            if (getScrollY() == 0) {
                MotionEvent downEvent = MotionEvent.obtain(event);
                downEvent.setAction(MotionEvent.ACTION_DOWN | event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
                downEvent.setLocation(event.getX(), event.getY());
                super.dispatchTouchEvent(downEvent);
            }
        }

        return super.dispatchTouchEvent(event);
    }

    private void updateFooterViewState() {
        if (getScrollY() >= footerViewHeight && footerViewState != STATE.GUIDE_USER_RELEASE) {
            footerTextView.setText("let it go ~ baby");
            arrowImageView.startAnimation(rotateDownAnim);
            footerViewState = STATE.GUIDE_USER_RELEASE;
        } else if (getScrollY() < footerViewHeight && footerViewState != STATE.GUIDE_USER_PULL) {
            footerTextView.setText("pull ~ baby");
            arrowImageView.startAnimation(rotateUpAnim);
            footerViewState = STATE.GUIDE_USER_PULL;
        }
    }

    private void release() {
        scroller.startScroll(0, getScrollY(), 0, -getScrollY());
        invalidate();//this will make computeScroll() get called
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()){
            scrollTo(0, scroller.getCurrY());
        }
    }

    private boolean contentCanScrollUp() {
        if (contentView instanceof AdapterView<?>) {
            AdapterView<?> adapterView = (AdapterView<?>) contentView;
            if (adapterView.getLastVisiblePosition() == adapterView.getCount() - 1
                    && adapterView.getChildAt(adapterView.getChildCount() - 1).getBottom() <= adapterView.getHeight()) {
                Log.e("", "adapterView content can not scroll up anymore");
                return false;
            } else {
                return true;
            }
        } else {
            if (contentView.getScrollY() >= contentView.getMeasuredHeight() - contentView.getHeight()) {
                return false;
            } else {
                return true;
            }
        }
    }

    private PTRListener PTRListener;

    public void setPTRListener(PTRListener PTRListener) {
        this.PTRListener = PTRListener;
    }

    public static interface PTRListener {
        public void onTrigger();
    }
}

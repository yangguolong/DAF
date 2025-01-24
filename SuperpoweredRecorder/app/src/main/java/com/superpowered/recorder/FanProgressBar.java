package com.superpowered.recorder;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by Administrator
 * On 2020/5/22
 * Description:
 * @author Administrator
 */
public class FanProgressBar extends ConstraintLayout  {
    private Listener listener;
    private TextView title, show;
    private ImageView circle,circleBg;
    private View progress, progressBack;
    private int width, cWidth;
    private int min, distance, interval, now, marginStart;;
    private float px;
    private float dWidth;
    private String unit ="";
    private Disposable addSubs,minusSubs;

    public FanProgressBar(Context context) {
        this(context, null);
    }

    public FanProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FanProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = View.inflate(context, R.layout.layout_fan_progress_bar, this);
        marginStart = getResources().getDimensionPixelSize(R.dimen.dp_6);
//        title=view.findViewById(R.id.txt_title);
        show=view.findViewById(R.id.txt_show);
        circle=view.findViewById(R.id.img_circle);
        circleBg = view.findViewById(R.id.img_circle_bg);
        progress=view.findViewById(R.id.v_progress);
        progressBack = view.findViewById(R.id.v_progress_background);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isSelected()) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                if (!inRangeOfView(event)) {
                    return false;
                }
                circleBg.setVisibility(View.VISIBLE);
                if(null!=listener){
                    listener.onDown(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                moveView(x-marginStart);
                if(null!=listener){
                    listener.onChange(true,now);
                }
                break;
            case MotionEvent.ACTION_UP:
                circleBg.setVisibility(View.INVISIBLE);
                if (null != listener) {
                    listener.onUp(true, now);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
            default:
                return super.onTouchEvent(event);
        }
        return true;
    }

    public void setStyle(String unit, String minStr, String maxStr, int min, int distance){
       this.unit = unit;
        this.min=min;
        this.distance=distance;
    }

    public void setTitle(String title){
//        this.title.setText(title);
    }

    public void setListener(Listener listener){
        this.listener=listener;
    }

    public void moveView(float x){
        int max = width - cWidth;
        x-=cWidth/2f;
        x = (x < 0 ? 0 : (x > max ? max : x));
        //当前移动的长度换算成显示文字
        now= Math.round(x/dWidth+min);
        show.setText(String.valueOf(now)+"");
        int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        show.measure(spec, spec);
        circle.setTranslationX(getTx());
        circleBg.setTranslationX(circle.getTranslationX());
//        circleBg.setVisibility(now==0?View.INVISIBLE:View.VISIBLE);
        int tvShowTextWidth = show.getMeasuredWidth();
        float mCurrentIvBarX = circle.getX();
//        if(mCurrentIvBarX==0){
//            show.setTranslationX(0);
//        }else if(mCurrentIvBarX==max){
//            show.setTranslationX(width-tvShowTextWidth);
//        }else{
            float tvshowTextX = mCurrentIvBarX + cWidth / 2f - tvShowTextWidth / 2f;
            show.setTranslationX(tvshowTextX < 0 ? 0 : tvshowTextX + tvShowTextWidth > width ? width - tvShowTextWidth : tvshowTextX);
//        }
        moveProgress(circle.getTranslationX());
    }

    private void moveProgress(float x) {
        progress.setTranslationX(marginStart);
        int width = (int)(x - marginStart);
        if (width <= 0) {
            width = 1;
        }
        ViewGroup.LayoutParams params = progress.getLayoutParams();
        params.width = width;
        progress.setLayoutParams(params);
    }

    public float getTx(){
        return (now-min)*dWidth + marginStart;
    }

    public void setProgress(int num){
        moveView((num-min)*dWidth+cWidth/2f);
        now=num;
        if(listener!=null){
            listener.onChange(false,now);
        }
    }

    /**
     * 是否按到了view上
     *
     * @param ev
     * @return
     */
    private boolean inRangeOfView(MotionEvent ev) {
        int[] location = new int[2];
        circle.getLocationOnScreen(location);
        float ivBarX = circle.getX();
        float ivBarY = circle.getY();
        float right = ivBarX + circle.getMeasuredWidth();
        float top = 0;
        float bottom = ivBarY + circle.getMeasuredHeight();
        return ev.getY() >= top && ev.getY() <= bottom && ev.getX() >= ivBarX - getResources().getDimensionPixelSize(R.dimen.dp_30)
                && ev.getX() <= right + getResources().getDimensionPixelSize(R.dimen.dp_30);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 三星手机有可能会返回0
        int bWidth =  progressBack.getMeasuredWidth();
        if (bWidth != 0) {
            width = bWidth;
        }
        int cW =  circle.getMeasuredWidth();
        if (cW != 0) {
            cWidth = cW;
        }
        dWidth=(width-cWidth)/ ((float) distance);
        post(new Runnable() {
            @Override
            public void run() {
                moveView((now-min)*dWidth+cWidth/2f);
            }
        });

    }

    public interface Listener{
        void onDown(boolean isProgress);
        void onUp(boolean isProgress, int value);
        void onChange(boolean isProgress, int value);
    }
}

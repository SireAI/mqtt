package com.jd.jrapp.bm.message.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.jd.jrapp.bm.message.R;


/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/27
 * Author:wangkai
 * Description: 可配置圆角图片
 * =====================================================
 */
public class CornerImageView extends AppCompatImageView {


    private final int DEFAULT_BORDER_WIDTH = 15;
    /*圆角的半径，依次为左上角xy半径，右上角，右下角，左下角*/
    private float[] rids = new float[8];


    public CornerImageView(Context context) {
        super(context);

    }


    public CornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CornerImageView);
        int cornerLeftTop = a.getDimensionPixelSize(R.styleable.CornerImageView_corner_letf_top, DEFAULT_BORDER_WIDTH);
        int cornerRightTop = a.getDimensionPixelSize(R.styleable.CornerImageView_corner_right_top, DEFAULT_BORDER_WIDTH);
        int cornerRightBottom = a.getDimensionPixelSize(R.styleable.CornerImageView_corner_right_bottom, DEFAULT_BORDER_WIDTH);
        int cornerLeftBottom = a.getDimensionPixelSize(R.styleable.CornerImageView_corner_left_bottom, DEFAULT_BORDER_WIDTH);
        a.recycle();
        rids = new float[]{cornerLeftTop, cornerLeftTop, cornerRightTop, cornerRightTop, cornerRightBottom, cornerRightBottom, cornerLeftBottom, cornerLeftBottom};    }


    public CornerImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public void setCorner_letf_top(float radius){
        rids[0]=radius;
        rids[1]=radius;
    }
    public void setCorner_right_bottom(float radius){
        rids[4]=radius;
        rids[5]=radius;
    }

    /**
     * unit dip to px
     */
    public int dip2px(Context context, float dip) {
        float v = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
        return (int) (v + 0.5f);
    }


    /**
     * 画图
     * by Hankkin at:2015-08-30 21:15:53
     *
     * @param canvas
     */
    protected void onDraw(Canvas canvas) {
        Path path = new Path();
        int w = this.getWidth();
        int h = this.getHeight();
        /*向路径中添加圆角矩形。radii数组定义圆角矩形的四个圆角的x,y半径。radii长度必须为8*/
        path.addRoundRect(new RectF(0, 0, w, h), rids, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
} 
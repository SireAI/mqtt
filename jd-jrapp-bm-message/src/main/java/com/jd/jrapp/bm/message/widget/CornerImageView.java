package com.jd.jrapp.bm.message.widget;

import android.content.Context;
import android.graphics.Canvas;  
import android.graphics.Path;  
import android.graphics.RectF;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.TypedValue;


/* 
*用来显示不规则图片， 
* 上面两个是圆角，下面两个是直角 
* */  
public class OvalImageView extends AppCompatImageView {
  
  
    /*圆角的半径，依次为左上角xy半径，右上角，右下角，左下角*/  
    private float[] rids ;
  
  
    public OvalImageView(Context context) {  
        super(context);
        init(context);

    }  
  
  
    public OvalImageView(Context context, AttributeSet attrs) {  
        super(context, attrs);
        init(context);

    }  
  
  
    public OvalImageView(Context context, AttributeSet attrs, int defStyleAttr) {  
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        rids = new float[]{dip2px(context,15), dip2px(context,15), dip2px(context,15), dip2px(context,15), dip2px(context,2), dip2px(context,2), dip2px(context,15), dip2px(context,15)};
    }


    /**
     * unit dip to px
     */
    public  int dip2px(Context context, float dip) {
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
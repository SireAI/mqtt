package com.jd.jrapp.bm.message.utils;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.jd.jrapp.bm.message.constant.Constant;

public class StringUtils {
    public static boolean isEmpty(String text){
       return TextUtils.isEmpty(text);
    }

    public static SpannableString text(String text){
        SpannableString spannableString = new SpannableString("");
       if(isEmpty(text))return spannableString;
       if(text.equals(Constant.NET_ERROR)){
            spannableString = new SpannableString("网络连接问题，请 重新发送");
           spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#4D7BFE")), 8,spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
       }else {
            spannableString = new SpannableString(text);
       }
       return spannableString;
    }
}

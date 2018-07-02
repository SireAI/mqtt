package com.jd.jrapp.bm.message.adapter.delegate;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.jd.jrapp.bm.message.constant.Constant;
import com.jd.jrapp.bm.message.db.IMMessage;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/25
 * Author:wangkai
 * Description:消息不通过信息处理
 * =====================================================
 */
public class MessageFailedExtroInfor {
    public static boolean isEmpty(String text) {
        return TextUtils.isEmpty(text);
    }
    public static boolean test(IMMessage imMessage){
       return imMessage.getMessageCreateTime().getTime()%2 == 0;
    }
    public static SpannableString text(String text) {
        SpannableString spannableString = new SpannableString("");
        if (isEmpty(text)) return spannableString;
        if (text.equals(Constant.NET_ERROR)) {
            spannableString = new SpannableString("网络连接问题，请 重新发送");
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#4D7BFE")), 8, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if(text.equals(Constant.FILE_SEND_FAILED)){
            spannableString = new SpannableString("发送失败，请 重新发送");
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#4D7BFE")), 8, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }else {
            spannableString = new SpannableString(text);
        }
        return spannableString;
    }
    public static boolean enableClick(String text){
       return !isEmpty(text)&&(text.equals(Constant.NET_ERROR)||text.equals(Constant.FILE_SEND_FAILED));
    }
}

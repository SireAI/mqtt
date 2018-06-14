package com.sire.corelibrary.Utils;

import android.text.TextUtils;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * ==================================================
 * All Right Reserved
 * Date:2017/11/16
 * Author:Sire
 * Description:
 * ==================================================
 */

public class TimeConverterUtils {
    public static String date2SimpleDescription(Date date) {
        Date currentDate = new Date();
        long deltaTime = currentDate.getTime() - date.getTime();
        //秒
        deltaTime = deltaTime / 1000;
        if (deltaTime <= 30) {
            return "刚刚";
        }
        if (deltaTime <= 60) {
            return deltaTime + "秒前";
        }
        //分钟
        deltaTime = deltaTime / 60;
        if (deltaTime >= 1 && deltaTime <= 60) {
            return deltaTime + "分钟前";
        }
        //小时
        deltaTime = deltaTime / 60;
        if (deltaTime >= 1 && deltaTime <= 24) {
            if (isNow(date) && deltaTime >= 6) {
                String dateDescription = getOneDayDescription(date);
                if (dateDescription != null) {
                    return dateDescription;
                }
            }
            return deltaTime + "小时前";
        }
        //天
        deltaTime = deltaTime / 24;
        if (deltaTime >= 1 && deltaTime <= 3) {
            if (deltaTime == 1) {
                return "昨天";
            }
            return deltaTime + "天前";
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        return simpleDateFormat.format(date);
    }

    /**
     * 获取某一天
     * @param date
     * @return
     */
    public static String getManKindDate(Date date){
        Date currentDate = new Date();
        long deltaTime = currentDate.getTime() - date.getTime();
        long oneDay = 24*60*60*1000;
        long countOfDays = deltaTime / oneDay;
        String datePart = "";
        if(countOfDays==0){
            datePart = "";
        }else if(countOfDays == 1){
            datePart = "昨天";
        }else if(countOfDays == 2 || countOfDays == 3){
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if(dayOfWeek == 1){
                datePart = "周末";
            }else if(dayOfWeek ==2){
                datePart = "周一";
            }else if(dayOfWeek==3){
                datePart = "周二";
            }else if(dayOfWeek == 4){
                datePart = "周三";
            }else if(dayOfWeek == 5){
                datePart = "周四";
            }else if(dayOfWeek == 6){
                datePart = "周五";
            }else if(dayOfWeek == 7){
                datePart = "周六";
            }
        }else if(countOfDays <= 365){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M月d日", Locale.CHINA);
            datePart = simpleDateFormat.format(date);
        }else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
            datePart = simpleDateFormat.format(date);
        }
        if(!TextUtils.isEmpty(datePart)){
            datePart = datePart+" ";
        }
        String manDate = datePart+getOneDayDescription(date);
        return manDate;
    }

    /**
     * 获取一天中的时间段
     * @param date
     * @return
     */
    public static String getOneDayDescription(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        String time = "";
        if (hour > 0 && hour < 6) {
            time = "凌晨" + hour + ":" + minute;
        } else if (hour >= 6 && hour < 8) {
            time = "早上" + hour + ":" + minute;
        } else if (hour >= 8 && hour < 11) {
            time = "上午" + hour + ":" + minute;
        } else if (hour >= 11 && hour < 13) {
            time = "中午" + hour + ":" + minute;
        } else if (hour >= 13 && hour < 18) {
            time = "下午" + (hour - 12) + ":" + minute;
        } else if (hour >= 18 && hour < 24) {
            time = "晚上" + (hour - 12) + ":" + minute;
        }
        return time;
    }

    /**
     * 判断时间是不是今天
     *
     * @param date
     * @return 是返回true，不是返回false
     */
    private static boolean isNow(Date date) {
        //当前时间
        Date now = new Date();
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        //获取今天的日期
        String nowDay = sf.format(now);


        //对比的时间
        String day = sf.format(date);

        return day.equals(nowDay);


    }
}

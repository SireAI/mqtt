package com.jd.jrapp.bm.message.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * ==================================================
 * All Right Reserved
 * Date:2017/11/3
 * Author:Sire
 * Description:
 * ==================================================
 */
public class TimeOffSetUtils {
    private static String datePatten = "yyyy-MM-dd HH:mm:ss SSS";
    public static Date tommorrow(){
        return getDate(1);
    }

    private static Date getDate(int offset) {
        Date date=new Date();//取时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE,offset);//把日期往加一天，若想把日期向后推一天则将负数改为正数
        return calendar.getTime();
    }

    public static Date yesterady(){

        return getDate(-1);
    }
    public static Date parse(String dateStr) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePatten, Locale.CHINA);
       return simpleDateFormat.parse(dateStr);
    }

    public static String format(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePatten);
    return simpleDateFormat.format(date);
    }
}
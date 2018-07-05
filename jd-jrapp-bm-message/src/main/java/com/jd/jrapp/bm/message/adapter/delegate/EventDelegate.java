package com.jd.jrapp.bm.message.adapter.delegate;

import android.view.View;

import com.jd.jrapp.bm.message.db.IMMessage;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/26
 * Author:wangkai
 * Description:事件响应代理
 * =====================================================
 */
public interface EventDelegate {
    void onClick(View view, IMMessage imMessage);
}

package com.jd.jrapp.bm.message.adapter.delegate;

import android.view.View;

import com.jd.jrapp.bm.message.adapter.Element;
import com.jd.jrapp.bm.message.db.IMMessage;
import com.jd.jrapp.bm.message.model.IMMessageModel;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/29
 * Author:wangkai
 * Description:左侧对话项
 * =====================================================
 */
public class LeftTalkerDelegate extends IMDelegate implements EventDelegate {

    public LeftTalkerDelegate(IMMessageModel imMessageModel, int layoutId) {
        super(imMessageModel, layoutId);
    }


    @Override
    public boolean isForViewType(Element item, int position) {
        if (item != null && item instanceof IMMessage) {
            IMMessage message = (IMMessage) item;
            if (imMessageModel.getUserId().equals(message.getToAuthorId())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void convert(ViewHolder holder, Element item, int position) {
        if (item instanceof IMMessage && imMessageModel != null && imMessageModel.getTalker() != null && imMessageModel.getTalker().getPeerTalker() != null) {
            ((IMMessage) item).setFromAuthorImg(imMessageModel.getTalker().getPeerTalker().getUserImg());
        }
        super.convert(holder, item, position);
    }

    @Override
    public void onClick(View view) {
        System.out.println("=======left");
    }
}
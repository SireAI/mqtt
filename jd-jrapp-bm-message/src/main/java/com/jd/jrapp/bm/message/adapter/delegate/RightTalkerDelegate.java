package com.jd.jrapp.bm.message.adapter.delegate;

import android.view.View;

import com.jd.im.converter.MsgContentResolve;
import com.jd.jrapp.bm.message.R;
import com.jd.jrapp.bm.message.adapter.Element;
import com.jd.jrapp.bm.message.db.IMMessage;
import com.jd.jrapp.bm.message.model.IMMessageModel;
import com.jd.jrapp.bm.message.utils.CommonUtils;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/29
 * Author:wangkai
 * Description: 右侧对话项
 * =====================================================
 */
public class RightTalkerDelegate extends IMDelegate implements EventDelegate{

    public RightTalkerDelegate(IMMessageModel imMessageModel, int layoutId) {
        super(imMessageModel,layoutId);
    }

    @Override
    public void convert(ViewHolder holder, Element item, int position) {
        if (imMessageModel.getTalker() != null) {
            ((IMMessage) item).setFromAuthorImg(imMessageModel.getTalker().getUserImg());
        }
        super.convert(holder, item, position);
    }

    /**
     * 点击事件
     * @param view 被点击的view
     */
    @Override
    public void onClick(View view,IMMessage imMessage) {
        if(CommonUtils.isFastClick(500)){
            return;
        }
        int id = view.getId();
        if(id == R.id.tv_failed_infor){
            if(MsgContentResolve.TEXT_PLAIN.equals(imMessage.getContentType())){
                imMessageModel.reSendOffLineMessage(imMessage);
            }else if(MsgContentResolve.isFileType(imMessage.getContentType())){
                imMessageModel.reSendOffLineFileMessage(imMessage);
            }
        }
    }


    @Override
    public boolean isForViewType(Element item, int position) {
        if (item != null && item instanceof IMMessage) {
            IMMessage message = (IMMessage) item;
            if (imMessageModel.getUserId().equals(message.getFromAuthorId())) {
                return true;
            }
        }
        return false;
    }
}
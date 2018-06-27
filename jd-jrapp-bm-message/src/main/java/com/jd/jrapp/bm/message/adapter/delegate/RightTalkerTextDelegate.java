package com.jd.jrapp.bm.message.adapter.delegate;

import android.view.View;

import com.jd.jrapp.bm.message.BR;
import com.jd.jrapp.bm.message.R;
import com.jd.jrapp.bm.message.adapter.Element;
import com.jd.jrapp.bm.message.constant.Constant;
import com.jd.jrapp.bm.message.db.IMMessage;
import com.jd.jrapp.bm.message.model.IMMessageModel;

public class RightTalkerDelegate extends ItemViewDataBindingDelegate<Element> {
    private final int layoutId;
    private final IMMessageModel imMessageModel;

    public RightTalkerDelegate(IMMessageModel imMessageModel, int layoutId) {
        this.imMessageModel = imMessageModel;
        this.layoutId = layoutId;
    }


    @Override
    public int getType() {
        return layoutId;
    }

    @Override
    public void convert(ViewHolder holder, Element item, int position) {
        if (item instanceof IMMessage && imMessageModel != null && imMessageModel.getTalker() != null) {
            ((IMMessage) item).setFromAuthorImg(imMessageModel.getTalker().getUserImg());
        }
        holder.getDataBinding().setVariable(BR.delegate,this);
        super.convert(holder, item, position);
    }

    /**
     * 点击事件
     * @param view 被点击的view
     */
    public void onClick(View view,IMMessage imMessage) {
        int id = view.getId();
        if(id == R.id.tv_failed_infor){
            imMessageModel.reSendOffLineMessage(view.getContext(),imMessage);
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

    @Override
    protected int getItemBRName() {
        return BR.imMessage;
    }
}
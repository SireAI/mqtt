package com.jd.jrapp.bm.message.adapter;

import com.jd.jrapp.bm.message.BR;
import com.jd.jrapp.bm.message.db.IMMessage;
import com.jd.jrapp.bm.message.model.IMMessageModel;

public class RightTalkerDelegate extends ItemViewDataBindingDelegate<IMMessage> {
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
    public boolean isForViewType(IMMessage item, int position) {
        if (item != null) {
            if (item.getFromAuthorId().equals(imMessageModel.getUserId())) {
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
package com.jd.jrapp.bm.message.adapter;

import com.jd.jrapp.bm.message.BR;
import com.jd.jrapp.bm.message.db.IMMessage;
import com.jd.jrapp.bm.message.model.IMMessageModel;

public class LeftTalkerDelegate extends ItemViewDataBindingDelegate<IMMessage> {
    private final int layoutId;
    private final IMMessageModel imMessageModel;

    public LeftTalkerDelegate(IMMessageModel imMessageModel, int layoutId) {
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
             if (item.getToAuthorId().equals(imMessageModel.getUserId())) {
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
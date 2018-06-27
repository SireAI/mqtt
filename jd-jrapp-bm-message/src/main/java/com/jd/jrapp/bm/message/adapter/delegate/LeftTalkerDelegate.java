package com.jd.jrapp.bm.message.adapter.delegate;

import com.jd.im.converter.MsgContentResolve;
import com.jd.jrapp.bm.message.BR;
import com.jd.jrapp.bm.message.adapter.Element;
import com.jd.jrapp.bm.message.db.IMMessage;
import com.jd.jrapp.bm.message.model.IMMessageModel;

public class LeftTalkerTextDelegate extends IMDelegate {

    public LeftTalkerTextDelegate(IMMessageModel imMessageModel,int layoutId) {
        super(imMessageModel,layoutId);
    }


    @Override
    public boolean isForViewType(Element item, int position) {
        if (item != null && item instanceof IMMessage) {
            IMMessage message = (IMMessage) item;
            if (MsgContentResolve.TEXT_PLAIN.equals(message.getContentType()) && imMessageModel.getUserId().equals(message.getToAuthorId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void convert(ViewHolder holder, Element item, int position) {
        if(item instanceof IMMessage && imMessageModel!=null && imMessageModel.getTalker()!=null && imMessageModel.getTalker().getPeerTalker()!=null){
            ((IMMessage) item).setFromAuthorImg(imMessageModel.getTalker().getPeerTalker().getUserImg());
        }
        super.convert(holder, item, position);
    }

}
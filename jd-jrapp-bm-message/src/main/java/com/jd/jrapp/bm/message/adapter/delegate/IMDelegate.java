package com.jd.jrapp.bm.message.adapter.delegate;

import com.jd.im.converter.MsgContentResolve;
import com.jd.jrapp.bm.message.BR;
import com.jd.jrapp.bm.message.manager.UploadManager;
import com.jd.jrapp.bm.message.manager.UploadState;
import com.jd.jrapp.bm.message.adapter.Element;
import com.jd.jrapp.bm.message.db.IMMessage;
import com.jd.jrapp.bm.message.model.IMMessageModel;

public abstract class IMDelegate extends ItemViewDataBindingDelegate<Element>{
    private final int layoutId;
    protected final IMMessageModel imMessageModel;

    public IMDelegate(IMMessageModel imMessageModel,int layoutId) {
        this.layoutId = layoutId;
        this.imMessageModel = imMessageModel;
    }

    @Override
    public void convert(ViewHolder holder, Element item, int position) {
        holder.getDataBinding().setVariable(BR.delegate,this);
        checkIfUploading(holder, item);
        super.convert(holder, item, position);
    }

    /**
     * 检测该消息是否正在执行上传
     * @param holder
     * @param item
     */
    private void checkIfUploading(ViewHolder holder, Element item) {
        if(item instanceof IMMessage && MsgContentResolve.isFileType(((IMMessage) item).getContentType())){
            UploadState uploadState = UploadManager.getInstance().getUploadState(((IMMessage) item).getMessageId());
            if(uploadState!=null){
                holder.getDataBinding().setVariable(BR.uploadState,uploadState);
            }
        }
    }

    @Override
    protected int getItemBRName() {
        return BR.imMessage;
    }

    @Override
    public int getType() {
        return layoutId;
    }
}

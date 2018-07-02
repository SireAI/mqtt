package com.jd.jrapp.bm.message.adapter.delegate;


import com.jd.jrapp.bm.message.BR;
import com.jd.jrapp.bm.message.adapter.Element;
import com.jd.jrapp.bm.message.bean.LoadingInfor;

public class LoadingDelegate extends ItemViewDataBindingDelegate<Element> {
    private final int layoutId;
    private ICallBack callBack;

    public LoadingDelegate(int layoutId, ICallBack callBack) {
        this.layoutId = layoutId;
        this.callBack = callBack;
    }

    public static LoadingInfor createLoadingMessage(boolean show) {
        return new LoadingInfor(show);
    }

    @Override
    public void convert(ViewHolder holder, Element item, int position) {
        super.convert(holder, item, position);
        synchronized (LoadingDelegate.class) {
            if (callBack != null) {
                callBack.onLoadMore();
            }
        }
    }

    @Override
    protected int getItemBRName() {
        return BR.loadingItem;
    }

    @Override
    public int getType() {
        return layoutId;
    }

    @Override
    public boolean isForViewType(Element item, int position) {
        if (position == 0 && item instanceof LoadingInfor) {

            return true;
        }
        return false;

    }

    public interface ICallBack {
        void onLoadMore();
    }
}

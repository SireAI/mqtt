package com.jd.im.socket;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/29
 * Author:wangkai
 * Description:事件封装
 * =====================================================
 */
public class Event<T> implements SlotElement{
    private int repeatCount;
    private T action;
    private int actionId;

    public Event(int repeatCount, T action,int actionId) {
        this.repeatCount = repeatCount;
        this.action = action;
        this.actionId = actionId;
    }

    public Event() {
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public T getAction() {
        return action;
    }

    public void setAction(T action) {
        this.action = action;
    }



    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    @Override
    public int getId() {
        return actionId;
    }
}

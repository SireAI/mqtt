package com.jd.jrapp.bm.message.bean;

import com.jd.jrapp.bm.message.adapter.Element;

import java.util.Date;
import java.util.UUID;

public class LoadingInfor implements Element{
    private final boolean loading;

    public LoadingInfor(boolean loading) {
        this.loading = loading;
    }

    public boolean isLoading() {
        return loading;
    }

    @Override
    public String diffId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String diffContent() {
        return "loading";
    }
}

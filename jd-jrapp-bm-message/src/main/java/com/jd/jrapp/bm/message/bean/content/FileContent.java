package com.jd.jrapp.bm.message.bean.content;

public class FileContent implements IMultiContent{
    private  String imageUrl;

    public FileContent(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }
}

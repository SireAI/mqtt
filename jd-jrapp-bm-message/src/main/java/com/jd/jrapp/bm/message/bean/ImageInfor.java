package com.jd.jrapp.bm.message.bean;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/27
 * Author:wangkai
 * Description: 图片参数
 * =====================================================
 */
public class ImageInfor {
    /**
     * 域名
     */
    private String imageDomain;
    /**
     * 地址
     */
    private String imageId;

    public String getImageDomain() {
        return imageDomain;
    }

    public void setImageDomain(String imageDomain) {
        this.imageDomain = imageDomain;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    @Override
    public String toString() {
        return "ImageInfor{" +
                "imageDomain='" + imageDomain + '\'' +
                ", imageId='" + imageId + '\'' +
                '}';
    }
}

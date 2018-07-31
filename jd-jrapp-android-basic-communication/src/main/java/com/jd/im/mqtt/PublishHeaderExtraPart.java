package com.jd.im.mqtt;

import com.jd.im.IVariableHeaderExtraPart;

import java.io.ByteArrayOutputStream;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/29
 * Author:wangkai
 * Description:"IM"协议额外的可变头信息
 * =====================================================
 */
public class PublishHeaderExtraPart extends IVariableHeaderExtraPart.Stub {
    /**
     * 数据编码类型,默认protobuffer  v3
     */
    private int codingType = 3;

    /**
     * 1代表普通消息发送，2代表消息到达回执，3代表离线
     */
    private int messageType = 1;

    public PublishHeaderExtraPart(int codingType, int messageType) {
        this.codingType = codingType;
        this.messageType = messageType;
    }

    @Override
    public byte[] extraVariableHeaderPart() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte extraInfor = (byte) (((codingType << 4) | ((messageType >> 8) & 0xF)) & 0xFF);
        byte operationType = (byte) (messageType & 0xFF);
        outputStream.write(extraInfor);
        outputStream.write(operationType);
        return outputStream.toByteArray();
    }
}

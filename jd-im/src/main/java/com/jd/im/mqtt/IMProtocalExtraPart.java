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
public class IMProtocalExtraPart extends IVariableHeaderExtraPart.Stub {
    @Override
    public byte[] extraVariableHeaderPart() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //数据编码类型,默认protobuffer  v3
        int codingType = 3;//
        //消息操作类型，IM聊天或信息上报
        int messageType = 1;
        byte extraInfor = (byte) (((codingType << 4) | ((messageType >> 8) & 0xF)) & 0xFF);
        byte operationType = (byte) (messageType & 0xFF);
        outputStream.write(extraInfor);
        outputStream.write(operationType);
        return outputStream.toByteArray();
    }
}

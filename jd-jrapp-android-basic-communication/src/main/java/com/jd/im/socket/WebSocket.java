package com.jd.im.socket;

import android.support.annotation.RestrictTo;

import java.io.IOException;
import java.net.Socket;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/26
 * Author:wangkai
 * Description: socket通信
 * =====================================================
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
 class WebSocket extends Socket {
    /**
     * 默认字节大小
     */
    public static final int DEFAULT_BUFFER_SIZE = 300;
    /**
     * 默认buffer
     */
    private byte[] data;
    /**
     * 自动扩展数据数组
     */
    private int readedCount = 0;

    public WebSocket() {
        super();
        reset();
    }

    /**
     * 阻塞方法，直到有响应，或者错误
     * 自动扩展buffer，在返回数据前读完所有数据
     *
     * @return 大于0表示正常，0表示对端关闭或者空字节数据，-1表示对端关闭链接或者流
     * @throws IOException
     */
    public int listenServer() throws IOException {
        int len = getInputStream().read(data, readedCount, data.length - readedCount);
        if (len > 0) {
            int available = getInputStream().available();
            if (available > 0) {
                byte[] expandArray = new byte[data.length + available];
                System.arraycopy(data, 0, expandArray, 0, data.length);
                readedCount = data.length;
                data = expandArray;
                listenServer();
            }
            return data.length;
        }
        return len;
    }

    public byte[] getData() {
        byte[] temp = data;
        reset();
        return temp;
    }

    private void reset() {
        data = new byte[DEFAULT_BUFFER_SIZE];
        readedCount = 0;
    }
}

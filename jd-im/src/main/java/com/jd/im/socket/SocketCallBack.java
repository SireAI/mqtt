package com.jd.im.socket;

 public interface SocketCallBack {
    /**
     * 连接异常
     */
    int CONNECT_EXCEPTION = 0;
    /**
     * socket读取异常
     */
    int SOCKET_READ_EXCPTION = 1;
    /**
     * socket写异常
     */
    int SOCKET_WRITE_EXCPTION = 2;
    /**
     *
     * @param type 0表示连接异常，1表示socket读异常,2表示socket写异常
     * @param e
     */
    void onSocketFailed(int type,Exception e);

    /**
     * 建立连接
     */
    void onSocketSuccees();

    /**
     * socket读到数据
     * @param data
     */
    void onDataArrived(byte[] data);

    /**
     * socket正常关闭
     */
    void onSocketClose(Runnable task);
}
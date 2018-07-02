package com.jd.im.socket;

import android.os.SystemClock;
import com.jd.im.utils.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;

import static android.content.ContentValues.TAG;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/24
 * Author:wangkai
 * Description:socket处理异步线程,主要处理接受消息数据
 * =====================================================
 */
 public class SocketWorker extends Thread {
    private static int DEFAULT_REPEAT_COUNT = 3;
    private static int RECONNECT_TIME = 5000;
    private final String host;
    private final int port;
    private final SocketCallBack socketCallBack;
    private int reconnectCount;
    private WebSocket webSocket;
    private int connectTimeOut;


    public SocketWorker(String host, int port, int connectTimeOut, int reconnectCount, SocketCallBack socketCallBack) {
        this.host = host;
        this.port = port;
        this.socketCallBack = socketCallBack;
        this.connectTimeOut = connectTimeOut;
        this.DEFAULT_REPEAT_COUNT = reconnectCount;
        this.reconnectCount = DEFAULT_REPEAT_COUNT;
        this.webSocket = new WebSocket();
    }

    @Override
    public void run() {
        // Keep listening to the InputStream while connected
        if (tryConnect()) {
            loop();
        }
    }

    /**
     * 尝试连接
     *
     * @return
     */
    private boolean tryConnect() {
        try {
            InetSocketAddress remoteAddr = new InetSocketAddress(host, port);
            // 阻塞，直到有响应或者异常
            webSocket.connect(remoteAddr, connectTimeOut);
        } catch (IOException e) {
            //连接重试
            if (reconnectCount > 0) {
                reconnectCount--;
                SystemClock.sleep(RECONNECT_TIME);
                Log.d(TAG, "connect失败，第"+(DEFAULT_REPEAT_COUNT-reconnectCount)+"次重试...");
                tryConnect();
            }else {
                if (socketCallBack != null) {
                    socketCallBack.onSocketFailed(SocketCallBack.CONNECT_EXCEPTION, e);
                }
            }
            return false;
        }
        resetReconnectParam();
        if (socketCallBack != null) {
            socketCallBack.onSocketSuccees();
        }
        return true;
    }

    /**
     * 重试成功将重置参数
     */
    private void resetReconnectParam() {
        this.reconnectCount = DEFAULT_REPEAT_COUNT;
    }

    /**
     * 监听服务端消息
     */
    private void loop() {
        while (!isInterrupted()) {
            // Read from the InputStream
            int len = 0;
            try {
                len = webSocket.listenServer();
                byte[] data = webSocket.getData();
                if (len > 0) {
                    if (socketCallBack != null) {
                        socketCallBack.onDataArrived(data);
                    }
                } else {
                    serverCloseSocket(len);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                if (socketCallBack != null) {
                    socketCallBack.onSocketFailed(SocketCallBack.SOCKET_READ_EXCPTION, e);
                    break;
                }
            }
        }
    }

    /**
     * 服务端关闭情况
     *
     * @param len
     */
    private void serverCloseSocket(int len) throws SocketException {
        if (len == 0) {
            //文件读取完毕后或者空内容也会返回0，这种情况在长链中不存在
            Log.e(TAG, "服务端正常关闭链接");
        } else if (len == -1) {
            Log.e(TAG, "服务端连接关闭或者读取超时");
        }
        cancel(false);
        throw new SocketException("服务端连接关闭");
    }


    /**
     * 关闭链接
     *
     * @param withExtraWork 关闭时是否有其他任务
     */
    public void cancel(boolean withExtraWork) {
        try {
            if (webSocket != null) {
                if (withExtraWork) {
                    if (socketCallBack != null && !webSocket.isClosed() && webSocket.isConnected()) {
                        //正常关闭
                        socketCallBack.onSocketClose(new Runnable() {
                            @Override
                            public void run() {
                                closeSocket();
                            }
                        });
                        return;
                    }
                } else {
                    closeSocket();
                }
            }
        } finally {
            if(this.isAlive()){
                interrupt();
            }
        }
    }

    /**
     * 此方法连接异常状态下会有异常信息
     *
     * @throws IOException
     */
    private synchronized void closeSocket() {
        try {
            if (webSocket != null) {
                webSocket.shutdownInput();
                webSocket.close();
                webSocket = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "socketworker服务停止异常：" + e.getMessage());
        }
    }

    /**
     * 发送字节数据
     *
     * @param data
     */
    public void sendData(byte[] data) {
        if (webSocket != null && webSocket.isConnected() && !webSocket.isOutputShutdown()) {
            try {
                webSocket.getOutputStream().write(data);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                if (socketCallBack != null) {
                    socketCallBack.onSocketFailed(SocketCallBack.SOCKET_WRITE_EXCPTION, e);
                }
            }
        }
    }
}
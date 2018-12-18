package com.jd.im.socket;

import android.content.Context;
import android.os.Binder;
import android.os.SystemClock;

import com.jd.im.heartbeat.net.NetStatusUtil;
import com.jd.im.service.MqttService;
import com.jd.im.utils.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static android.content.ContentValues.TAG;
import static com.jd.im.heartbeat.PinDetecter.ACTIVE_MODE;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/24
 * Author:wangkai
 * Description:socket处理异步线程,主要处理接受消息数据
 * =====================================================
 */
public class SocketWorker extends Thread {
    private final String host;
    private final int port;
    private final SocketCallBack socketCallBack;
    private final WeakReference<Context> weakContext;
    private int DEFAULT_REPEAT_COUNT;
    private int reconnectCount;
    private WebSocket webSocket;
    private int connectTimeOut;
    private AtomicInteger userMode = new AtomicInteger(ACTIVE_MODE);
    private InetSocketAddress remoteAddr;
    private  AtomicBoolean readyToInterupt =  new AtomicBoolean(false);


    public SocketWorker(String host, int port, int connectTimeOut, int reconnectCount, SocketCallBack socketCallBack, Context context) {
        this.host = host;
        this.port = port;
        this.socketCallBack = socketCallBack;
        this.connectTimeOut = connectTimeOut;
        this.DEFAULT_REPEAT_COUNT = reconnectCount;
        this.reconnectCount = DEFAULT_REPEAT_COUNT;
        this.weakContext = new WeakReference<Context>(context);
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
        if(readyToInterupt.get()){
            return false;
        }
         long startPoint = 0;
        try {
            //连接失败的webSocket会关闭掉，需要重新创建
            webSocket = new WebSocket();
            if(remoteAddr==null){
                remoteAddr = new InetSocketAddress(host, port);
            }
            // 阻塞，直到有响应或者异常
            startPoint = SystemClock.currentThreadTimeMillis();
            webSocket.connect(remoteAddr, connectTimeOut);
        } catch (IOException e) {
            //服务器宕机情况会导致快速失败，为避免过快重试导致内存压力
            final long endPoint = SystemClock.currentThreadTimeMillis();
            final long tryTime = endPoint - startPoint;
            final long tryThreshold = connectTimeOut-1000;
            if(tryTime <=tryThreshold){
                SystemClock.sleep(tryThreshold-tryTime);
            }
            //连接重试
           return retrayStrategy(e);
        }
        resetReconnectParam();
        if (socketCallBack != null) {
            socketCallBack.onSocketSuccees();
        }
        return true;
    }

    private boolean retrayStrategy(IOException e) {
        final Context context = weakContext.get();
        if(context!=null && NetStatusUtil.isConnected(context)){
            if(userMode.get() == ACTIVE_MODE){
                Log.d(TAG, "活跃模式，无限重试握手");
                return tryConnect();
            }else {
                if (reconnectCount > 0) {
                    reconnectCount--;
                    Log.d(TAG, "非活跃模式,握手失败，第" + (DEFAULT_REPEAT_COUNT - reconnectCount) + "次重试...");
                    return tryConnect();
                } else {
                    return notifyError(e);
                }
            }
        }else {
            return notifyError(e);
        }
    }

    /**
     * 连接失败通知
     * @param e
     * @return
     */
    private boolean notifyError(IOException e) {
        if (socketCallBack != null) {
            socketCallBack.onSocketFailed(SocketCallBack.CONNECT_EXCEPTION, e);
        }
        return false;
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
        while (!isInterrupted() && !readyToInterupt.get()) {
            // Read from the InputStream
            int len = 0;
            Binder.flushPendingCommands();
            try {
                byte[] data = webSocket.listenServer();
                if (data.length > 0) {
                    if (socketCallBack != null) {
                        socketCallBack.onDataArrived(data);
                    }
                } else {
                    serverCloseSocket(len);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage() == null ? "socket closed" : e.getMessage());
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
            tryInterrupt();
        }
    }

    private void tryInterrupt() {
        if (this.isAlive() || !isInterrupted()) {
            try {
                interrupt();
                readyToInterupt.set(true);
            }catch (Exception e){
                e.printStackTrace();
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
            if (webSocket != null && webSocket.isConnected()) {
                webSocket.shutdownInput();
                webSocket.close();
                webSocket = null;
            }else {
                Log.e(TAG, "socketworker服务连接已经中断");
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
                webSocket.getOutputStream().flush();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                if (socketCallBack != null) {
                    socketCallBack.onSocketFailed(SocketCallBack.SOCKET_WRITE_EXCPTION, e);
                }
            }
        }
    }

    public void recordeUserState(int mode) {
        this.userMode.set(mode);
    }
}
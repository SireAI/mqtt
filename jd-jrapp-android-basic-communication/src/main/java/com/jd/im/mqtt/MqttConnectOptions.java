
package com.jd.im.mqtt;



import android.os.RemoteException;

import com.jd.im.IMQTTMessage;
import com.jd.im.IMqttConnectOptions;
import com.jd.im.IVariableHeaderExtraPart;

import java.net.URI;
import java.net.URISyntaxException;

import static com.jd.im.mqtt.MQTTConnectionConstants.CLIENT_MAX_INTERVAL;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/16
 * Author:wangkai
 * Description:连接配置
 * =====================================================
 */
public class MqttConnectOptions extends IMqttConnectOptions.Stub {
    /**
     * 默认发送间隔以秒未单位，超过此间隔服务器认为超时
     *
     */
    public static final int KEEP_ALIVE_INTERVAL_DEFAULT = CLIENT_MAX_INTERVAL;
    /**
     * socket连接超时时间,ms单位
     */
    public static final int CONNECTION_TIMEOUT_DEFAULT = 10000;
    /**
     * 消息超时时间，s单位
     */
    public static final int MESSAGE_SEND_TIMEOUT_DEFAULT = 30000;
    /**
     * 消息最大在飞时间
     */
    public static final int MAX_INFLIGHT_DEFAULT = 10;
    /**
     * 默认清楚session
     */
    public static final boolean CLEAN_SESSION_DEFAULT = true;



    protected static final int URI_TYPE_TCP = 0;
    protected static final int URI_TYPE_SSL = 1;
    protected static final int URI_TYPE_LOCAL = 2;
    protected static final int URI_TYPE_WS = 3;
    protected static final int URI_TYPE_WSS = 4;

    private int keepAliveInterval = KEEP_ALIVE_INTERVAL_DEFAULT;
    private String willMessage = null;
    private String userName;
    private char[] password;
    private boolean cleanSession = CLEAN_SESSION_DEFAULT;
    private int connectionTimeout = CONNECTION_TIMEOUT_DEFAULT;
    private int messageSendTimeout = MESSAGE_SEND_TIMEOUT_DEFAULT;
    private String clientId ;
    private String protocalName = MQTTVersion.VERSION_311;
    /**
     * 主机地址
     */
    private String[] serverURIs = null;
    private int[] prorts = null;
    /**
     * 是否自动重连
     */
    private boolean automaticReconnect = true;
    /**
     * 重连次数
     */
    private int reconnectCount = 3;
    private int willQos = 2;
    private boolean retain;
    private boolean willFlag;
    private String willTopic;


    public MqttConnectOptions() {
    }


    @Override
    public char[] getPassword() {
        return password;
    }


    public MqttConnectOptions setPassword(String password) {
        this.password = password.toCharArray();
        return this;
    }


    @Override
    public String getUserName() {
        return userName;
    }


    public MqttConnectOptions setUserName(String userName) {
        if ((userName != null) && (userName.trim().equals(""))) {
            throw new IllegalArgumentException();
        }
        this.userName = userName;
        return this;
    }


    @Override
    public int getKeepAliveInterval() {
        return keepAliveInterval;
    }




    public MqttConnectOptions setKeepAliveInterval(int keepAliveInterval) throws IllegalArgumentException {
        if (keepAliveInterval < 0) {
            throw new IllegalArgumentException();
        }
        this.keepAliveInterval = keepAliveInterval;
        return this;
    }



    @Override
    public int getConnectionTimeout() {
        return connectionTimeout;
    }


    public MqttConnectOptions setConnectionTimeout(int connectionTimeout) {
        if (connectionTimeout < 0) {
            throw new IllegalArgumentException();
        }
        this.connectionTimeout = connectionTimeout;
        return this;
    }


    @Override
    public boolean isCleanSession() {
        return this.cleanSession;
    }


    public MqttConnectOptions setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
        return this;
    }

    public MqttConnectOptions setMessageSendTimeout(int messageSendTimeout) {
        this.messageSendTimeout = messageSendTimeout;
        return this;
    }

    @Override
    public String[] getServerURIs() {
        return serverURIs;
    }


    public MqttConnectOptions setServerURIs(String[] array) {
        String[] hosts = new String[array.length];
        int[] ports = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            hosts[i] = getHost(array[i]);
            int post = getPost(array[i]);
            if(post!=-1){
                ports[i]= post;
            }
        }
        this.serverURIs = hosts;
        this.prorts = ports;
        return this;
    }

    public MqttConnectOptions setHost(String host){
        setServerURIs(new String[]{host});
        return this;
    }


    public static String getHost(String srvURI) {
        String host = "";
        try {
            URI url = new URI(srvURI);
             host = url.getHost();
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(srvURI);
        }
        return host;
    }

    public static int getPost(String srvURI) {
        int  port = -1;
        try {
            URI url = new URI(srvURI);
            port = url.getPort();
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(srvURI);
        }
        return port;
    }


    @Override
    public boolean isAutomaticReconnect() {
        return automaticReconnect;
    }

    public MqttConnectOptions setAutomaticReconnect(boolean automaticReconnect) {
        this.automaticReconnect = automaticReconnect;
        return this;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getWillTopic() throws RemoteException {
        return this.willTopic;
    }
    public MqttConnectOptions setWillTopic(String willTopic){
        this.willTopic = willTopic;
        return this;
    }

    @Override
    public String getWillMessage() throws RemoteException {
        return this.willMessage;
    }

    public MqttConnectOptions setWillMessage(String willMessage){
        this.willMessage = willMessage;
        return this;
    }

    public MqttConnectOptions setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    @Override
    public boolean isWillRetain() throws RemoteException {
        return this.retain;
    }

    @Override
    public boolean willFlag() throws RemoteException {
        return this.willFlag;
    }
    public MqttConnectOptions setWillFlag(boolean willFlag){
        this.willFlag = willFlag;
        return this;
    }

    public MqttConnectOptions setWillRetain(boolean retain){
        this.retain = retain;
        return this;
    }

    @Override
    public int getWillQoS() throws RemoteException {
        return this.willQos;
    }
    public MqttConnectOptions setWillQos(int qos){
        this.willQos = qos;
        return this;
    }

    @Override
    public String getProtocalName() throws RemoteException {
        return this.protocalName;
    }

    @Override
    public int[] getPorts() throws RemoteException {
        return prorts;
    }

    @Override
    public int getReconnectCount() throws RemoteException {
        return this.reconnectCount;
    }

    @Override
    public int getMessageSendTimeout() throws RemoteException {
        return messageSendTimeout;
    }

    public MqttConnectOptions setReconnectCount(int reconnectCount) {
        this.reconnectCount = reconnectCount;
        return this;
    }

    public MqttConnectOptions setProrts(int[] prorts) {
        this.prorts = prorts;
        return this;
    }

    public MqttConnectOptions setProtocalName(String protocalName) {
        this.protocalName = protocalName;
        return this;
    }




}

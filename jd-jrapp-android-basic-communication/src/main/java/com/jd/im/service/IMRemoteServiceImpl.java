package com.jd.im.service;

import android.os.RemoteException;
import android.support.annotation.RestrictTo;

import com.jd.im.ConnectStateCallBack;
import com.jd.im.IMLocalService;
import com.jd.im.IMRemoteService;
import com.jd.im.IMqttConnectOptions;
import com.jd.im.IVariableHeaderExtraPart;
import com.jd.im.utils.Log;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/27
 * Author:wangkai
 * Description: 处理客户端调用
 * =====================================================
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
 class IMRemoteServiceImpl extends IMRemoteService.Stub {
    private final MqttService mqttService;
    private IMLocalService localService;


    public IMRemoteServiceImpl(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    @Override
    public int publish(String topic,byte[] payload,  byte qos,IVariableHeaderExtraPart extraPart) throws RemoteException {
        int identifier = -1;
        if(mqttService!=null){
            identifier = mqttService.publish(topic,payload,qos,extraPart);
        }
        return identifier;
    }


    @Override
    public void connect(IMqttConnectOptions connectOptions, ConnectStateCallBack connectCallBack) throws RemoteException {
        if(mqttService!=null){
            if(mqttService.isServiceOk()){
                mqttService.setTryReconnect(connectOptions.isAutomaticReconnect());
                mqttService.connect(connectOptions, connectCallBack);
            }else {
                mqttService.setConnectConfigure(connectOptions,connectCallBack);
            }

        }
    }


    @Override
    public void disConnect(boolean tryReconnect) throws RemoteException {
        if(mqttService!=null){
            mqttService.setTryReconnect(tryReconnect);
            mqttService.disconnect(true);
        }
    }

    @Override
    public int subscribe( String[] topics,  byte[] qoss) throws RemoteException {
        int identifier = -1;
        if(mqttService!=null){
            identifier = mqttService.subscribe(topics,qoss);
        }
        return identifier;
    }

    @Override
    public int unSubscribe(String[] topicFilters) throws RemoteException {
        int identifier = -1;
        if(mqttService!=null){
            identifier = mqttService.unSubscribe(topicFilters);
        }
        return identifier;
    }

    @Override
    public void bindLocalService(IMLocalService localService) throws RemoteException {
        this.localService = localService;
    }

    @Override
    public void unBindLocalService() throws RemoteException {
        this.localService = null;
    }
    @Override
    public IMLocalService getLocalService() {
        return localService;
    }

    @Override
    public void modeEvent(int mode) throws RemoteException {
        mqttService.setMode(mode);
    }

    @Override
    public void openLog() throws RemoteException {
        Log.openLog();
    }

}

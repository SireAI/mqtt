// IMRemoteService.aidl
package com.jd.im;
import com.jd.im.IMqttConnectOptions;
import com.jd.im.IMLocalService;
import com.jd.im.ConnectStateCallBack;
import com.jd.im.IVariableHeaderExtraPart;

interface IMRemoteService {
       /**
         * 发布
         * @param message
         */
        int publish(String topic,in byte[] payload,  byte qos, boolean retain);


        /**
         * 连接
         * @param message
         */
        void connect(IMqttConnectOptions connectOptions,ConnectStateCallBack connectCallBack);

        /**
         * 断开连接
         * @param message
         * @param tryReconnect
         */
        void disConnect(boolean tryReconnect);

        /**
         * 订阅
         */
        int subscribe(in String[] topics, in byte[] qoss);

        /**
         * 取消订阅
         */
        int unSubscribe(in String[] topicFilters);

          /**
           * 绑定前端服务
           */
         void bindLocalService(IMLocalService localService);

         /**
          * 解绑前端服务
          */
         void unBindLocalService();

         /**
          * 获取前端服务
          */
         IMLocalService getLocalService();

         void modeEvent(int mode);

         void openLog();

}

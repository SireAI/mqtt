// IMLocalService.aidl
package com.jd.im;

import com.jd.im.IMQTTMessage;

interface IMLocalService {
    /**
     * 推送消息
     */
   void push2Client(IMQTTMessage message);
   /**
    * 操作回调
    */
   void operationResult(boolean success,int id,in byte[] extraInfor);

}

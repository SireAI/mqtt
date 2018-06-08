package com.jd.im.client;

import android.os.RemoteException;

import com.jd.im.IMLocalService;
import com.jd.im.IMQTTMessage;
import com.jd.im.converter.Converter;
import com.jd.im.converter.ConverterProcessor;
import com.jd.im.socket.BlockingLooper;
import com.jd.im.utils.Log;

import static com.jd.im.client.Task.DESERIALIZE_RESPONSE;
import static com.jd.im.client.Task.SERIALIZE_REQUEST;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/24
 * Author:wangkai
 * Description:主要处理服务端主动推送消息
 * =====================================================
 */
 class IMLocalServiceImpl extends IMLocalService.Stub implements BlockingLooper.Callback<Task> {

    private static final String TAG = "IMLocalServiceImpl";
    private final BlockingLooper<Task> pushBlockingLooper;
    private final ClientReceiver clientReceiver;
    private final ConverterProcessor converterProcessor;

    public IMLocalServiceImpl(ClientReceiver clientReceiver, Converter.Factory factory) {
        this.clientReceiver = clientReceiver;
        pushBlockingLooper = new BlockingLooper<>(this);
        converterProcessor = new ConverterProcessor(factory);
    }

    @Override
    public void push2Client(IMQTTMessage message) {
        enqueueTask(Task.create(DESERIALIZE_RESPONSE, message));
    }

    public synchronized void enqueueTask(Task task) {
        if (pushBlockingLooper != null) {
            pushBlockingLooper.enqueueTask(task);
        }
    }

    @Override
    public void operationResult(boolean success, int id) throws RemoteException {
        if (clientReceiver != null) {
            clientReceiver.onOperationCallBack(success, id);
        }
    }

    @Override
    public boolean meetTheCondition() {
        return true;
    }

    /**
     * 处理push消息，异步线程
     *
     * @param task
     */
    @Override
    public void onHandleTask(Task task) {
        if (clientReceiver != null && converterProcessor != null) {
            if (task.getType() == SERIALIZE_REQUEST) {
                Object object = task.getObject();
                if (object instanceof Publish) {
                    Object payload = ((Publish) object).getPayload();
                    byte[] serialize = converterProcessor.serialize(payload);
                    if (serialize != null) {
                        Publish publish = Publish.create(((Publish) object).getTopic(), serialize, ((Publish) object).getMessageCallBack());
                        clientReceiver.onRequestSerialized(publish);
                    } else {
                        Log.e(TAG, "序列化错误...");
                    }

                }
            } else if (task.getType() == DESERIALIZE_RESPONSE) {
                try {
                    Object pushMessage = task.getObject();
                    if (pushMessage instanceof IMQTTMessage) {
                        Object deserialize = converterProcessor.deserialize(((IMQTTMessage) pushMessage).getPayload());
                        if (deserialize != null) {
                            clientReceiver.onPushArrived(deserialize);
                        } else {
                            Log.e(TAG, "反序列化错误...");
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            } else {
                Log.e(TAG, "任务类型未定义...");
            }
        }
    }

    interface ClientReceiver {
        void onPushArrived(Object message);

        void onOperationCallBack(boolean success, int id);

        void onRequestSerialized(Publish<byte[]> publish);
    }

}

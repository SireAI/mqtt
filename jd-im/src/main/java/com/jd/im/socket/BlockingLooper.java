package com.jd.im.socket;

import java.util.concurrent.LinkedBlockingQueue;


/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/11
 * Author:wangkai
 * Description:异步线程排队处理任务
 * =====================================================
 */
public class BlockingLooper<T> {
    private LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();
    private  WorkerThread workerThread;

    public BlockingLooper(Callback callback) {
        workerThread = new WorkerThread(queue, callback);
        workerThread.start();
    }


    private static class WorkerThread extends Thread{
        private  LinkedBlockingQueue queue;
        private  Callback callback;



        public WorkerThread(LinkedBlockingQueue queue, Callback callback) {
            this.queue = queue;
            this.callback = callback;
        }

        @Override
        public void run() {
            loop();
        }
        private  void loop(){
            if(queue!=null){
                while (!isInterrupted()){
                    if(callback == null){
                        throw new RuntimeException("callback回调不能为空");
                    }
                    if(callback.meetTheCondition()){
                        try {
                            Object task = queue.take();
                            if(task==null)return;
                            if(callback!=null){
                                callback.onHandleTask(task);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }



    /**
     * 排队任务
     * @param task
     */
    public void enqueueTask(T task){
        queue.offer(task);
    }

    /**
     * 取消任务，任何有可能已经完成，有可能为完成
     * @param task
     */
    public boolean cancelTask(T task){
        return queue.remove(task);
    }


    /**
     * 程序推出时调用
     */
    public void release(){
        if(queue!=null){
            queue.clear();
            queue = null;
        }
        if(workerThread!=null){
            workerThread.interrupt();
            workerThread = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }
    public interface Callback<T>{
        /**
         * 任务执行前提条件
         * @return
         */
        boolean meetTheCondition();
        /**
         * 取出任务后
         * @param task
         */
       void onHandleTask(T task);
    }
}

package com.jd.im.socket;

import com.jd.im.utils.Log;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/29
 * Author:wangkai
 * Description: 时间轮定时器
 * =====================================================
 */
public class TimingWheel<E extends SlotElement> {
    private static final String TAG = "TimingWheel";
    /**
     * 指针跳动时间间隔
     */
    private final long tickDuration;
    /**
     * 一周跳动多少次
     */
    private final int ticksPerWheel;
    /**
     * 当前指针位置
     */
    private volatile int currentTickIndex = 0;
    /**
     * 时间过期通知
     */
    private final CopyOnWriteArrayList<ExpirationListener<E>> expirationListeners = new CopyOnWriteArrayList();
    /**
     * 轮结构
     */
    private final ArrayList<Slot<E>> wheel;
    /**
     * 指针，与槽的对应关系
     */
    private final Map<Integer, Slot<E>> indicator = new ConcurrentHashMap<>();
    /**
     * 是否停止
     */
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    /**
     * 读写锁
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    /**
     * 定时器运行线程
     */
    private Thread workerThread;

    private boolean freeze = false;
    /**
     * 构建时间轮
     * @param tickDuration  指针转动间隔事件
     * @param ticksPerWheel 一圈指针转动数
     * @param timeUnit  时间单位
     */
    public TimingWheel(int tickDuration, int ticksPerWheel, TimeUnit timeUnit) {  
        if (timeUnit == null) {  
            throw new NullPointerException("unit");  
        }  
        if (tickDuration <= 0) {  
            throw new IllegalArgumentException("tickDuration must be greater than 0: " + tickDuration);  
        }  
        if (ticksPerWheel <= 0) {  
            throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel);  
        }  
          
        this.wheel = new ArrayList<>();
        this.tickDuration = TimeUnit.MILLISECONDS.convert(tickDuration, timeUnit);
        this.ticksPerWheel = ticksPerWheel + 1;  
          
        for (int i = 0; i < this.ticksPerWheel; i++) {  
            wheel.add(new Slot<E>(i));  
        }  
        wheel.trimToSize();  
          
        workerThread = new Thread(new TickWorker(), "Timing-Wheel");  
    }  
      

    public void start() {  
        if (shutdown.get()) {  
            throw new IllegalStateException("Cannot be started once stopped");  
        }  
  
        if (workerThread!=null&&!workerThread.isAlive()) {
            workerThread.start();  
        }  
    }

    public void setFreeze(boolean freeze) {
        this.freeze = freeze;
        if(!freeze){
            synchronized (lock){
                lock.notify();
            }
        }
    }

    /**
     * 停止转动
     * @return
     */
    public boolean stop() {  
        if (!shutdown.compareAndSet(false, true)) {  
            return false;  
        }  
          
        boolean interrupted = false;  
        while (workerThread.isAlive()) {  
            workerThread.interrupt();  
            try {  
                workerThread.join(100);  
            } catch (InterruptedException e) {  
                interrupted = true;  
            }  
        }  
        if (interrupted) {  
            Thread.currentThread().interrupt();  
        }  
          
        return true;  
    }  

    public void addExpirationListener(ExpirationListener<E> listener) {  
        expirationListeners.add(listener);  
    }  
      
    public void removeExpirationListener(ExpirationListener<E> listener) {  
        expirationListeners.remove(listener);  
    }

    /**
     * 遍历事件
     * @param forEach
     */
    public void each(ForEach<E> forEach){
        if(forEach == null)return;
        for (int key:indicator.keySet()) {
            Slot<E> eSlot = indicator.get(key);
            forEach.on(eSlot.elements.get(key));
        }
    }

    /**
     * 添加一个事件
     * @param e
     * @return
     */
    public long add(E e) {  
        synchronized(e) {
            //如果该事件存在就删除
            checkAdd(e);
            //获取当前指针的前一个位置,前一个位置的槽一定是空的
            int previousTickIndex = getPreviousTickIndex();
            Slot<E> slot = wheel.get(previousTickIndex);
            //添加进入槽
            slot.add(e);
            //纪录每个事件添加进入的槽
            indicator.put(e.getId(), slot);
            //返回这个事件的超时时间
            return (ticksPerWheel - 1) * tickDuration;  
        }  
    }

    public synchronized boolean hasEvent(int id){
       return indicator.containsKey(id);
    }
      
    private void checkAdd(E e) {  
        Slot<E> slot = indicator.get(e.getId());
        if (slot != null) {  
            slot.remove(e.getId());
        }  
    }  
      
    private int getPreviousTickIndex() {  
        lock.readLock().lock();  
        try {  
            int cti = currentTickIndex;  
            if (cti == 0) {  
                return ticksPerWheel - 1;  
            }  
              
            return cti - 1;  
        } finally {  
            lock.readLock().unlock();  
        }  
    }



    /**
     * 删除一个事件
      * @param id 元素id
     * @return
     */
    public boolean remove(int id) {
        synchronized (lock) {
            Slot<E> slot = indicator.get(id);
            if (slot == null) {  
                return false;  
            }
            indicator.remove(id);
            return slot.remove(id) != null;
        }  
    }  
  
    private void notifyExpired(int idx) {
        //获取制定位置的槽
        Slot<E> slot = wheel.get(idx);
        //槽中元素
        Set<Integer> elements = slot.elements();
        //遍历槽中元素
        for (Integer e : elements) {
            //删除该槽中过期事件
            E remove = slot.remove(e);
            synchronized (remove) {
                Slot<E> latestSlot = indicator.get(e);
                if(latestSlot == null) continue;
                if (latestSlot.equals(slot)) {
                    indicator.remove(e);  
                }  
            }  
            for (ExpirationListener listener : expirationListeners) {
                listener.expired(remove);

            }  
        }  
    }  
      

    private class TickWorker implements Runnable {
  
        private long startTime;  
        private long tick;
  
        @Override  
        public void run() {  
            startTime = System.currentTimeMillis();  
            tick = 1;

            for (int i = 0; !shutdown.get(); i++) {
                if (i == wheel.size()) {
                    i = 0;
                }  
                lock.writeLock().lock();
                try {
                    currentTickIndex = i;
                } finally {  
                    lock.writeLock().unlock();  
                }
                if(freeze){
                    synchronized (lock){
                        try {
                            Log.d(TAG,"时间轮冻结...");
                            lock.wait();
                            Log.d(TAG,"时间轮解冻...");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                notifyExpired(currentTickIndex);
                waitForNextTick();  
            }  
        }  
  
        private void waitForNextTick() {  
            for (;;) {  
                long currentTime = System.currentTimeMillis();  
                long sleepTime = tickDuration * tick - (currentTime - startTime);  
  
                if (sleepTime < 0) {
                    //由于等待产生的时间差额
                    tick+=(-sleepTime/tickDuration);
                    break;  
                }  
  
                try {  
                    Thread.sleep(sleepTime);  
                } catch (InterruptedException e) {  
                    return;  
                }  
            }  
              
            tick++;  
        }  
    }

    /**
     * 槽本身是一个集合结构，这样一个槽可以存储多个事件
     * @param <E>
     */
    private static class Slot<E extends SlotElement> {
          
        private int id;  
        private Map<Integer, E> elements = new ConcurrentHashMap();
          
        public Slot(int id) {  
            this.id = id;  
        }  
  
        public void add(E e) {  
            elements.put(e.getId(), e);
        }  
          
        public E remove(int e) {
            return elements.remove(e);
        }  
          
        public Set<Integer> elements() {
            return elements.keySet();  
        }  
  
        @Override  
        public int hashCode() {  
            final int prime = 31;  
            int result = 1;  
            result = prime * result + id;  
            return result;  
        }  
  
        @Override  
        public boolean equals(Object obj) {  
            if (this == obj)  
                return true;  
            if (obj == null)  
                return false;  
            if (getClass() != obj.getClass())  
                return false;  
            Slot other = (Slot) obj;
            if (id != other.id)  
                return false;  
            return true;  
        }  
  
        @Override  
        public String toString() {  
            return "Slot [id=" + id + ", elements=" + elements + "]";  
        }  
          
    }

    public interface ForEach<E>{
        void on(E element);
    }
      
}  
package com.sire.micro.databuffer.storage;

import android.os.MemoryFile;

import com.sire.micro.databuffer.CacheEntry;
import com.sire.micro.databuffer.cache.Topic;

import java.io.IOException;
import java.util.List;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/10/23
 * Author:wangkai
 * Description: 使用内存映射方式存储文件
 * =====================================================
 */
public class FileStore implements IStore {

    private final MemoryFile mMemoryFile;

    public FileStore(int length) throws IOException {
        mMemoryFile = new MemoryFile("databuffer", length);
    }

    @Override
    public void insert(CacheEntry cacheEntry) {
    }

    @Override
    public void delete(int topic, String key) {

    }

    @Override
    public void delete(int topic) {

    }

    @Override
    public void update(CacheEntry cacheEntry) {

    }

    @Override
    public List<CacheEntry> query(int topic) {
        return null;
    }

    @Override
    public List<CacheEntry> queryAllEnties() {
        return null;
    }

    @Override
    public List<Topic> queryAllTopics() {
        return null;
    }

    @Override
    public List[] queryAll() {
        return new List[0];
    }

    @Override
    public void close() {

    }

    @Override
    public void insert(int topic, int maxSize, long expiredTime) {

    }
}

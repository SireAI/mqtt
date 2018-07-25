package sire.com.micro.databuffer;

import java.util.List;

public interface IStore {

    void insert(CacheEntry cacheEntry);

    void delete(int topic,String key);

    void update(CacheEntry cacheEntry);

    List<CacheEntry> query(int topic);

    List<CacheEntry> queryAll();

    void close();
}

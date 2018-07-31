package com.sire.micro.databuffer.cache;

import android.support.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface CacheDelegate {
    void onTrimSize(int topic, String key, ValueInfor oldValue);

    void onManualDelete(int topic, String key, ValueInfor oldValue);

    void onCache(int topic,  String key, ValueInfor valueInfor);

    void onTopicCacheBuild(int topic,int maxSize,long expiredTime);

    void onDeleteTopic(int topic);
}

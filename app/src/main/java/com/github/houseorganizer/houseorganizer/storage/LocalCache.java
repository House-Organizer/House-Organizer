package com.github.houseorganizer.houseorganizer.storage;

import androidx.annotation.NonNull;

import java.util.LinkedHashMap;

public class LocalCache {
    protected static LinkedHashMap<CacheKey, Object> cache = new LinkedHashMap<CacheKey, Object>();

    public static boolean contains(CacheKey key){
        return cache.containsKey(key);
    }

    public static void put(CacheKey key, Object object){
        if(cache.containsKey(key)){
            cache.replace(key, object);
        } else {
            cache.put(key, object);
        }
    }

    public static Object get(CacheKey key){
        return cache.get(key);
    }

    public enum CacheKey{
        HOUSEHOLD, EVENT, TASK
    }
}

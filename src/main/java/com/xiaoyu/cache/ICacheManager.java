package com.xiaoyu.cache;

import java.lang.reflect.Method;

import com.xiaoyu.cache.exception.CacheCenterConnectionException;
import com.xiaoyu.cache.to.CacheKeyTO;
import com.xiaoyu.cache.to.CacheWrapper;

/**
 * 缓存管理
 * @author xiaoyu
 */
public interface ICacheManager {

    /**
     * 往缓存写数据
     * @param cacheKey 缓存Key
     * @param result 缓存数据
     * @param method Method
     * @param args args
     * @throws CacheCenterConnectionException
     */
    void setCache(final CacheKeyTO cacheKey, final CacheWrapper<Object> result, final Method method, final Object args[])
        throws CacheCenterConnectionException;

    /**
     * 根据缓存Key获得缓存中的数据
     * @param key 缓存key
     * @param method Method
     * @param args args
     * @return 缓存数据
     * @throws CacheCenterConnectionException
     */
    CacheWrapper<Object> get(final CacheKeyTO key, final Method method, final Object args[]) throws CacheCenterConnectionException;

    /**
     * 删除缓存
     * @param key 缓存key
     * @throws CacheCenterConnectionException
     */
    void delete(final CacheKeyTO key) throws CacheCenterConnectionException;
}

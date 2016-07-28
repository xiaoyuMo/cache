package com.xiaoyu.cache.to;

import java.io.Serializable;

import lombok.Data;

/**
 * 对缓存数据进行封装
 * @author xiaoyu
 */
@Data
public class CacheWrapper<T> implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 缓存数据
     */
    private T cacheObject;

    /**
     * 最后加载时间
     */
    private long lastLoadTime;

    /**
     * 缓存时长
     */
    private int expire;

    public CacheWrapper() {
    }

    public CacheWrapper(T cacheObject, int expire) {
        this.cacheObject=cacheObject;
        this.lastLoadTime=System.currentTimeMillis();
        this.expire=expire;
    }

    /**
     * 判断缓存是否已经过期
     * @return boolean
     */
    public boolean isExpired() {
        if(expire > 0) {
            return (System.currentTimeMillis() - lastLoadTime) > expire * 1000;
        }
        return false;
    }

	public T getCacheObject() {
		return cacheObject;
	}

	public void setCacheObject(T cacheObject) {
		this.cacheObject = cacheObject;
	}

	public long getLastLoadTime() {
		return lastLoadTime;
	}

	public void setLastLoadTime(long lastLoadTime) {
		this.lastLoadTime = lastLoadTime;
	}

	public int getExpire() {
		return expire;
	}

	public void setExpire(int expire) {
		this.expire = expire;
	}

}
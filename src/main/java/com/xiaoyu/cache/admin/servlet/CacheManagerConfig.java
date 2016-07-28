package com.xiaoyu.cache.admin.servlet;

import javax.servlet.http.HttpServletRequest;

import com.xiaoyu.cache.AbstractCacheManager;

public interface CacheManagerConfig {

	String[] getCacheManagerNames(HttpServletRequest req);

	AbstractCacheManager getCacheManagerByName(HttpServletRequest req, String cacheManagerName);
}

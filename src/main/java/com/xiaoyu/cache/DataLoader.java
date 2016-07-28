package com.xiaoyu.cache;

import org.apache.log4j.Logger;

import com.xiaoyu.cache.annotation.Cache;
import com.xiaoyu.cache.aop.CacheAopProxyChain;
import com.xiaoyu.cache.to.AutoLoadConfig;
import com.xiaoyu.cache.to.AutoLoadTO;
import com.xiaoyu.cache.to.CacheKeyTO;
import com.xiaoyu.cache.to.CacheWrapper;
import com.xiaoyu.cache.to.ProcessingTO;

/**
 * 数据加载器
 * @author xiaoyu
 */
public class DataLoader {

    private static final Logger logger=Logger.getLogger(DataLoader.class);

    private final AbstractCacheManager cacheManager;

    private final CacheAopProxyChain pjp;

    private final CacheKeyTO cacheKey;

    private final Cache cache;

    private final Object[] arguments;

    private final AutoLoadTO autoLoadTO;

    private boolean isFirst=true;

    private long loadDataUseTime;

    private CacheWrapper<Object> cacheWrapper;

    public DataLoader(CacheAopProxyChain pjp, AutoLoadTO autoLoadTO, CacheKeyTO cacheKey, Cache cache,
        AbstractCacheManager cacheManager) {
        this.pjp=pjp;
        this.autoLoadTO=autoLoadTO;
        this.cacheKey=cacheKey;
        this.cache=cache;
        this.cacheManager=cacheManager;
        if(null == autoLoadTO) {// 用户请求
            arguments=pjp.getArgs();
        } else {// 来自AutoLoadHandler的请求
            arguments=autoLoadTO.getArgs();
        }
    }

    public DataLoader(CacheAopProxyChain pjp, CacheKeyTO cacheKey, Cache cache, AbstractCacheManager cacheManager,
        Object[] arguments) {
        this.pjp=pjp;
        this.cacheKey=cacheKey;
        this.cache=cache;
        this.cacheManager=cacheManager;
        this.arguments=arguments;
        this.autoLoadTO=null;
    }

    public DataLoader(CacheAopProxyChain pjp, Cache cache, AbstractCacheManager cacheManager) {
        this(pjp, null, null, cache, cacheManager);
    }

    public DataLoader(CacheAopProxyChain pjp, CacheKeyTO cacheKey, Cache cache, AbstractCacheManager cacheManager) {
        this(pjp, null, cacheKey, cache, cacheManager);
    }

    public DataLoader loadData() throws Throwable {
        ProcessingTO isProcessing=cacheManager.processing.get(cacheKey);
        ProcessingTO processingTO=null;
        if(null == isProcessing) {
            processingTO=new ProcessingTO();
            ProcessingTO _isProcessing=cacheManager.processing.putIfAbsent(cacheKey, processingTO);// 为发减少数据层的并发，增加等待机制。
            if(null != _isProcessing) {
                isProcessing=_isProcessing;// 获取到第一个线程的ProcessingTO 的引用，保证所有请求都指向同一个引用
            }
        }
        Object lock=null;
        String tname=Thread.currentThread().getName();
        if(null == isProcessing) {// 当前并发中的第一个请求
            isFirst=true;
            lock=processingTO;
            try {
                logger.debug(tname + " first thread!");
                Object result=getData();
                buildCacheWrapper(result);
                processingTO.setCache(cacheWrapper);// 本地缓存
            } catch(Throwable e) {
                processingTO.setError(e);
                throw e;
            } finally {
                processingTO.setFirstFinished(true);
                cacheManager.processing.remove(cacheKey);
                synchronized(lock) {
                    lock.notifyAll();
                }
            }
        } else {
            isFirst=false;
            lock=isProcessing;
            long startWait=isProcessing.getStartTime();

            do {// 等待
                if(null == isProcessing) {
                    break;
                }
                if(isProcessing.isFirstFinished()) {
                    CacheWrapper<Object> _cacheWrapper=isProcessing.getCache();// 从本地缓存获取数据， 防止频繁去缓存服务器取数据，造成缓存服务器压力过大
                    logger.debug(tname + " do FirstFinished" + " is null :" + (null == _cacheWrapper));
                    if(null != _cacheWrapper) {
                        cacheWrapper=_cacheWrapper;
                        return this;
                    }
                    Throwable error=isProcessing.getError();
                    if(null != error) {// 当DAO出错时，直接抛异常
                        logger.debug(tname + " do error");
                        throw error;
                    }
                    break;
                } else {
                    synchronized(lock) {
                        logger.debug(tname + " do wait");
                        try {
                            lock.wait(50);// 如果要测试lock对象是否有效，wait时间去掉就可以
                        } catch(InterruptedException ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                    }
                }
            } while(System.currentTimeMillis() - startWait < cache.waitTimeOut());
            if(null == cacheWrapper) {
                cacheWrapper=cacheManager.get(cacheKey, pjp.getMethod(), this.arguments);
            }
            try {
                if(null == cacheWrapper) {
                    Object result=getData();
                    buildCacheWrapper(result);
                }
            } catch(Throwable e) {
                throw e;
            } finally {
                synchronized(lock) {
                    lock.notifyAll();
                }
            }
        }
        return this;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public Object getData() throws Throwable {
        try {
            if(null != autoLoadTO) {
                autoLoadTO.setLoading(true);
            }
            long loadDataStartTime=System.currentTimeMillis();
            Object result=pjp.doProxyChain(arguments);
            loadDataUseTime=System.currentTimeMillis() - loadDataStartTime;
            AutoLoadConfig config=cacheManager.getAutoLoadHandler().getConfig();
            if(config.isPrintSlowLog() && loadDataUseTime >= config.getSlowLoadTime()) {
                String className=pjp.getTargetClass().getName();
                logger.error(className + "." + pjp.getMethod().getName() + ", use time:" + loadDataUseTime + "ms");
            }
            return result;
        } catch(Throwable e) {
            throw e;
        } finally {
            if(null != autoLoadTO) {
                autoLoadTO.setLoading(false);
            }
        }
    }

    public DataLoader buildCacheWrapper(Object result) {
        int expire=cache.expire();
        try {
            expire=cacheManager.getScriptParser().getRealExpire(cache.expire(), cache.expireExpression(), arguments, result);
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
        cacheWrapper=new CacheWrapper<Object>(result, expire);
        return this;
    }

    public CacheWrapper<Object> getCacheWrapper() {
        if(null == cacheWrapper) {
            throw new RuntimeException("run loadData() or buildCacheWrapper() please!");
        }
        return cacheWrapper;
    }

    public long getLoadDataUseTime() {
        return loadDataUseTime;
    }

}

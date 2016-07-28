package com.xiaoyu.example;

import java.util.List;

import com.xiaoyu.cache.annotation.Cache;
import com.xiaoyu.cache.annotation.CacheDelete;
import com.xiaoyu.cache.annotation.CacheDeleteKey;
import com.xiaoyu.cache.type.CacheOpType;

/**
 * 类调用例子
 * @author moxiaoyu
 */
public class ExampleClass {

    private static final String cacheName="example";

    private static final int expire = 121;

    /**
     * 添加example的同时，把数据放到缓存中
     * @param userName
     * @return
     */
    @Cache(expire=expire, key="'" + cacheName + "'+#retVal.id", opType=CacheOpType.WRITE)
    public Object addExample(String exampleName) {
        return null;
    }

    /**
     * 使用 hash 方法，将参数转为字符串
     * @param example
     * @return
     */
    @Cache(expire=expire, key="'" + cacheName + "'+#hash(#args)")
    public List<Object> getExampleList(Object example) {
        return null;
    }

    /**
     * 使用自定义缓存Key，并在指定的条件下才进行缓存。
     * @param id
     * @return
     */
    @Cache(expire=expire, autoload=true, key="'exampleId'+#args[0]", condition="#args[0]>0")
    public Object getExampleById(Integer id) {
        return null;
    }

    /**
     * 使用自定义缓存Key，并在指定的条件下才进行缓存。
     * @param id
     * @return
     */
    @Cache(expire=expire, autoload=false, key="'exampleId2'+#args[0]", condition="#args[0]>0")
    public Object getExampleById2(Integer id) throws Exception {
        return null;
    }

    // 注意：因为没有用 SpEL表达式，所以不需要用单引号
    @CacheDelete({@CacheDeleteKey(value="'exampleId2'+#args[0]", condition="#args[0]>0")})
    public void clearExampleById2Cache(Integer id) {
    }

    @CacheDelete({@CacheDeleteKey(value="'example'+#args[0].id", condition="null != #args[0]")})
    public void updateExampleName(Object user) {
    }

    // 注意：因为没有用 SpEL表达式，所以不需要用单引号
    @CacheDelete({@CacheDeleteKey(value="example*")})
    public void clearExampleCache() {
    }

}

package com.xiaoyu.example;

import com.xiaoyu.cache.annotation.Cache;
import com.xiaoyu.cache.annotation.CacheDelete;
import com.xiaoyu.cache.annotation.CacheDeleteKey;

/**
 * 接口调用例子
 * @author moxiaoyu
 */
public interface ExampleInterface {

    @Cache(expire=600, autoload=true, key="'exampkeid_'+#args[0]", condition="#args[0]>0")
    Object getExampleById(Integer id);

    @CacheDelete({@CacheDeleteKey(value="'exampkeid_'+#args[0].id")})
    int incExample(Object Example);

}

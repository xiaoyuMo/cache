package com.xiaoyu.cache.serializer;

import java.lang.ref.WeakReference;

import com.caucho.hessian.io.AbstractSerializerFactory;
import com.caucho.hessian.io.Deserializer;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.Serializer;

public class WeakReferenceSerializerFactory extends AbstractSerializerFactory {

    private final WeakReferenceSerializer beanSerializer=new WeakReferenceSerializer();

    private final WeakReferenceDeserializer beanDeserializer=new WeakReferenceDeserializer();

    @Override
    public Serializer getSerializer(@SuppressWarnings("rawtypes") Class cl) throws HessianProtocolException {
        if(WeakReference.class.isAssignableFrom(cl)) {
            return beanSerializer;
        }
        return null;
    }

    @Override
    public Deserializer getDeserializer(@SuppressWarnings("rawtypes") Class cl) throws HessianProtocolException {
        if(WeakReference.class.isAssignableFrom(cl)) {
            return beanDeserializer;
        }
        return null;
    }

}

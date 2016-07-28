package com.xiaoyu.cache.serializer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyListSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyMapSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptySetSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonListSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonMapSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonSetSerializer;
import de.javakaffee.kryoserializers.GregorianCalendarSerializer;
import de.javakaffee.kryoserializers.JdkProxySerializer;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import de.javakaffee.kryoserializers.cglib.CGLibProxySerializer;

/**
 * Created by xiaoyu on 14-10-3.
 */
//Kryo is not thread safe. Each thread should have its own Kryo,
// Input, and Output instances. Also, the byte[] Input uses
// may be modified and then returned to its original state during deserialization,
// so the same byte[] "should not be used concurrently in separate threads.
public class KryoThreadLocalSerializer implements ISerializer<Object> {

    private KryoThreadLocalSerializer() {
    }

    /**
     * a singleton of kryo thread local
     *
     * @return
     */
    public static KryoThreadLocalSerializer getInstance() {
        return Singleton.kryoThreadLocal;
    }

    /**
     * creat a Singleton
     */
    private static class Singleton {
        private static final KryoThreadLocalSerializer kryoThreadLocal = new KryoThreadLocalSerializer();
    }

    private final ThreadLocal<KryoHolder> kryoThreadLocal = new ThreadLocal<KryoHolder>() {
        @Override
        protected KryoHolder initialValue() {
            return new KryoHolder(new Kryo());
        }
    };

    private class KryoHolder {
        private Kryo kryo;
        static final int BUFFER_SIZE = 1024;
        private Output output = new Output(BUFFER_SIZE, -1);     //reuse
        private Input input = new Input();

        KryoHolder(Kryo kryo) {
            this.kryo = kryo;
            this.kryo.setReferences(false);

            //   register
            this.kryo.register(Arrays.asList("").getClass(), new ArraysAsListSerializer());
            this.kryo.register(Collections.EMPTY_LIST.getClass(), new CollectionsEmptyListSerializer());
            this.kryo.register(Collections.EMPTY_MAP.getClass(), new CollectionsEmptyMapSerializer());
            this.kryo.register(Collections.EMPTY_SET.getClass(), new CollectionsEmptySetSerializer());
            this.kryo.register(Collections.singletonList("").getClass(), new CollectionsSingletonListSerializer());
            this.kryo.register(Collections.singleton("").getClass(), new CollectionsSingletonSetSerializer());
            this.kryo.register(Collections.singletonMap("", "").getClass(), new CollectionsSingletonMapSerializer());
            this.kryo.register(GregorianCalendar.class, new GregorianCalendarSerializer());
            this.kryo.register(InvocationHandler.class, new JdkProxySerializer());
            // register CGLibProxySerializer, works in combination with the appropriate action in handleUnregisteredClass (see below)
            this.kryo.register(CGLibProxySerializer.CGLibProxyMarker.class, new CGLibProxySerializer());
            UnmodifiableCollectionsSerializer.registerSerializers(this.kryo);
            SynchronizedCollectionsSerializer.registerSerializers(this.kryo);
        }
    }

	@Override
	public Object deepClone(Object obj, Type type) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] deepCloneMethodArgs(Method method, Object[] args) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] serialize(Object obj) throws Exception {
        try {
            KryoHolder kryoHolder = kryoThreadLocal.get();
            kryoHolder.output.clear();  //clear Output    -->每次调用的时候  重置
            kryoHolder.kryo.writeClassAndObject(kryoHolder.output, obj);
            return kryoHolder.output.toBytes();// 无法避免拷贝  ~~~
        } finally {
            obj = null;
        }
    }

	@Override
	public Object deserialize(byte[] bytes, Type returnType) throws Exception {
        try {
            KryoHolder kryoHolder = kryoThreadLocal.get();
            kryoHolder.input.setBuffer(bytes, 0, bytes.length);//call it ,and then use input object  ,discard any array
            return kryoHolder.kryo.readClassAndObject(kryoHolder.input);
        } finally {
            bytes = null;       //  for gc
        }
    }

}

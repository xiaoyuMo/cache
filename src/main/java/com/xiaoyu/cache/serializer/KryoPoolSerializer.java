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
import javolution.util.FastTable;

/**
 * Created by xiaoyu on 14-10-4.
 * PROJECT_NAME: springjrediscache
 * PACKAGE_NAME: com.xiaoyu.springjrediscache.Serializations
 */
public class KryoPoolSerializer implements ISerializer<Object> {

    /**
     * Kryo 的包装
     */
    private static class KryoHolder {
        private Kryo kryo;
        static final int BUFFER_SIZE = 1024;
        private Output output = new Output(BUFFER_SIZE, -1);     //reuse
        private Input input = new Input();

        KryoHolder(Kryo kryo) {
            this.kryo = kryo;

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

            //其他第三方
//        // joda datetime
//        kryo.register(DateTime.class, new JodaDateTimeSerializer());
//        // wicket
//        kryo.register(MiniMap.class, new MiniMapSerializer());
//        // guava ImmutableList
//        ImmutableListSerializer.registerSerializers(kryo);
        }
    }

    interface KryoPool {
        /**
         * get o kryo object
         *
         * @return
         */
        KryoHolder get();

        /**
         * return object
         *
         * @param kryo
         */
        void offer(KryoHolder kryo);
    }


    //基于kryo序列换方案

    /**
     * 由于kryo创建的代价相对较高 ，这里使用空间换时间
     * 对KryoHolder对象进行重用
     * KryoHolder会出现峰值，应该不会造成内存泄漏哦
     */
    public static class KryoPoolImpl implements KryoPool {
        /**
         * default is 1500
         * online server limit 3K
         */
        // private static int DEFAULT_MAX_KRYO_SIZE = 1500;

        /**
         * thread safe list
         */
        private final FastTable<KryoHolder> kryoFastTable = new FastTable<KryoHolder>().atomic();

        /**
         *
         */
        private KryoPoolImpl() {
        }

        /**
         * @return
         */
        public static KryoPool getInstance() {
            return Singleton.pool;
        }

        /**
         * get o KryoHolder object
         *
         * @return
         */
        @Override
        public KryoHolder get() {
            KryoHolder kryoHolder = kryoFastTable.pollFirst();       // Retrieves and removes the head of the queue represented by this table
            return kryoHolder == null ? creatInstnce() : kryoHolder;
        }

        /**
         * create a new kryo object to application use
         *
         * @return
         */
        public KryoHolder creatInstnce() {
            Kryo kryo = new Kryo();
            kryo.setReferences(false);//
            return new KryoHolder(kryo);
        }

        /**
         * return object
         * Inserts the specified element at the tail of this queue.
         *
         * @param kryoHolder
         */
        @Override
        public void offer(KryoHolder kryoHolder) {
            kryoFastTable.addLast(kryoHolder);
        }

        /**
         * creat a Singleton
         */
        private static class Singleton {
            private static final KryoPool pool = new KryoPoolImpl();
        }
    }

	@Override
	public Object deepClone(Object obj, Type type) throws Exception {
		return null;
	}

	@Override
	public Object[] deepCloneMethodArgs(Method method, Object[] args) throws Exception {
		return null;
	}

	@Override
	public byte[] serialize(Object obj) throws Exception {
        KryoHolder kryoHolder = null;
        if (obj == null) throw new Exception("obj can not be null");
        try {
            kryoHolder = KryoPoolImpl.getInstance().get();
            kryoHolder.output.clear();  //clear Output    -->每次调用的时候  重置
            kryoHolder.kryo.writeClassAndObject(kryoHolder.output, obj);
            return kryoHolder.output.toBytes();// 无法避免拷贝  ~~~
        } catch (Exception e) {
            throw new Exception("Serialize obj exception");
        } finally {
            KryoPoolImpl.getInstance().offer(kryoHolder);
            obj = null; //GC
        }
    }

	@Override
	public Object deserialize(byte[] bytes, Type returnType) throws Exception {
        KryoHolder kryoHolder = null;
        if (bytes == null) throw new Exception("bytes can not be null");
        try {
            kryoHolder = KryoPoolImpl.getInstance().get();
            kryoHolder.input.setBuffer(bytes, 0, bytes.length);//call it ,and then use input object  ,discard any array
            return kryoHolder.kryo.readClassAndObject(kryoHolder.input);
        } catch (Exception e) {
            throw new Exception("Deserialize bytes exception");
        } finally {
            KryoPoolImpl.getInstance().offer(kryoHolder);
            bytes = null;       //  for gc
        }
    }
}

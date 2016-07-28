package com.xiaoyu.cache.serializer;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.nustaq.serialization.FSTConfiguration;

/**
 * Created by xiaoyu on 14-10-4. PROJECT_NAME: springjrediscache PACKAGE_NAME:
 * com.xiaoyu.springjrediscache.Serializations 基于fst的序列化方案
 */
public class FstSerializer implements ISerializer<Object> {
    private static final FSTConfiguration conf = FSTConfiguration.getDefaultConfiguration();
	
    @Override
    public Object deepClone(Object obj, final Type type) throws Exception {
		return null;
    }

    @Override
    public Object[] deepCloneMethodArgs(Method method, Object[] args) throws Exception {
    	return null;
    }

//	@Override
//	public byte[] serialize(Object obj) throws Exception {
//		ByteArrayOutputStream byteArrayOutputStream = null;
//		FSTObjectOutput out = null;
//		try {
//			// stream closed in the finally
//			byteArrayOutputStream = new ByteArrayOutputStream(512);
//			out = new FSTObjectOutput(byteArrayOutputStream); // 32000 buffer
//																// size
//			out.writeObject(obj);
//			out.flush();
//			return byteArrayOutputStream.toByteArray();
//		} catch (IOException ex) {
//			throw new IOException(ex);
//		} finally {
//			try {
//				obj = null;
//				if (out != null) {
//					out.close(); // call flush byte buffer
//					out = null;
//				}
//				if (byteArrayOutputStream != null) {
//
//					byteArrayOutputStream.close();
//					byteArrayOutputStream = null;
//				}
//			} catch (IOException ex) {
//				// ignore close exception
//			}
//		}
//	}
//
//	@Override
//	public Object deserialize(byte[] bytes, Type returnType) throws Exception {
//
//		ByteArrayInputStream byteArrayInputStream = null;
//		FSTObjectInput in = null;
//		try {
//			// stream closed in the finally
//			byteArrayInputStream = new ByteArrayInputStream(bytes);
//			in = new FSTObjectInput(byteArrayInputStream);
//			return in.readObject();
//		} catch (ClassNotFoundException ex) {
//			throw new Exception(ex);
//		} catch (IOException ex) {
//			throw new IOException(ex);
//		} finally {
//			try {
//				bytes = null;
//				if (in != null) {
//					in.close();
//					in = null;
//				}
//				if (byteArrayInputStream != null) {
//					byteArrayInputStream.close();
//					byteArrayInputStream = null;
//				}
//			} catch (IOException ex) {
//				// ignore close exception
//			}
//		}
//	
//	}
    
    @Override
    public byte[] serialize(Object obj) throws Exception {
        byte barray[]=conf.asByteArray(obj);
        return barray;
    }

    @Override
    public Object deserialize(byte[] data, Type returnType) throws Exception {
        return conf.asObject(data);
    }
}

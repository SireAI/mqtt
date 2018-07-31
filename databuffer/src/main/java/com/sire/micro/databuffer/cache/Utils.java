package com.sire.micro.databuffer.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

class Utils {

     static Object bytes2Object(byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Object object = objectInputStream.readObject();
            return object;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

     static byte[] Object2Bytes(Serializable object) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static Object fill(Class componentType,String[] elements){
        int length = elements.length;
        Object array =null;
        if (!componentType.getName().equals(String.class.getCanonicalName())) {
             return elements;
        } else if (componentType == char.class) {
            char[] chars = new char[length];
            for (int i = 0; i < length; i++) {
                chars[i]=elements[i].charAt(0);
            }
            array = chars;
        } else if (componentType == int.class) {
            int[] ints = new int[length];
            for (int i = 0; i < length; i++) {
                ints[i]=Integer.valueOf(elements[i]);
            }
            array = ints;
        } else if (componentType == byte.class) {
            byte[] bytes = new byte[length];
            for (int i = 0; i < length; i++) {
                bytes[i]=Byte.valueOf(elements[i]);
            }
            array = bytes;
        } else if (componentType == boolean.class) {
            boolean[] booleans = new boolean[length];
            for (int i = 0; i < length; i++) {
                booleans[i]=Boolean.valueOf(elements[i]);
            }
            array = booleans;
        } else if (componentType == short.class) {
            short[] shorts = new short[length];
            for (int i = 0; i < length; i++) {
                shorts[i]=Short.valueOf(elements[i]);
            }
            array = shorts;
        } else if (componentType == long.class) {
            long[] longs = new long[length];
            for (int i = 0; i < length; i++) {
                longs[i]=Long.valueOf(elements[i]);
            }
            array = longs;
        } else if (componentType == float.class) {
            float[] floats = new float[length];
            for (int i = 0; i < length; i++) {
                floats[i]=Float.valueOf(elements[i]);
            }
            array = floats;
        } else if (componentType == double.class) {
            double[] doubles = new double[length];
            for (int i = 0; i < length; i++) {
                doubles[i]=Double.valueOf(elements[i]);
            }
            array = doubles;
        } else if (componentType == void.class) {
            throw new IllegalArgumentException("Can't allocate an array of void");
        }else {
            throw new AssertionError(componentType.getClass()+" not support");
        }
        return array;
    }

}

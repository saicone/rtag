package com.saicone.rtag.data;

import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class TagDataStream {

    private static final MethodHandle readNBT;
    private static final MethodHandle writeNBT;

    static {
        MethodHandle m1 = null, m2 = null;
        try {
            m1 = EasyLookup.staticMethod("NBTCompressedStreamTools", "a", "NBTTagCompound", ServerInstance.verNumber >= 16 ? DataInput.class : DataInputStream.class);
            m2 = EasyLookup.staticMethod("NBTCompressedStreamTools", "a", void.class, "NBTTagCompound", DataOutput.class);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        readNBT = m1;
        writeNBT = m2;
    }

    public static Object read(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            return read(in);
        }
    }

    public static Object read(InputStream input) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(input)))) {
            return read(in);
        }
    }

    public static Object read(DataInputStream input) throws IOException {
        try {
            return readNBT.invoke(input);
        } catch (IOException e) {
            throw e;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public static Object read(DataInput input) throws IOException {
        return read((DataInputStream) input);
    }

    public static void write(Object tag, File file) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            write(tag, out);
        }
    }

    public static void write(Object tag, OutputStream output) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(output)))) {
            write(tag, out);
        }
    }

    public static void write(Object tag, DataOutputStream output) throws IOException {
        write(tag, (DataOutput) output);
    }

    public static void write(Object tag, DataOutput output) throws IOException {
        try {
            writeNBT.invoke(tag, output);
        } catch (IOException e) {
            throw e;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

package com.saicone.rtag.stream;

import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Class to invoke NBTCompressedStreamTools methods across versions.
 *
 * @author Rubenicos
 */
public class TStreamTools {

    private static final MethodHandle readNBT;
    private static final MethodHandle writeNBT;

    static {
        MethodHandle method$read = null;
        MethodHandle method$write = null;
        try {
            method$read = EasyLookup.staticMethod("NBTCompressedStreamTools", "a", "NBTTagCompound", ServerInstance.verNumber >= 16 ? DataInput.class : DataInputStream.class);
            method$write = EasyLookup.staticMethod("NBTCompressedStreamTools", "a", void.class, "NBTTagCompound", DataOutput.class);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        readNBT = method$read;
        writeNBT = method$write;
    }

    TStreamTools() {
    }

    /**
     * Read NBTTagCompound from file.
     *
     * @param file File to read.
     * @return     A NBTTagCompound instance.
     * @throws IOException if root tag is not a compound.
     */
    public static Object read(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            return read(in);
        }
    }

    /**
     * Read NBTTagCompound from InputStream.
     *
     * @param input InputStream to read.
     * @return      A NBTTagCompound instance.
     * @throws IOException if root tag is not a compound.
     */
    public static Object read(InputStream input) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(input)))) {
            return read(in);
        }
    }

    /**
     * Read NBTTagCompound from DataInputStream.
     *
     * @param input DataInputStream to read.
     * @return      A NBTTagCompound instance.
     * @throws IOException if root tag is not a compound.
     */
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

    /**
     * Read NBTTagCompound from DataInput.
     *
     * @param input DataInput to read.
     * @return      A NBTTagCompound instance.
     * @throws IOException if root tag is not a compound.
     */
    public static Object read(DataInput input) throws IOException {
        return read((DataInputStream) input);
    }

    /**
     * Write NBTTagCompound to File.
     *
     * @param tag  Compound to write.
     * @param file File to write in.
     * @throws IOException if an I/O error occurs while writing.
     */
    public static void write(Object tag, File file) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            write(tag, out);
        }
    }

    /**
     * Write NBTTagCompound to OutputStream.
     *
     * @param tag  Compound to write.
     * @param output OutputStream to write in.
     * @throws IOException if an I/O error occurs while writing.
     */
    public static void write(Object tag, OutputStream output) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(output)))) {
            write(tag, out);
        }
    }

    /**
     * Write NBTTagCompound to DataOutputStream.
     *
     * @param tag  Compound to write.
     * @param output DataOutputStream to write in.
     * @throws IOException if an I/O error occurs while writing.
     */
    public static void write(Object tag, DataOutputStream output) throws IOException {
        write(tag, (DataOutput) output);
    }

    /**
     * Write NBTTagCompound to DataOutput.
     *
     * @param tag  Compound to write.
     * @param output DataOutput to write in.
     * @throws IOException if an I/O error occurs while writing.
     */
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

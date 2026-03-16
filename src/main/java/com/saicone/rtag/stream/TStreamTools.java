package com.saicone.rtag.stream;

import com.saicone.rtag.util.MC;
import com.saicone.rtag.util.reflect.Lookup;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Class to invoke NbtIo methods across versions.
 *
 * @author Rubenicos
 */
public class TStreamTools {

    // import
    private static final Lookup.AClass<?> Tag = Lookup.SERVER.importClass("net.minecraft.nbt.Tag");
    private static final Lookup.AClass<?> NbtIo = Lookup.SERVER.importClass("net.minecraft.nbt.NbtIo");
    private static final Lookup.AClass<?> NbtAccounter = Lookup.SERVER.importClass("net.minecraft.nbt.NbtAccounter");
    private static final Lookup.AClass<?> FastBufferedInputStream = Lookup.SERVER.importClass("net.minecraft.util.FastBufferedInputStream");

    // declare
    private static final boolean USE_FAST_STREAM = MC.version().isNewerThanOrEquals(MC.V_1_18);
    private static final boolean NETWORK_RECODE = MC.version().isNewerThanOrEquals(MC.V_1_20_2);

    private static final MethodHandle FastBufferedInputStream$new;
    static {
        if (USE_FAST_STREAM) {
            FastBufferedInputStream$new = FastBufferedInputStream.constructor(InputStream.class).handle();
        } else {
            FastBufferedInputStream$new = null;
        }
    }

    private static final MethodHandle NbtIo_readUnnamedTag;
    static {
        if (NETWORK_RECODE) {
            NbtIo_readUnnamedTag = NbtIo.method(Modifier.STATIC, Tag, "readUnnamedTag", DataInput.class, NbtAccounter).handle();
        } else {
            NbtIo_readUnnamedTag = NbtIo.method(Modifier.STATIC, Tag, "readUnnamedTag", DataInput.class, int.class, NbtAccounter).handle();
        }
    }
    private static final MethodHandle NbtIo_writeUnnamedTag = NbtIo.method(Modifier.STATIC, void.class, "writeUnnamedTag", Tag, DataOutput.class).handle();

    private static final MethodHandle NbtAccounter_unlimitedHeap;
    static {
        if (NETWORK_RECODE) {
            NbtAccounter_unlimitedHeap = NbtAccounter.method(Modifier.STATIC, NbtAccounter, "unlimitedHeap").handle();
        } else {
            NbtAccounter_unlimitedHeap = NbtAccounter.field(Modifier.STATIC, NbtAccounter, "UNLIMITED").getter();
        }
    }

    private static final Supplier<Object> READ_LIMITER;
    static {
        if (NETWORK_RECODE) {
            READ_LIMITER = () -> {
                try {
                    return NbtAccounter_unlimitedHeap.invoke();
                } catch (Throwable t) {
                    throw new RuntimeException("Cannot get NbtAccounter with unlimited heap");
                }
            };
        } else {
            Supplier<Object> supplier = () -> {
                throw new RuntimeException("Cannot get unlimited NbtAccounter");
            };
            try {
                final Object readLimiter = NbtAccounter_unlimitedHeap.invoke();
                supplier = () -> readLimiter;
            } catch (Throwable t) {
                t.printStackTrace();
            }
            READ_LIMITER = supplier;
        }
    }

    TStreamTools() {
    }

    /**
     * Get a NbtAccounter instance with no limit to read NBT data.
     *
     * @return A NbtAccounter instance.
     */
    public static Object getReadLimiter() {
        return READ_LIMITER.get();
    }

    /**
     * Get the required data input stream to read.<br>
     * This method will use the new enhanced FastBufferedInputStream
     * if the server version is 1.18 or higher, so this method checks if
     * the provided InputStream is GZIP formatted to parse it correctly.
     *
     * @param input The InputStream to parse.
     * @return      A parsed input stream.
     * @throws IOException if an I/O error occurs.
     */
    public static DataInputStream getDataInput(InputStream input) throws IOException {
        return getDataInput(input, isGzipFormat(input));
    }


    /**
     * Get the required data input stream to read.<br>
     * This method will use the new enhanced FastBufferedInputStream
     * if the server version is 1.18 or higher.
     *
     * @param input The InputStream to parse.
     * @param gzip  Set to true if the InputStream is on GZIP format.
     * @return      A parsed input stream.
     * @throws IOException if an I/O error occurs.
     */
    public static DataInputStream getDataInput(InputStream input, boolean gzip) throws IOException {
        final InputStream in = gzip ? new GZIPInputStream(input) : input;

        if (USE_FAST_STREAM) {
            try {
                return new DataInputStream((InputStream) FastBufferedInputStream$new.invoke(in));
            } catch (Throwable ignored) { }
        }

        return new DataInputStream(new BufferedInputStream(in));
    }

    /**
     * Check if the provided file is a GZIP file.
     *
     * @param file The file to check.
     * @return     true if the file is GZIP formatted, false otherwise.
     * @throws IOException if an I/O error occurs.
     */
    public static boolean isGzipFormat(File file) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            int ID1 = raf.read();
            if (ID1 <= -1 || ID1 > 255) {
                return false;
            }
            int ID2 = raf.read();
            if (ID2 <= -1 || ID2 > 255) {
                return false;
            }
            return isGzipHeader(ID1, ID2);
        }
    }

    /**
     * Check if the provided InputStream is a GZIP formatted input.
     *
     * @param input The InputStream to check.
     * @return      true if the input stream is GZIP formatted, false otherwise.
     * @throws IOException if an I/O error occurs.
     */
    public static boolean isGzipFormat(InputStream input) throws IOException {
        final InputStream in;
        if (input.markSupported()) {
            in = input;
        } else {
            in = new BufferedInputStream(input);
        }
        in.mark(2);

        int ID1 = in.read();
        if (ID1 <= -1 || ID1 > 255) {
            in.reset();
            return false;
        }
        int ID2 = in.read();
        if (ID2 <= -1 || ID2 > 255) {
            in.reset();
            return false;
        }
        in.reset();

        return isGzipHeader(ID1, ID2);
    }

    /**
     * Check if the provided byte array has GZIP header.
     *
     * @param bytes The byte array to check.
     * @return      true if it contains GZIP header.
     */
    public static boolean isGzipHeader(byte[] bytes) {
        return isGzipHeader(bytes[0] & 0xff, bytes[1] & 0xff);
    }

    /**
     * Check if the provided ID1 and ID2 are the same has GZIP magic.
     *
     * @param ID1 The first ID.
     * @param ID2 The second ID.
     * @return    true if the IDs are the same has GZIP magic.
     */
    public static boolean isGzipHeader(int ID1, int ID2) {
        return ((ID2 << 8) | ID1) == GZIPInputStream.GZIP_MAGIC;
    }

    /**
     * Check if the provided byte array has NBT header.
     *
     * @param bytes The byte array to check.
     * @return      true of it contains NBT header.
     */
    public static boolean isNbtHeader(byte[] bytes) {
        return bytes[0] >= 1 && bytes[0] <= 12 && bytes[1] == 0 && bytes[2] == 0;
    }

    /**
     * Read Tag from file.
     *
     * @param file File to read.
     * @return     A Tag instance.
     * @throws IOException if root object is not a nbt tag.
     */
    public static Object read(File file) throws IOException {
        try (DataInputStream in = getDataInput(new FileInputStream(file), isGzipFormat(file))) {
            return read((DataInput) in);
        }
    }

    /**
     * Read Tag from byte array.
     *
     * @param bytes The byte array to read.
     * @return      A Tag instance.
     * @throws IOException if root object is not a nbt tag.
     */
    public static Object read(byte[] bytes) throws IOException {
        try (DataInputStream in = getDataInput(new ByteArrayInputStream(bytes), bytes.length >= 2 && isGzipHeader(bytes))) {
            return read((DataInput) in);
        }
    }

    /**
     * Read Tag from InputStream.
     *
     * @param input InputStream to read.
     * @return      A Tag instance.
     * @throws IOException if root object is not a nbt tag.
     */
    public static Object read(InputStream input) throws IOException {
        try (DataInputStream in = getDataInput(input)) {
            return read((DataInput) in);
        }
    }

    /**
     * Read Tag from DataInputStream.
     *
     * @param input DataInputStream to read.
     * @return      A Tag instance.
     * @throws IOException if root object is not a nbt tag.
     */
    public static Object read(DataInputStream input) throws IOException {
        return read((DataInput) input);
    }

    /**
     * Read Tag from DataInput.
     *
     * @param input DataInput to read.
     * @return      A Tag instance.
     * @throws IOException if root object is not a nbt tag.
     */
    public static Object read(DataInput input) throws IOException {
        try {
            if (NETWORK_RECODE) {
                return NbtIo_readUnnamedTag.invoke(input, getReadLimiter());
            } else {
                return NbtIo_readUnnamedTag.invoke(input, 0, getReadLimiter());
            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Write Tag to File.
     *
     * @param tag  The tag to write.
     * @param file File to write in.
     * @throws IOException if an I/O error occurs while writing.
     */
    public static void write(Object tag, File file) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            write(tag, out);
        }
    }

    /**
     * Write Tag to OutputStream.
     *
     * @param tag  The tag to write.
     * @param output OutputStream to write in.
     * @throws IOException if an I/O error occurs while writing.
     */
    public static void write(Object tag, OutputStream output) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(output)))) {
            write(tag, (DataOutput) out);
        }
    }

    /**
     * Write Tag to DataOutputStream.
     *
     * @param tag    The tag to write.
     * @param output DataOutputStream to write in.
     * @throws IOException if an I/O error occurs while writing.
     */
    public static void write(Object tag, DataOutputStream output) throws IOException {
        write(tag, (DataOutput) output);
    }

    /**
     * Write Tag to DataOutput.
     *
     * @param tag    The tag to write.
     * @param output DataOutput to write in.
     * @throws IOException if an I/O error occurs while writing.
     */
    public static void write(Object tag, DataOutput output) throws IOException {
        try {
            NbtIo_writeUnnamedTag.invoke(tag, output);
        } catch (IOException e) {
            throw e;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

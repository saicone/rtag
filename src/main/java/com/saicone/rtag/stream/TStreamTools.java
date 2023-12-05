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

    private static final Object READ_LIMITER;

    private static final boolean USE_FAST_STREAM = ServerInstance.verNumber >= 18;
    private static final MethodHandle newFastInputStream;

    private static final MethodHandle readNBT;
    private static final MethodHandle writeNBT;

    static {
        // Constructors
        MethodHandle new$FastInputStream = null;
        // Methods
        MethodHandle method$read = null;
        MethodHandle method$write = null;
        // Getters
        MethodHandle get$unlimited = null;
        try {
            EasyLookup.addNMSClass("nbt.NBTCompressedStreamTools", "NbtIo");
            EasyLookup.addNMSClass("nbt.NBTReadLimiter", "NbtAccounter");

            // Old names
            String read = "a";
            String write = "a";
            String unlimited = "a";
            // New names
            if (ServerInstance.isMojangMapped) {
                read = "readUnnamedTag";
                write = "writeUnnamedTag";
                unlimited = ServerInstance.fullVersion >= 12002 ? "unlimitedHeap" : "UNLIMITED";
            } else if (ServerInstance.fullVersion >= 12002) {
                read = "c";
                write = "b";
            }

            if (USE_FAST_STREAM) {
                EasyLookup.addNMSClass("util.FastBufferedInputStream");
                new$FastInputStream = EasyLookup.constructor("FastBufferedInputStream", InputStream.class);
            }

            if (ServerInstance.fullVersion >= 12002) {
                // Private method
                // Note: The "unused" integer was removed, and also was added a new method (DataInput, NBTReadLimiter, byte)
                //       to specify the id of NBT you're reading (probably add it here)
                method$read = EasyLookup.staticMethod("NBTCompressedStreamTools", read, "NBTBase", DataInput.class, "NBTReadLimiter");
            } else {
                // Private method
                method$read = EasyLookup.staticMethod("NBTCompressedStreamTools", read, "NBTBase", DataInput.class, int.class, "NBTReadLimiter");
            }
            // (1.8 - 1.17) private method
            // (1.20.2) Note: New method to write NBT without adding an empty String after write NBT id, only used to send serialized packets
            // (1.20.3) Note: New method to write NBT using a DelegateDataOutput that writes empty String if any error occurs
            method$write = EasyLookup.staticMethod("NBTCompressedStreamTools", write, void.class, "NBTBase", DataOutput.class);

            if (ServerInstance.fullVersion >= 12002) {
                get$unlimited = EasyLookup.staticMethod("NBTReadLimiter", unlimited, "NBTReadLimiter");
            } else {
                get$unlimited = EasyLookup.staticGetter("NBTReadLimiter", unlimited, "NBTReadLimiter");
            }
        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }

        newFastInputStream = new$FastInputStream;
        readNBT = method$read;
        writeNBT = method$write;

        Object readLimiter = null;
        try {
            if (get$unlimited != null) {
                readLimiter = get$unlimited.invoke();
            } else {
                // Fallback instance constructor
                if (ServerInstance.fullVersion >= 12002) {
                    readLimiter = EasyLookup.classById("NBTReadLimiter").getDeclaredConstructor(long.class, int.class).newInstance(Long.MAX_VALUE, 512);
                } else {
                    readLimiter = EasyLookup.classById("NBTReadLimiter").getDeclaredConstructor(long.class).newInstance(Long.MAX_VALUE);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        READ_LIMITER = readLimiter;
    }

    TStreamTools() {
    }

    /**
     * Get a NBTReadLimiter instance with no limit to read NBT data.
     *
     * @return A NBTReadLimiter instance.
     */
    public static Object getReadLimiter() {
        return READ_LIMITER;
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
                return new DataInputStream((InputStream) newFastInputStream.invoke(in));
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
            return false;
        }
        int ID2 = in.read();
        if (ID2 <= -1 || ID2 > 255) {
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
     * Read NBTBase from file.
     *
     * @param file File to read.
     * @return     A NBTBase instance.
     * @throws IOException if root object is not a nbt tag.
     */
    public static Object read(File file) throws IOException {
        try (DataInputStream in = getDataInput(new FileInputStream(file), isGzipFormat(file))) {
            return read((DataInput) in);
        }
    }

    /**
     * Read NBTBase from byte array.
     *
     * @param bytes The byte array to read.
     * @return      A NBTBase instance.
     * @throws IOException if root object is not a nbt tag.
     */
    public static Object read(byte[] bytes) throws IOException {
        try (DataInputStream in = getDataInput(new ByteArrayInputStream(bytes), bytes.length >= 2 && isGzipHeader(bytes))) {
            return read((DataInput) in);
        }
    }

    /**
     * Read NBTBase from InputStream.
     *
     * @param input InputStream to read.
     * @return      A NBTBase instance.
     * @throws IOException if root object is not a nbt tag.
     */
    public static Object read(InputStream input) throws IOException {
        try (DataInputStream in = getDataInput(input)) {
            return read((DataInput) in);
        }
    }

    /**
     * Read NBTBase from DataInputStream.
     *
     * @param input DataInputStream to read.
     * @return      A NBTBase instance.
     * @throws IOException if root object is not a nbt tag.
     */
    public static Object read(DataInputStream input) throws IOException {
        return read((DataInput) input);
    }

    /**
     * Read NBTBase from DataInput.
     *
     * @param input DataInput to read.
     * @return      A NBTBase instance.
     * @throws IOException if root object is not a nbt tag.
     */
    public static Object read(DataInput input) throws IOException {
        try {
            if (ServerInstance.fullVersion >= 12002) {
                return readNBT.invoke(input, getReadLimiter());
            } else {
                return readNBT.invoke(input, 0, getReadLimiter());
            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Write NBTBase to File.
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
     * Write NBTBase to OutputStream.
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
     * Write NBTBase to DataOutputStream.
     *
     * @param tag    The tag to write.
     * @param output DataOutputStream to write in.
     * @throws IOException if an I/O error occurs while writing.
     */
    public static void write(Object tag, DataOutputStream output) throws IOException {
        write(tag, (DataOutput) output);
    }

    /**
     * Write NBTBase to DataOutput.
     *
     * @param tag    The tag to write.
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

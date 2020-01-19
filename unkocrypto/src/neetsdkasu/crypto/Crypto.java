package neetsdkasu.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;
import neetsdkasu.util.zip.Checksum;

public final class Crypto
{
    public static final int MIN_SIZE = 32;
    public static final int MAX_SIZE = 1024;
    public static final int META_SIZE = (/* Integer.SIZE */ 32 / 8) + (/* Long.SIZE */ 64 / 8); // SIZE is nothing CLDC/MIDP

    private Crypto() {
        // throw new UnsupportedOperationException();
        throw new RuntimeException("UnsupportedOperationException");
    }

    private static void validateParams(int size, Checksum checksum, Random rand, byte[] src)
    {
        if (size < MIN_SIZE || MAX_SIZE < size)
        {
            throw new IllegalArgumentException(
                "OUT OF RANGE size(" + size + "): "
                + MIN_SIZE + " <= size <= " + MAX_SIZE);
        }
        if (checksum == null)
        {
            throw new IllegalArgumentException("checksum is null");
        }
        if (rand == null)
        {
            throw new IllegalArgumentException("rand is null");
        }
        if (src == null)
        {
            throw new IllegalArgumentException("src is null");
        }
    }

    public static byte[] decrypt(int size, Checksum checksum, Random rand, byte[] src)
    {
        validateParams(size, checksum, rand, src);
        return doDecrypt(size, checksum, rand, src, 0);
    }

    public static byte[] decrypt(int size, Checksum checksum, Random rand, byte[] src, int off)
    {
        validateParams(size, checksum, rand, src);
        return doDecrypt(size, checksum, rand, src, off);
    }

    public static byte[] encrypt(int size, Checksum checksum, Random rand, byte[] src)
    {
        validateParams(size, checksum, rand, src);
        return doEncrypt(size, checksum, rand, src, 0, src.length);
    }

    public static byte[] encrypt(int size, Checksum checksum, Random rand, byte[] src, int off, int len)
    {
        validateParams(size, checksum, rand, src);
        return doEncrypt(size, checksum, rand, src, off, len);
    }

    private static byte[] doDecrypt(int size, Checksum checksum, Random rand, byte[] src, int off)
    {
        try
        {
            if (off < 0 || src.length <= off)
            {
                throw new IllegalArgumentException(
                    "OUT OF RANGE off(" + off + "): 0 <= off < src.length("
                    + src.length + ") - size(" + size + ")");
            }
            if (src.length < off + size)
            {
                throw new IllegalArgumentException(
                    "FEW SRC LENGTH (" + src.length + "): off(" + off + ") + size("
                    + size + ") <= src.length");
            }
            int[] mask = new int[size];
            int[] indexes = new int[size];
            for (int i = 0; i < size; i++)
            {
                mask[i] = rand.nextInt(0x100);
                indexes[i] = i;
            }
            for (int i = 0; i < indexes.length; i++)
            {
                int j = rand.nextInt(indexes.length - i) + i;
                int tmp = indexes[i];
                indexes[i] = indexes[j];
                indexes[j] = tmp;
            }
            byte[] data = new byte[size];
            for (int i = 0; i < indexes.length; i++)
            {
                int j = indexes[i];
                int b = 0xFF & src[i];
                b ^= 0xFF & mask[j];
                data[j] = (byte)b;
            }
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            int datasize = size - META_SIZE;
            dis.skip(datasize);
            int count = dis.readInt();
            long code = dis.readLong();
            dis.close();
            if (count <= 0 || datasize < count)
            {
                // Invalid
                throw new CryptoException(CryptoException.TYPE_INVALID_COUNT);
            }
            checksum.reset();
            for (int i = 0; i < datasize; i++)
            {
                if (i < count)
                {
                    checksum.update(0xFF & (int)data[i]);
                }
                else if (data[i] != 0)
                {
                    throw new CryptoException(CryptoException.TYPE_INVALID_DATA);
                }
            }
            if (code != checksum.getValue())
            {
                throw new CryptoException(CryptoException.TYPE_INVALID_CHECKSUM);
            }
            byte[] ret = new byte[count];
            System.arraycopy(data, 0, ret, 0, ret.length);
            return ret;
        }
        catch (IOException ex)
        {
            // Unreachable
            throw new CryptoException(ex);
        }
    }


    private static byte[] doEncrypt(int size, Checksum checksum, Random rand, byte[] src, int off, int len)
    {
        try
        {
            if (off < 0 || src.length <= off)
            {
                throw new IllegalArgumentException(
                    "OUT OF RANGE off(" + off + "): 0 <= off < src.length("
                        + src.length + ") - len");
            }
            if (len <= 0 || src.length < off + len || size - META_SIZE < len)
            {
                throw new IllegalArgumentException(
                    "OUT OF RANGE len(" + len + "): 0 < len <= min(src.length("
                        + src.length + ") - off(" + off + "), size("
                        + size + ") - " + META_SIZE + ")");
            }
            int datasize = size - META_SIZE;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
            DataOutputStream dos = new DataOutputStream(baos);
            checksum.reset();
            int count = 0;
            for (; count < datasize; count++)
            {
                if (count >= len)
                {
                    break;
                }
                int b = 0xFF & (int)src[off + count];
                checksum.update(b);
                b ^= 0xFF & rand.nextInt(0x100);
                dos.writeByte(b);
            }
            for (int i = count; i < datasize; i++)
            {
                int b = 0xFF & rand.nextInt(0x100);
                dos.writeByte(b);
            }
            dos.writeInt(count);
            dos.writeLong(checksum.getValue());
            byte[] memory = baos.toByteArray();
            dos.close();
            for (int i = datasize; i < memory.length; i++)
            {
                int b = 0xFF & (int)memory[i];
                b ^= 0xFF & rand.nextInt(0x100);
                memory[i] = (byte)b;
            }
            for (int i = 0; i < memory.length; i++)
            {
                int j = rand.nextInt(memory.length - i) + i;
                byte tmp = memory[i];
                memory[i] = memory[j];
                memory[j] = tmp;
            }
            return memory;
        }
        catch (IOException ex)
        {
            // Unreachable
            throw new CryptoException(ex);
        }
    }

}
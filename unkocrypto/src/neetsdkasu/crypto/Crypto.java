package neetsdkasu.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.zip.Checksum;

public final class Crypto
{
    public static final int MIN_BLOCKSIZE = 32;
    public static final int MAX_BLOCKSIZE = 1024;
    public static final int META_SIZE = (Integer.SIZE / 8) + (Long.SIZE / 8);
    private static final int BYTE = 0x100; // 256
    private static final int MASK = 0x0FF; // 255

    private Crypto() {
        throw new UnsupportedOperationException();
    }

    private static void validateParams(int blockSize, Checksum checksum, Random rand, byte[] src)
    {
        if (blockSize < MIN_BLOCKSIZE || MAX_BLOCKSIZE < blockSize)
        {
            throw new IllegalArgumentException(
                "OUT OF RANGE blockSize(" + blockSize + "): "
                + MIN_BLOCKSIZE + " <= blockSize <= " + MAX_BLOCKSIZE);
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

    public static byte[] decrypt(int blockSize, Checksum checksum, Random rand, byte[] src)
    {
        validateParams(blockSize, checksum, rand, src);
        return doDecrypt(blockSize, checksum, rand, src, 0, src.length);
    }

    public static byte[] decrypt(int blockSize, Checksum checksum, Random rand, byte[] src, int fromIndex, int toIndex)
    {
        validateParams(blockSize, checksum, rand, src);
        return doDecrypt(blockSize, checksum, rand, src, fromIndex, toIndex);
    }

    public static byte[] encrypt(int blockSize, Checksum checksum, Random rand, byte[] src)
    {
        validateParams(blockSize, checksum, rand, src);
        return doEncrypt(blockSize, checksum, rand, src, 0, src.length);
    }

    public static byte[] encrypt(int blockSize, Checksum checksum, Random rand, byte[] src, int fromIndex, int toIndex)
    {
        validateParams(blockSize, checksum, rand, src);
        return doEncrypt(blockSize, checksum, rand, src, fromIndex, toIndex);
    }

    private static byte[] doDecrypt(int blockSize, Checksum checksum, Random rand, byte[] src, int fromIndex, int toIndex)
    {
        try
        {
            if (fromIndex < 0 || src.length <= fromIndex)
            {
                throw new IllegalArgumentException(
                    "OUT OF RANGE fromIndex(" + fromIndex + "): 0 <= fromIndex < src.length(" + src.length + ")");
            }
            if (toIndex <= fromIndex || src.length < toIndex)
            {
                throw new IllegalArgumentException(
                    "OUT OF RANGE toIndex(" + toIndex + "): fromIndex(" + fromIndex + ") < toIndex <= src.length(" + src.length + ")");
            }
            int len = toIndex - fromIndex;
            if (len % blockSize != 0) {
                throw new IllegalArgumentException(
                    "LENGTH(toIndex(" + toIndex + ") - fromIndex(" + fromIndex + ")) MUST HAVE DIVISOR blockSize(" + blockSize + ")");
            }
            int blockCount = len / blockSize;
            int dataSize = blockSize - META_SIZE;
            ByteArrayOutputStream ret = new ByteArrayOutputStream(blockCount * dataSize);
            int[] mask = new int[blockSize];
            int[] indexes = new int[blockSize];
            byte[] data = new byte[blockSize];
            while (blockCount-- > 0) {
                for (int i = 0; i < blockSize; i++)
                {
                    mask[i] = rand.nextInt(BYTE);
                    indexes[i] = i;
                }
                for (int i = 0; i < blockSize; i++)
                {
                    int j = rand.nextInt(blockSize - i) + i;
                    int tmp = indexes[i];
                    indexes[i] = indexes[j];
                    indexes[j] = tmp;
                }
                for (int i = 0; i < blockSize; i++)
                {
                    int j = indexes[i];
                    int b = MASK & src[fromIndex];
                    b ^= MASK & mask[j];
                    data[j] = (byte)b;
                    fromIndex++;
                }
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
                dis.skip(dataSize);
                int count = dis.readInt();
                long code = dis.readLong();
                dis.close();
                if (count <= 0 || dataSize < count)
                {
                    // Invalid
                    throw new CryptoException(CryptoException.TYPE_INVALID_COUNT);
                }
                checksum.reset();
                for (int i = 0; i < dataSize; i++)
                {
                    if (i < count)
                    {
                        ret.write(data[i]);
                        checksum.update(MASK & (int)data[i]);
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
            }
            return ret.toByteArray();
        }
        catch (IOException ex)
        {
            // Unreachable
            throw new CryptoException(ex);
        }
    }


    private static byte[] doEncrypt(int blockSize, Checksum checksum, Random rand, byte[] src, int fromIndex, int toIndex)
    {
        try
        {
            if (fromIndex < 0 || src.length <= fromIndex)
            {
                throw new IllegalArgumentException(
                    "OUT OF RANGE fromIndex(" + fromIndex + "): 0 <= fromIndex < src.length(" + src.length + ")");
            }
            if (toIndex < fromIndex || src.length < toIndex)
            {
                throw new IllegalArgumentException(
                    "OUT OF RANGE toIndex(" + toIndex + "): fromIndex(" + fromIndex + ") <= toIndex <= src.length(" + src.length + ")");
            }
            int dataSize = blockSize - META_SIZE;
            int len = toIndex - fromIndex;
            int blockCount = (int)(((long)len + (long)dataSize - 1L) / (long)dataSize);
            long totalSize = (long)blockCount * (long)blockSize;
            if (totalSize > (long)java.lang.Integer.MAX_VALUE) {
                throw new CryptoException(CryptoException.TYPE_INVALID_DATASIZE);
            }
            byte[] ret = new byte[(int)totalSize];
            int pos = 0;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(blockSize);
            DataOutputStream dos = new DataOutputStream(baos);
            do {
                baos.reset();
                checksum.reset();
                int count = 0;
                for (; count < dataSize; count++)
                {
                    if (fromIndex + count >= toIndex)
                    {
                        break;
                    }
                    int b = MASK & (int)src[fromIndex];
                    checksum.update(b);
                    b ^= MASK & rand.nextInt(BYTE);
                    dos.writeByte(b);
                    fromIndex++;
                }
                for (int i = count; i < dataSize; i++)
                {
                    int b = MASK & rand.nextInt(BYTE);
                    dos.writeByte(b);
                }
                dos.writeInt(count);
                dos.writeLong(checksum.getValue());
                byte[] memory = baos.toByteArray();
                dos.close();
                for (int i = dataSize; i < memory.length; i++)
                {
                    int b = MASK & (int)memory[i];
                    b ^= MASK & rand.nextInt(BYTE);
                    memory[i] = (byte)b;
                }
                for (int i = 0; i < memory.length; i++)
                {
                    int j = rand.nextInt(memory.length - i) + i;
                    byte tmp = memory[i];
                    memory[i] = memory[j];
                    memory[j] = tmp;
                }
                System.arraycopy(memory, 0, ret, pos, memory.length);
                pos += memory.length;
            } while (fromIndex < toIndex);
            return ret;
        }
        catch (IOException ex)
        {
            // Unreachable
            throw new CryptoException(ex);
        }
    }

}
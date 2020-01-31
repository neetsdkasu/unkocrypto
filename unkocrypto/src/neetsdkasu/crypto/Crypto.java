package neetsdkasu.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.zip.Checksum;

public final class Crypto
{
    public static final int MIN_BLOCKSIZE = 32;
    public static final int MAX_BLOCKSIZE = 1024;
    public static final int META_SIZE = (Integer.SIZE / 8) + (Long.SIZE / 8);
    private static final int BYTE = 0x100; // 256
    private static final int MASK = 0x0FF; // 255

    private Crypto()
    {
        throw new UnsupportedOperationException();
    }

    private static void validateParams(int blockSize, Checksum checksum, Random rand, InputStream src, OutputStream dst)
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
        if (dst == null)
        {
            throw new IllegalArgumentException("dst is null");
        }
    }

    private static int nextInt(Random rand, int bound)
    {
        if ((bound & -bound) == bound)
        {
            return (int)(((long)bound * (long)(rand.nextInt() >>> 1)) >> 31);
        }
        int bits, val;
        do
        {
            bits = rand.nextInt() >>> 1;
            val = bits % bound;
        } while (bits - val + (bound - 1) < 0);
        return val;
    }

    public static int decrypt(int blockSize, Checksum checksum, Random rand, InputStream src, OutputStream dst) throws IOException
    {
        validateParams(blockSize, checksum, rand, src, dst);
        int dataSize = blockSize - META_SIZE;
        int[] mask = new int[blockSize];
        int[] indexes = new int[blockSize];
        byte[] data = new byte[blockSize];
        int len = 0;
        int onebyte = src.read();
        while (onebyte >= 0)
        {
            for (int i = 0; i < blockSize; i++)
            {
                mask[i] = nextInt(rand, BYTE);
                indexes[i] = i;
            }
            for (int i = 0; i < blockSize; i++)
            {
                int j = nextInt(rand, blockSize - i) + i;
                int tmp = indexes[i];
                indexes[i] = indexes[j];
                indexes[j] = tmp;
            }
            for (int i = 0; i < blockSize; i++)
            {
                int j = indexes[i];
                int b = MASK & onebyte;
                b ^= MASK & mask[j];
                data[j] = (byte)b;
                onebyte = src.read();
            }
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            dis.skip(dataSize);
            int count = dis.readInt();
            long code = dis.readLong();
            dis.close();
            if (count < 0 || (count == 0 && onebyte >= 0) || dataSize < count)
            {
                // Invalid
                throw new CryptoException(CryptoException.TYPE_INVALID_COUNT);
            }
            len += count;
            checksum.reset();
            for (int i = 0; i < dataSize; i++)
            {
                if (i < count)
                {
                    dst.write(data[i]);
                    checksum.update(MASK & (int)data[i]);
                }
                else if (data[i] != 0) // これダメっしょ(data[i]==0のデータが誤復元されてる可能性を検出できてない)
                {
                    throw new CryptoException(CryptoException.TYPE_INVALID_DATA);
                }
            }
            if (code != checksum.getValue())
            {
                throw new CryptoException(CryptoException.TYPE_INVALID_CHECKSUM);
            }
        }
        return len;
    }

    public static int encrypt(int blockSize, Checksum checksum, Random rand, InputStream src, OutputStream dst) throws IOException
    {
        validateParams(blockSize, checksum, rand, src, dst);
        int dataSize = blockSize - META_SIZE;
        int len = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(blockSize);
        DataOutputStream dos = new DataOutputStream(baos);
        int onebyte = src.read();
        do
        {
            len += blockSize;
            baos.reset();
            checksum.reset();
            int count = 0;
            for (; count < dataSize; count++)
            {
                if (onebyte < 0)
                {
                    break;
                }
                int b = MASK & onebyte;
                checksum.update(b);
                b ^= MASK & nextInt(rand, BYTE);
                dos.writeByte(b);
                onebyte = src.read();
            }
            for (int i = count; i < dataSize; i++)
            {
                int b = MASK & nextInt(rand, BYTE);
                dos.writeByte(b);
            }
            dos.writeInt(count);
            dos.writeLong(checksum.getValue());
            byte[] memory = baos.toByteArray();
            dos.close();
            for (int i = dataSize; i < memory.length; i++)
            {
                int b = MASK & (int)memory[i];
                b ^= MASK & nextInt(rand, BYTE);
                memory[i] = (byte)b;
            }
            for (int i = 0; i < memory.length; i++)
            {
                int j = nextInt(rand, memory.length - i) + i;
                byte tmp = memory[i];
                memory[i] = memory[j];
                memory[j] = tmp;
            }
            dst.write(memory);
        } while (onebyte >= 0);
        return len;
    }
}
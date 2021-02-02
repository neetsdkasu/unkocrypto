package idpwmemo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Checksum;
import java.util.zip.CRC32;

import mt19937ar.MTRandom;
import neetsdkasu.crypto.Crypto;
import neetsdkasu.crypto.CryptoException;

final class Cryptor
{
    static final int VERSION = 2;
    static final int MAX_BLOCKSIZE = Math.min(1024, Crypto.MAX_BLOCKSIZE);
    static final String CHARSET = "UTF-8";

    static byte[] getBytes(String s) throws IOException
    {
        return s.getBytes(CHARSET);
    }

    static final Cryptor instance = new Cryptor();

    private final MTRandom rand = new MTRandom();
    private final Checksum cs = new CRC32();

    Cryptor() {}

    private static long[] genSeedV1(int size)
    {
        long[] seed = new long[size];
        seed[0] = 0x98765432L;
        seed[1] = 0xF1E2D3C4L;
        for (int i = 2; i < seed.length; i++)
        {
            seed[i] = seed[i - 2] ^ (seed[i - 1] >> ((i - 1) & 0xF)) ^ (seed[i - 1] << ((i + i + 1) & 0xF));
            seed[i] &= 0xFFFFFFFFL;
        }
        return seed;
    }

    private static long[] genSeedV1(byte[] password)
    {
        if (password == null || password.length == 0)
        {
            return genSeedV1(23);
        }
        long[] seed = genSeedV1(password.length + 13);
        int p = 0;
        for (int i = 0; i < seed.length; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                seed[i] |= (int)password[p] << (((j + i * i) & 3) << 3);
                p++;
                if (p >= password.length)
                {
                    p = 0;
                }
            }
        }
        return seed;
    }

    private static int encryptBlockSize(int srclen)
    {
        int size = Math.max(Crypto.MIN_BLOCKSIZE, srclen + Crypto.META_SIZE);
        if (size <= MAX_BLOCKSIZE)
        {
            return size;
        }
        size = Crypto.MIN_BLOCKSIZE;
        int blockCount = (srclen + (size - Crypto.META_SIZE) - 1) / (size - Crypto.META_SIZE);
        int totalSize = size * blockCount;
        for (int sz = Crypto.MIN_BLOCKSIZE + 1; sz <= MAX_BLOCKSIZE; sz++)
        {
            blockCount = (srclen + (sz - Crypto.META_SIZE) - 1) / (sz - Crypto.META_SIZE);
            if (sz * blockCount < totalSize)
            {
                size = sz;
                totalSize = sz * blockCount;
            }
        }
        return size;
    }

    byte[] decryptV1(String password, byte[] src) throws IOException
    {
        return decryptV1(getBytes(password), src);
    }

    synchronized byte[] decryptV1(byte[] password, byte[] src) throws IOException
    {
        ByteArrayInputStream in = new ByteArrayInputStream(src);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long[] seed = genSeedV1(password);
        for (int size = Math.min(src.length, MAX_BLOCKSIZE); size >= Crypto.MIN_BLOCKSIZE; size--)
        {
            if (src.length % size != 0)
            {
                continue;
            }
            in.reset();
            out.reset();
            rand.setSeed(seed);
            try
            {
                Crypto.decrypt(size, cs, rand, in, out);
                seed = null;
                byte[] ret = out.toByteArray();
                in.close();
                out.close();
                return ret;
            }
            catch (CryptoException ex)
            {
                // continue;
            }
        }
        seed = null;
        in.close();
        out.close();
        return null;
    }

    byte[] encryptV1(String password, byte[] src) throws IOException
    {
        return encryptV1(getBytes(password), src);
    }

    synchronized byte[] encryptV1(byte[] password, byte[] src) throws IOException
    {
        int blockSize = encryptBlockSize(src.length);
        ByteArrayInputStream in = new ByteArrayInputStream(src);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        rand.setSeed(genSeedV1(password));
        Crypto.encrypt(blockSize, cs, rand, in, out);
        byte[] ret = out.toByteArray();
        in.close();
        out.close();
        return ret;
    }

    byte[] decryptRepeatV1(int times, String password, byte[] src) throws IOException
    {
        return decryptRepeatV1(times, getBytes(password), src);
    }

    byte[] decryptRepeatV1(int times, byte[] password, byte[] src) throws IOException
    {
        for (int i = 0; i < times; i++)
        {
            src = decryptV1(password, src);
            if (src == null)
            {
                return null;
            }
        }
        return src;
    }

    byte[] encryptRepeatV1(int times, String password, byte[] src) throws IOException
    {
        return encryptRepeatV1(times, getBytes(password), src);
    }

    byte[] encryptRepeatV1(int times, byte[] password, byte[] src) throws IOException
    {
        for (int i = 0; i < times; i++)
        {
            src = encryptV1(password, src);
        }
        return src;
    }

    private static long[] genSeedV2(int size)
    {
        long[] seed = new long[size];
        seed[0] = 0x98765432L;
        seed[1] = 0xF1E2D3C4L;
        for (int i = 2; i < seed.length; i++)
        {
            seed[i] = seed[i - 2] ^ (seed[i - 1] >>> ((i - 1) & 0xF)) ^ (seed[i - 1] << ((i + i + 1) & 0xF));
            seed[i] &= 0xFFFFFFFFL;
        }
        return seed;
    }

    private static long[] genSeedV2(byte[] password)
    {
        if (password == null || password.length == 0)
        {
            return genSeedV2(37);
        }
        long[] seed = genSeedV2(password.length + 29);
        int p = 0;
        for (int i = 0; i < seed.length; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                seed[i] ^= (0xFF & (int)password[p]) << (((j + i * i) & 3) << 3);
                p++;
                if (p >= password.length)
                {
                    p = 0;
                }
            }
        }
        return seed;
    }

    static int checkSrcType(byte[] src)
    {
        int srcType = 0;
        int version = 0;
        for (int i = 0; i < 8; i++)
        {
            version ^= (int)src[i] & 0xFF;
        }
        if (version == VERSION)
        {
            int dataLen = src.length-1;
            for (int size = Math.min(dataLen, MAX_BLOCKSIZE); size >= Crypto.MIN_BLOCKSIZE; size--)
            {
                if (dataLen % size == 0)
                {
                    srcType = 2;
                    break;
                }
            }
        }
        for (int size = Math.min(src.length, MAX_BLOCKSIZE); size >= Crypto.MIN_BLOCKSIZE; size--)
        {
            if (src.length % size == 0)
            {
                return srcType|1;
            }
        }
        return srcType;
    }

    byte[] decryptV2(String password, byte[] src) throws IOException
    {
        return decryptV2(getBytes(password), src);
    }

    synchronized byte[] decryptV2(byte[] password, byte[] src) throws IOException
    {
        if (src.length < Crypto.MIN_BLOCKSIZE + 1)
        {
            return null;
        }
        int version = 0;
        for (int i = 0; i < 8; i++)
        {
            version ^= (int)src[i] & 0xFF;
        }
        if (version != VERSION)
        {
            return null;
        }
        ByteArrayInputStream in1 = new ByteArrayInputStream(src);
        ByteArrayOutputStream out1 = new ByteArrayOutputStream(src.length);
        ByteArrayOutputStream out2 = new ByteArrayOutputStream(src.length);
        long[] seed = genSeedV2(password);
        int dataLen = src.length-1;
        for (int size1 = Math.min(dataLen, MAX_BLOCKSIZE); size1 >= Crypto.MIN_BLOCKSIZE; size1--)
        {
            if (dataLen % size1 != 0)
            {
                continue;
            }
            in1.reset();
            in1.skip(1);
            out1.reset();
            rand.setSeed(seed);
            byte[] tmp = null;
            try
            {
                Crypto.decrypt(size1, cs, rand, in1, out1);
                tmp = out1.toByteArray();
            }
            catch (CryptoException ex)
            {
                continue;
            }
            ByteArrayInputStream in2 = new ByteArrayInputStream(tmp);
            for (int size2 = Math.min(tmp.length, MAX_BLOCKSIZE); size2 >= Crypto.MIN_BLOCKSIZE; size2--)
            {
                if (tmp.length % size2 != 0)
                {
                    continue;
                }
                in2.reset();
                out2.reset();
                rand.setSeed(seed);
                try
                {
                    Crypto.decrypt(size2, cs, rand, in2, out2);
                    byte[] ret = out2.toByteArray();
                    seed = null;
                    tmp = null;
                    in1.close(); in1 = null;
                    in2.close(); in2 = null;
                    out1.close(); out1 = null;
                    out2.close(); out2 = null;
                    return ret;
                }
                catch (CryptoException ex)
                {
                    // continue;
                }
            }
            tmp = null;
            in2.close(); in2 = null;
        }
        seed = null;
        in1.close(); in1 = null;
        out1.close(); out1 = null;
        out2.close(); out2 = null;
        return null;
    }

    int calcSize(int srclen, int blockSize)
    {
        int dataSize = blockSize - Crypto.META_SIZE;
        int blockCount = (srclen + dataSize - 1) / dataSize;
        return blockSize * blockCount;
    }

    byte[] encryptV2(String password, byte[] src) throws IOException
    {
        return encryptV2(getBytes(password), src);
    }

    synchronized byte[] encryptV2(byte[] password, byte[] src) throws IOException
    {
        int blockSize = encryptBlockSize(src.length);
        int size = calcSize(src.length, blockSize);
        long[] seed = genSeedV2(password);
        ByteArrayInputStream in = new ByteArrayInputStream(src);
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        rand.setSeed(seed);
        Crypto.encrypt(blockSize, cs, rand, in, out);
        src = out.toByteArray();
        in.close();
        out.close();
        blockSize = encryptBlockSize(src.length);
        size = calcSize(src.length, blockSize);
        in = new ByteArrayInputStream(src);
        out = new ByteArrayOutputStream(size+1);
        out.write(VERSION);
        rand.setSeed(seed);
        Crypto.encrypt(blockSize, cs, rand, in, out);
        byte[] ret = out.toByteArray();
        in.close();
        out.close();
        for (int i = 1; i < 8; i++)
        {
            ret[0] = (byte)((int)ret[0] ^ (int)ret[i]);
        }
        return ret;
    }
}
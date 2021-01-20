package idpwmemo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import neetsdkasu.util.zip.Checksum;
import neetsdkasu.util.zip.CRC32;

import mt19937ar.MTRandom;
import neetsdkasu.crypto.oap.Crypto;
import neetsdkasu.crypto.CryptoException;

final class Cryptor
{
    static final int MAX_BLOCKSIZE = Math.min(1024, Crypto.MAX_BLOCKSIZE);
    static final String CHARSET = "UTF-8";

    static byte[] getBytes(String s) throws IOException
    {
        return s.getBytes(CHARSET);
    }

    static final Cryptor instance = new Cryptor();

    private final MTRandom rand = new MTRandom() {
        public int nextInt()
        {
            return next(32); // In OpenAppli-JavaMachine-java.util.Random, nextInt() method does not use next(int) method...
        }
    };
    private final Checksum cs = new CRC32();

    Cryptor() {}

    private static long[] genSeed(int size)
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

    private static long[] genSeed(byte[] password)
    {
        if (password == null || password.length == 0)
        {
            return genSeed(23);
        }
        long[] seed = genSeed(password.length + 13);
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

    byte[] decrypt(String password, byte[] src) throws IOException
    {
        return decrypt(getBytes(password), src);
    }

    synchronized byte[] decrypt(byte[] password, byte[] src) throws IOException
    {
        ByteArrayInputStream in = new ByteArrayInputStream(src);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long[] seed = genSeed(password);
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

    byte[] encrypt(String password, byte[] src) throws IOException
    {
        return encrypt(getBytes(password), src);
    }

    synchronized byte[] encrypt(byte[] password, byte[] src) throws IOException
    {
        int blockSize = encryptBlockSize(src.length);
        ByteArrayInputStream in = new ByteArrayInputStream(src);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        rand.setSeed(genSeed(password));
        Crypto.encrypt(blockSize, cs, rand, in, out);
        byte[] ret = out.toByteArray();
        in.close();
        out.close();
        return ret;
    }

    byte[] decrypt_repeat(int times, String password, byte[] src) throws IOException
    {
        return decrypt_repeat(times, getBytes(password), src);
    }

    byte[] decrypt_repeat(int times, byte[] password, byte[] src) throws IOException
    {
        for (int i = 0; i < times; i++)
        {
            src = decrypt(password, src);
            if (src == null)
            {
                return null;
            }
        }
        return src;
    }

    byte[] encrypt_repeat(int times, String password, byte[] src) throws IOException
    {
        return encrypt_repeat(times, getBytes(password), src);
    }

    byte[] encrypt_repeat(int times, byte[] password, byte[] src) throws IOException
    {
        for (int i = 0; i < times; i++)
        {
            src = encrypt(password, src);
        }
        return src;
    }
}
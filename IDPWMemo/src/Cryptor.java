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
    static final Cryptor instance = new Cryptor();

    private final MTRandom rand = new MTRandom();
    private final Checksum cs = new CRC32();

    private Cryptor() {}

    private static long[] genSeed(int size)
    {
        long[] seed = new long[size];
        seed[0] = 0x9876_5432L;
        seed[1] = 0xF1E2_D3C4L;
        for (int i = 2; i < seed.length; i++)
        {
            seed[i] = seed[i - 2] ^ (seed[i - 1] >> ((i - 1) & 0xF)) ^ (seed[i - 1] << ((i + i + 1) & 0xF));
            seed[i] &= 0xFFFF_FFFFL;
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
                password[p] = 0; /* clear password */
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
        if (size <= Crypto.MAX_BLOCKSIZE)
        {
            return size;
        }
        size = Crypto.MIN_BLOCKSIZE;
        int blockCount = (srclen + (size - Crypto.META_SIZE) - 1) / (size - Crypto.META_SIZE);
        int totalSize = size * blockCount;
        for (int sz = Crypto.MIN_BLOCKSIZE + 1; sz <= Crypto.MAX_BLOCKSIZE; sz++)
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

    synchronized byte[] decrypt(byte[] password, byte[] src) throws IOException
    {
        ByteArrayInputStream in = new ByteArrayInputStream(src);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long[] seed = genSeed(password);
        for (int size = Math.min(src.length, Crypto.MAX_BLOCKSIZE); size >= Crypto.MIN_BLOCKSIZE; size--)
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
                return out.toByteArray();
            }
            catch (CryptoException ex)
            {
                // continue;
            }
        }
        return null;
    }

    synchronized byte[] encrypt(byte[] password, byte[] src) throws IOException
    {
        int blockSize = encryptBlockSize(src.length);
        ByteArrayInputStream in = new ByteArrayInputStream(src);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        rand.setSeed(genSeed(password));
        Crypto.encrypt(blockSize, cs, rand, in, out);
        return out.toByteArray();
    }
}
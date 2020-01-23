
import java.lang.annotation.*;
import java.lang.reflect.*;

import java.util.Calendar;
import java.util.Random;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import neetsdkasu.crypto.Crypto;
import neetsdkasu.crypto.CryptoException;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Test {}

class CryptoTest
{
    public static void main(String[] args)
    {
        for (Method m : CryptoTest.class.getDeclaredMethods())
        {
            if (m.isAnnotationPresent(Test.class))
            {
                Calendar cal = Calendar.getInstance();
                System.out.print("[" + cal.getTime() + "]");
                System.out.print(" " + m.getName() + ": ");
                try
                {
                    m.invoke(null);
                    System.out.println("passed");
                }
                catch (Exception ex)
                {
                    System.out.println("failed");
                    ex.printStackTrace();
                    break;
                }
            }
        }
    }

    @Test
    static void testRandomWithAdler32()
    {
        run10000CycleAtRandom(new Adler32(), new RandomInstanceProvider() {
            public Random getInstance()
            {
                return new Random();
            }
        });
    }

    @Test
    static void testRandomWithCRC32()
    {
        run10000CycleAtRandom(new CRC32(), new RandomInstanceProvider() {
            public Random getInstance()
            {
                return new Random();
            }
        });
    }

    @Test
    static void testSecureRandomWithAdler32()
    {
        run10000CycleAtRandom(new Adler32(), new RandomInstanceProvider() {
            public Random getInstance()
            {
                try
                {
                    return java.security.SecureRandom.getInstance("SHA1PRNG");
                }
                catch (Exception ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    @Test
    static void testSecureRandomWithCRC32()
    {
        run10000CycleAtRandom(new CRC32(), new RandomInstanceProvider() {
            public Random getInstance()
            {
                try
                {
                    return java.security.SecureRandom.getInstance("SHA1PRNG");
                }
                catch (Exception ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    static interface RandomInstanceProvider
    {
        Random getInstance();
    }

    static void run10000CycleAtRandom(Checksum cs, RandomInstanceProvider prov)
    {
        Random rand = prov.getInstance();
        rand.setSeed(System.currentTimeMillis());
        long seed = 0;
        for (int cy = 0; cy < 10000; cy++)
        {
            seed = rand.nextLong();

            byte[] originalData = new byte[rand.nextInt(Crypto.MAX_BLOCKSIZE * 5)];
            rand.nextBytes(originalData);

            int blockSize = rand.nextInt(Crypto.MAX_BLOCKSIZE - Crypto.MIN_BLOCKSIZE + 1) + Crypto.MIN_BLOCKSIZE;

            rand = prov.getInstance();
            rand.setSeed(seed);
            byte[] secret = Crypto.encrypt(blockSize, cs, rand, originalData);

            rand = prov.getInstance();
            rand.setSeed(seed);
            byte[] recover = Crypto.decrypt(blockSize, cs, rand, secret);

            if (originalData.length != recover.length)
            {
                throw new RuntimeException("length invalid");
            }

            for (int i = 0; i < originalData.length; i++)
            {
                if (originalData[i] != recover[i])
                {
                    throw new RuntimeException("unmatch data");
                }
            }
        }
    }

}

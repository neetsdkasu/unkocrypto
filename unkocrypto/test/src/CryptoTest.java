
import java.lang.annotation.*;
import java.lang.reflect.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
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
    public static void main(String[] args) throws Exception
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
    static void testRandomWithAdler32() throws Exception
    {
        run10000CycleAtRandom(new Adler32(), new RandomInstanceProvider() {
            public Random getInstance(long seed)
            {
                return new Random(seed);
            }
        });
    }

    @Test
    static void testRandomWithCRC32() throws Exception
    {
        run10000CycleAtRandom(new CRC32(), new RandomInstanceProvider() {
            public Random getInstance(long seed)
            {
                return new Random(seed);
            }
        });
    }

    @Test
    static void testSecureRandomWithAdler32() throws Exception
    {
        run10000CycleAtRandom(new Adler32(), new RandomInstanceProvider() {
            public Random getInstance(long seed)
            {
                try
                {
                    Random rand = java.security.SecureRandom.getInstance("SHA1PRNG");
                    rand.setSeed(seed);
                    return rand;
                }
                catch (Exception ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    @Test
    static void testSecureRandomWithCRC32() throws Exception
    {
        run10000CycleAtRandom(new CRC32(), new RandomInstanceProvider() {
            public Random getInstance(long seed)
            {
                try
                {
                    Random rand = java.security.SecureRandom.getInstance("SHA1PRNG");
                    rand.setSeed(seed);
                    return rand;
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
        Random getInstance(long seed);
    }

    static void run10000CycleAtRandom(Checksum cs, RandomInstanceProvider prov) throws Exception
    {
        Random rand = prov.getInstance(System.currentTimeMillis());
        long seed = 0;
        for (int cy = 0; cy < 10000; cy++)
        {
            seed = rand.nextLong();

            byte[] originalBuffer = new byte[rand.nextInt(Crypto.MAX_BLOCKSIZE * 5)];
            rand.nextBytes(originalBuffer);

            int blockSize = rand.nextInt(Crypto.MAX_BLOCKSIZE - Crypto.MIN_BLOCKSIZE + 1) + Crypto.MIN_BLOCKSIZE;

            ByteArrayInputStream originalData = new ByteArrayInputStream(originalBuffer);
            ByteArrayOutputStream secret = new ByteArrayOutputStream();
            rand = prov.getInstance(seed);
            int secretSize = Crypto.encrypt(blockSize, cs, rand, originalData, secret);
            byte[] secretBuffer = secret.toByteArray();
            if (secretBuffer.length != secretSize)
            {
                throw new RuntimeException("invalid secret buffer size");
            }

            ByteArrayInputStream secretData = new ByteArrayInputStream(secretBuffer);
            ByteArrayOutputStream recoverData = new ByteArrayOutputStream();
            rand = prov.getInstance(seed);
            int recoverSize = Crypto.decrypt(blockSize, cs, rand, secretData, recoverData);
            byte[] recover = recoverData.toByteArray();
            if (recover.length != recoverSize)
            {
                throw new RuntimeException("invalid recover buffer size");
            }

            if (!Arrays.equals(originalBuffer, recover))
            {
                throw new RuntimeException("unmatch data");
            }
        }
    }

}

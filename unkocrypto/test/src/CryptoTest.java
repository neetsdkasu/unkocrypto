
import java.lang.annotation.*;
import java.lang.reflect.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
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
        String target = null;
        if (args.length > 0)
        {
            target = args[0];
        }

        if ("demo".equals(target))
        {
            demo();
            return;
        }

        for (Method m : CryptoTest.class.getDeclaredMethods())
        {
            if (m.isAnnotationPresent(Test.class))
            {
                if (target != null && m.getName().indexOf(target) < 0)
                {
                    continue;
                }
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
        System.out.println("done.");
    }

    @Test
    static void testRandomWithAdler32() throws Exception
    {
        run10000CycleAtRandom(new Adler32(), new RandomInstanceProvider() {
            Random rand = new Random();
            public Random getInstance(long seed)
            {
                rand.setSeed(seed);
                return rand;
            }
        });
    }

    @Test
    static void testRandomWithCRC32() throws Exception
    {
        run10000CycleAtRandom(new CRC32(), new RandomInstanceProvider() {
            Random rand = new Random();
            public Random getInstance(long seed)
            {
                rand.setSeed(seed);
                return rand;
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

    @Test
    static void testMersenneTwisterWithAdler32() throws Exception
    {
        run10000CycleAtRandom(new Adler32(), new RandomInstanceProvider() {
            Random rand = new mt19937ar.Random();
            public Random getInstance(long seed)
            {
                rand.setSeed(seed);
                return rand;
            }
        });
    }

    @Test
    static void testMersenneTwisterWithCRC32() throws Exception
    {
        run10000CycleAtRandom(new CRC32(), new RandomInstanceProvider() {
            Random rand = new mt19937ar.Random();
            public Random getInstance(long seed)
            {
                rand.setSeed(seed);
                return rand;
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

    static final String ErrorMessageChecksumIsNull = "checksum is null";

    @Test
    static void testChecksumIsNullCallEncrypt() throws Exception
    {
        try
        {
            Crypto.encrypt(Crypto.MIN_BLOCKSIZE, null,
                new Random(), new DummyInputStream(), new DummyOutputStream());
        }
        catch (IllegalArgumentException ex)
        {
            if (ErrorMessageChecksumIsNull.equals(ex.getMessage()))
            {
                return;
            }
            throw ex;
        }
        throw new RuntimeException("invalid check checksum is null");
    }

    @Test
    static void testChecksumIsNullCallDecrypt() throws Exception
    {
        try
        {
            Crypto.decrypt(Crypto.MIN_BLOCKSIZE, null,
                new Random(), new DummyInputStream(), new DummyOutputStream());
        }
        catch (IllegalArgumentException ex)
        {
            if (ErrorMessageChecksumIsNull.equals(ex.getMessage()))
            {
                return;
            }
            throw ex;
        }
        throw new RuntimeException("invalid check checksum is null");
    }

    static final String ErrorMessageRandIsNull = "rand is null";

    @Test
    static void testRandIsNullCallEncrypt() throws Exception
    {
        try
        {
            Crypto.encrypt(Crypto.MIN_BLOCKSIZE, new Adler32(),
                null, new DummyInputStream(), new DummyOutputStream());
        }
        catch (IllegalArgumentException ex)
        {
            if (ErrorMessageRandIsNull.equals(ex.getMessage()))
            {
                return;
            }
            throw ex;
        }
        throw new RuntimeException("invalid check rand is null");
    }

    @Test
    static void testRandIsNullCallDecrypt() throws Exception
    {
        try
        {
            Crypto.decrypt(Crypto.MIN_BLOCKSIZE, new Adler32(),
                null, new DummyInputStream(), new DummyOutputStream());
        }
        catch (IllegalArgumentException ex)
        {
            if (ErrorMessageRandIsNull.equals(ex.getMessage()))
            {
                return;
            }
            throw ex;
        }
        throw new RuntimeException("invalid check rand is null");
    }

    static final String ErrorMessageSrcIsNull = "src is null";

    @Test
    static void testSrcIsNullCallEncrypt() throws Exception
    {
        try
        {
            Crypto.encrypt(Crypto.MIN_BLOCKSIZE, new Adler32(),
                new Random(), null, new DummyOutputStream());
        }
        catch (IllegalArgumentException ex)
        {
            if (ErrorMessageSrcIsNull.equals(ex.getMessage()))
            {
                return;
            }
            throw ex;
        }
        throw new RuntimeException("invalid check src is null");
    }

    @Test
    static void testSrcIsNullCallDecrypt() throws Exception
    {
        try
        {
            Crypto.decrypt(Crypto.MIN_BLOCKSIZE, new Adler32(),
                new Random(), null, new DummyOutputStream());
        }
        catch (IllegalArgumentException ex)
        {
            if (ErrorMessageSrcIsNull.equals(ex.getMessage()))
            {
                return;
            }
            throw ex;
        }
        throw new RuntimeException("invalid check src is null");
    }

    static final String ErrorMessageDstIsNull = "dst is null";

    @Test
    static void testDstIsNullCallEncrypt() throws Exception
    {
        try
        {
            Crypto.encrypt(Crypto.MIN_BLOCKSIZE, new Adler32(),
                new Random(), new DummyInputStream(), null);
        }
        catch (IllegalArgumentException ex)
        {
            if (ErrorMessageDstIsNull.equals(ex.getMessage()))
            {
                return;
            }
            throw ex;
        }
        throw new RuntimeException("invalid check dst is null");
    }

    @Test
    static void testDstIsNullCallDecrypt() throws Exception
    {
        try
        {
            Crypto.decrypt(Crypto.MIN_BLOCKSIZE, new Adler32(),
                new Random(), new DummyInputStream(), null);
        }
        catch (IllegalArgumentException ex)
        {
            if (ErrorMessageDstIsNull.equals(ex.getMessage()))
            {
                return;
            }
            throw ex;
        }
        throw new RuntimeException("invalid check dst is null");
    }

    static String errorMessageIlligalBlockSize(int blockSize)
    {
        return "OUT OF RANGE blockSize(" + blockSize + "): "
            + Crypto.MIN_BLOCKSIZE + " <= blockSize <= " + Crypto.MAX_BLOCKSIZE;
    }

    @Test
    static void testInvalidBlockSizeCallEncrypt() throws Exception
    {
        int[] testSizes = {
            Crypto.MIN_BLOCKSIZE - 1,
            Crypto.MAX_BLOCKSIZE + 1,
            0,
            -1
        };
        for (int blockSize : testSizes)
        {
            String errorMessage = errorMessageIlligalBlockSize(blockSize);
            try
            {
                Crypto.encrypt(blockSize, new Adler32(),
                    new Random(), new DummyInputStream(), new DummyOutputStream());
            }
            catch (IllegalArgumentException ex)
            {
                if (errorMessage.equals(ex.getMessage()))
                {
                    continue;
                }
                throw ex;
            }
            throw new RuntimeException("invalid size check at " + blockSize);
        }
    }

    @Test
    static void testInvalidBlockSizeCallDecrypt() throws Exception
    {
        int[] testSizes = {
            Crypto.MIN_BLOCKSIZE - 1,
            Crypto.MAX_BLOCKSIZE + 1,
            0,
            -1
        };
        for (int blockSize : testSizes)
        {
            String errorMessage = errorMessageIlligalBlockSize(blockSize);
            try
            {
                Crypto.decrypt(blockSize, new Adler32(),
                    new Random(), new DummyInputStream(), new DummyOutputStream());
            }
            catch (IllegalArgumentException ex)
            {
                if (errorMessage.equals(ex.getMessage()))
                {
                    continue;
                }
                throw ex;
            }
            throw new RuntimeException("invalid size check at " + blockSize);
        }
    }

    @Test
    static void testZeroSizeData() throws Exception
    {
        long seed = 123456789L;
        Random rand = new Random();
        Checksum cs = new CRC32();
        int blockSize = (Crypto.MAX_BLOCKSIZE + Crypto.MIN_BLOCKSIZE) / 2;

        InputStream zeroSizeData = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream secret = new ByteArrayOutputStream();
        rand.setSeed(seed);
        int encrytedSize = Crypto.encrypt(blockSize, cs, rand, zeroSizeData, secret);
        if (encrytedSize != blockSize)
        {
            throw new RuntimeException("invalid encrytedSize: " + encrytedSize);
        }
        InputStream secretData = new ByteArrayInputStream(secret.toByteArray());
        OutputStream recover = new DummyOutputStream();
        rand.setSeed(seed);
        int decryptedSize = Crypto.decrypt(blockSize, cs, rand, secretData, recover);
        if (decryptedSize != 0)
        {
            throw new RuntimeException("invalid decryptedSize: " + decryptedSize);
        }
    }

    @Test
    static void testBoundaryValueBlockSize() throws Exception
    {
        int[] blockSizes = { Crypto.MIN_BLOCKSIZE, Crypto.MAX_BLOCKSIZE };
        int[] dataSizes = {
            Crypto.MIN_BLOCKSIZE / 2,
            Crypto.MIN_BLOCKSIZE - Crypto.META_SIZE,
            Crypto.MIN_BLOCKSIZE,
            (Crypto.MIN_BLOCKSIZE - Crypto.META_SIZE) * 2,
            Crypto.MIN_BLOCKSIZE * 2,
            Crypto.MAX_BLOCKSIZE / 2,
            Crypto.MAX_BLOCKSIZE - Crypto.META_SIZE,
            Crypto.MAX_BLOCKSIZE,
            (Crypto.MAX_BLOCKSIZE - Crypto.META_SIZE) * 2,
            Crypto.MAX_BLOCKSIZE * 2
        };
        long seed = 123456789L;
        Random rand = new Random();
        Checksum cs = new CRC32();
        for (int blockSize : blockSizes)
        {
            for (int dataSize : dataSizes)
            {
                byte[] buffer = new byte[dataSize];
                rand.nextBytes(buffer);

                InputStream zeroSizeData = new ByteArrayInputStream(buffer);
                ByteArrayOutputStream secret = new ByteArrayOutputStream();
                rand.setSeed(seed);
                int encrytedSize = Crypto.encrypt(blockSize, cs, rand, zeroSizeData, secret);
                byte[] secretBuffer = secret.toByteArray();
                if (encrytedSize != secretBuffer.length)
                {
                    throw new RuntimeException("invalid encrytedSize");
                }

                InputStream secretData = new ByteArrayInputStream(secretBuffer);
                ByteArrayOutputStream recover = new ByteArrayOutputStream();
                rand.setSeed(seed);
                int decryptedSize = Crypto.decrypt(blockSize, cs, rand, secretData, recover);
                byte[] recoverBuffer = recover.toByteArray();
                if (decryptedSize != recoverBuffer.length)
                {
                    throw new RuntimeException("invalid decryptedSize");
                }

                if (!Arrays.equals(buffer, recoverBuffer))
                {
                    throw new RuntimeException("unmatch data");
                }
            }
        }
    }

    static void demo() throws Exception
    {
        byte[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        long seed = 123456789L;
        Random rand = new Random();
        Checksum cs = new CRC32();
        int blockSize = Crypto.MIN_BLOCKSIZE;

        System.out.println("seed: " + seed);
        System.out.println("blockSize: " + blockSize);
        System.out.println("data:");
        showBytes(data);
        /* (stdout)
            seed: 123456789
            blockSize: 32
            data:
             1,  2,  3,  4,  5,  6,  7,  8,  9,  a,
        */

        InputStream src = new ByteArrayInputStream(data);
        ByteArrayOutputStream dst = new ByteArrayOutputStream();
        rand.setSeed(seed);

        // Encrypt
        Crypto.encrypt(blockSize, cs, rand, src, dst);

        byte[] secret = dst.toByteArray();
        System.out.println("secret:");
        showBytes(secret);
        /* (stdout)
            secret:
            a2, fc, 45, 90, 64, 80, 77, 46, 3f, 7e,
            1d, 7c, 64, fe, 5c, 98, 7a,  0, 79, a8,
            64, f2, 7d, c1, e3, 66, 31, 31, 1e, 62,
            b6,  4,
        */

        src = new ByteArrayInputStream(secret);
        dst.reset();
        rand.setSeed(seed);

        // Decrypt
        Crypto.decrypt(blockSize, cs, rand, src, dst);

        byte[] recover = dst.toByteArray();
        System.out.println("recover:");
        showBytes(recover);
        /* (stdout)
            recover:
             1,  2,  3,  4,  5,  6,  7,  8,  9,  a,
        */

        if (Arrays.equals(data, recover))
        {
            System.out.println("match!");
        }
        /* (stdout)
            match!
        */
    }

    static void showBytes(byte[] buf)
    {
        for (int i = 0; i < buf.length; i++)
        {
            System.out.printf("%2x, ", 0xFF & (int)buf[i]);
            if (i % 10 == 9) System.out.println();
        }
        System.out.println();
    }

}

class DummyInputStream extends InputStream
{
    @Override
    public int read() throws IOException
    {
        throw new UnsupportedOperationException();
    }
}

class DummyOutputStream extends OutputStream
{
    @Override
    public void write(int b) throws IOException
    {
        throw new UnsupportedOperationException();
    }
}

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
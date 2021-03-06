import javax.microedition.midlet.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Random;
import neetsdkasu.util.zip.Adler32;
import neetsdkasu.util.zip.CRC32;
import neetsdkasu.util.zip.Checksum;

import neetsdkasu.crypto.oap.Crypto;
import neetsdkasu.crypto.CryptoException;

public class TestCryptoMIDlet extends MIDlet
{
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {}
    protected void pauseApp() {}
    protected void startApp() throws MIDletStateChangeException
    {

        {
            Calendar cal = Calendar.getInstance();
            System.out.print("[" + cal.getTime() + "]");
            System.out.println(" start.");
        }
        
        // System.out.flush() does not work.

        try
        {
            Calendar cal = Calendar.getInstance();
            System.out.print("[" + cal.getTime() + "] ");
            System.out.print("testRandomWithAdler32: ");
            System.out.flush();
            testRandomWithAdler32();
            System.out.println("passed");
        }
        catch (Exception ex)
        {
            System.out.println("failed");
            ex.printStackTrace();
            notifyDestroyed();
            return;
        }

        try
        {
            Calendar cal = Calendar.getInstance();
            System.out.print("[" + cal.getTime() + "] ");
            System.out.print("testRandomWithCRC32: ");
            System.out.flush();
            testRandomWithCRC32();
            System.out.println("passed");
        }
        catch (Exception ex)
        {
            System.out.println("failed");
            ex.printStackTrace();
            notifyDestroyed();
            return;
        }

        try
        {
            Calendar cal = Calendar.getInstance();
            System.out.print("[" + cal.getTime() + "] ");
            System.out.print("testMersenneTwisterWithAdler32: ");
            System.out.flush();
            testMersenneTwisterWithAdler32();
            System.out.println("passed");
        }
        catch (Exception ex)
        {
            System.out.println("failed");
            ex.printStackTrace();
            notifyDestroyed();
            return;
        }

        try
        {
            Calendar cal = Calendar.getInstance();
            System.out.print("[" + cal.getTime() + "] ");
            System.out.print("testMersenneTwisterWithCRC32: ");
            System.out.flush();
            testMersenneTwisterWithCRC32();
            System.out.println("passed");
        }
        catch (Exception ex)
        {
            System.out.println("failed");
            ex.printStackTrace();
            notifyDestroyed();
            return;
        }

        {
            Calendar cal = Calendar.getInstance();
            System.out.print("[" + cal.getTime() + "] ");
            System.out.println("done.");
        }
        notifyDestroyed();
    }

    static void testRandomWithAdler32() throws Exception
    {
        run50CycleAtRandom(new Adler32(), new RandomInstanceProvider() {
            Random rand = new Random();
            public Random getInstance(long seed)
            {
                rand.setSeed(seed);
                return rand;
            }
        });
    }

    static void testRandomWithCRC32() throws Exception
    {
        run50CycleAtRandom(new CRC32(), new RandomInstanceProvider() {
            Random rand = new Random();
            public Random getInstance(long seed)
            {
                rand.setSeed(seed);
                return rand;
            }
        });
    }

    static void testMersenneTwisterWithAdler32() throws Exception
    {
        run50CycleAtRandom(new Adler32(), new RandomInstanceProvider() {
            Random rand = new mt19937ar.MTRandom();
            public Random getInstance(long seed)
            {
                rand.setSeed(seed);
                return rand;
            }
        });
    }

    static void testMersenneTwisterWithCRC32() throws Exception
    {
        run50CycleAtRandom(new CRC32(), new RandomInstanceProvider() {
            Random rand = new mt19937ar.MTRandom();
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

    static void run50CycleAtRandom(Checksum cs, RandomInstanceProvider prov) throws Exception
    {
        final int MAX_BLOCKSIZE = Math.min(1 << 16, Crypto.MAX_BLOCKSIZE);

        Random rand = prov.getInstance(System.currentTimeMillis());
        long seed = 0;
        for (int cy = 0; cy < 50; cy++)
        {
            seed = rand.nextLong();

            byte[] originalBuffer = new byte[rand.nextInt(MAX_BLOCKSIZE * 3)];
            nextBytes(rand, originalBuffer);

            int blockSize = rand.nextInt(MAX_BLOCKSIZE - Crypto.MIN_BLOCKSIZE + 1) + Crypto.MIN_BLOCKSIZE;

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

            // Total Memory Limit 1MB?
            secret.close();
            recoverData.close();
            originalBuffer = null;
            secretBuffer = null;
            recover = null;
            System.gc();
        }
    }

    static void nextBytes(Random rand, byte[] buf)
    {
        for (int i = 0; i < buf.length; i++)
        {
            buf[i] = (byte)rand.nextInt(256);
        }
    }

}

class Arrays
{
    static boolean equals(byte[] buf1, byte[] buf2)
    {
        if (buf1 == null || buf2 == null)
        {
            return buf1 == buf2;
        }
        if (buf1.length != buf2.length)
        {
            return false;
        }
        for (int i = 0; i < buf1.length; i++)
        {
            if (buf1[i] != buf2[i])
            {
                return false;
            }
        }
        return true;
    }
}